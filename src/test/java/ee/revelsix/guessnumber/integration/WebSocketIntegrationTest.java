package ee.revelsix.guessnumber.integration;

import ee.revelsix.guessnumber.model.Winner;
import ee.revelsix.guessnumber.scheduler.GameScheduler;
import ee.revelsix.guessnumber.service.GameService;
import jakarta.websocket.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {
    @LocalServerPort
    private int port;
    @Autowired
    private GameService gameService;
    @MockBean
    private GameScheduler schedulerService;
    private BlockingQueue<String> blockingQueue;

    @BeforeEach
    public void setup() {
        blockingQueue = new ArrayBlockingQueue<>(5);
    }

    @Test
    public void testWebSocketCommunication() throws Exception {
        String messageToSend = "{\"nickname\": \"ren\",\"number\": 3,\"amount\": 100}";
        Session session = ContainerProvider.getWebSocketContainer().
                connectToServer(new WebSocketClient(), URI.create("ws://localhost:" + port + "/game"));

        session.getAsyncRemote().sendText(messageToSend);
        // Wait for the response
        String receivedMessage = blockingQueue.poll(5, TimeUnit.SECONDS);

        assertNotNull(receivedMessage);
        assertTrue(receivedMessage.contains("PLACE BET"));

        List<Winner> winners = gameService.newRound();
        receivedMessage = blockingQueue.poll(5, TimeUnit.SECONDS);

        assertNotNull(receivedMessage);
        assertTrue(receivedMessage.contains("NEW ROUND AT:"));

        receivedMessage = blockingQueue.poll(5, TimeUnit.SECONDS);
        assertNotNull(receivedMessage);
        if (winners.isEmpty())
            assertTrue(receivedMessage.startsWith("YOU LOSE"));
        else
            assertTrue(receivedMessage.endsWith("YOU WIN"));
        session.close();
    }

    @ClientEndpoint
    public class WebSocketClient {

        @OnOpen
        public void onOpen(Session session) {
            System.out.println("Connected to server");
        }

        @OnMessage
        public void onMessage(String message) {
            blockingQueue.offer(message);
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