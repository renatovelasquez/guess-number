package ee.revelsix.guessnumber.integration;

import ee.revelsix.guessnumber.model.Winner;
import ee.revelsix.guessnumber.scheduler.GameScheduler;
import ee.revelsix.guessnumber.service.GameService;
import jakarta.websocket.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
public class GameIntegrationRTPTest {

    @LocalServerPort
    private int port;

    @Autowired
    private GameService gameService;

    @MockBean
    private GameScheduler schedulerService;

    private final Random random = new Random();
    @Value("${rtp.test.sessions.number}")
    private int numberOfSessions;
    @Value("${rtp.test.batch.size}")
    private int batchSize;
    @Value("${rtp.test.batch.processing.pause}")
    private int batchProcessingPause;
    private String WS_URI;
    private double totalBets = 0;
    private double totalWinnings = 0;
    private WebSocketContainer container;
    private WebSocketClient webSocketClient;

    @BeforeEach
    public void setup() {
        webSocketClient = new WebSocketClient();
        container = ContainerProvider.getWebSocketContainer();
        WS_URI = "ws://localhost:" + port + "/game";
    }

    @Test
    public void testWebSocketMillion() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(24);

        for (int batch = 0; batch < numberOfSessions / batchSize; batch++) {
            // Create and connect sessions asynchronously in batches
            List<CompletableFuture<Session>> futures = IntStream.range(0, batchSize)
                    .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return container.connectToServer(webSocketClient, URI.create(WS_URI));
                        } catch (DeploymentException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    }, executor)).toList();
            // Wait for all futures to complete and collect the sessions
            List<Session> sessions = futures.stream().map(CompletableFuture::join).toList();

            sessions.forEach(session -> {
                try {
                    session.getAsyncRemote().sendText(
                            "{\"nickname\": \"ren\",\"number\": " + (random.nextInt(10) + 1) + ",\"amount\": 1}"
                            , a -> {
                                totalBets += 1;
                                List<Winner> winners = gameService.newRound();
                                for (Winner winner : winners)
                                    totalWinnings += winner.getAmount();
                            }
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            // Close sessions
            sessions.forEach(session -> {
                try {
                    session.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            System.out.println("Batch " + (batch + 1) + " completed.");
            Thread.sleep(batchProcessingPause);
        }

        // Shut down the executor
        executor.shutdown();

        // Simple verification
        System.out.println("All sessions created and messages sent.");

        //Return to Player (RTP) calculation
        double rtp = (totalWinnings / totalBets) * 100;
        System.out.println("RTP: " + rtp);
        assertTrue(rtp >= 50, "RTP should be close to 99%");
    }

    @ClientEndpoint
    public class WebSocketClient {

        @OnOpen
        public void onOpen(Session session) {
            System.out.println("Connected to server");
        }

        @OnMessage
        public void onMessage(String message) {
        }

        @OnError
        public void onError(Session session, Throwable throwable) {
            throwable.printStackTrace();
        }

        @OnClose
        public void onClose(Session session, CloseReason closeReason) {
            System.out.println("Session closed: " + closeReason);
        }
    }
}
