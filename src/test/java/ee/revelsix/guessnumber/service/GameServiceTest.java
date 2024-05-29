package ee.revelsix.guessnumber.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.revelsix.guessnumber.model.WebSocketRequest;
import ee.revelsix.guessnumber.model.Winner;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, OutputCaptureExtension.class})
@SpringBootTest
public class GameServiceTest {

    private GameService gameService;
    private ObjectMapper objectMapper;
    private WebSocketSession mockSession;
    @MockBean
    private Validator validator;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        gameService = new GameService(objectMapper, validator);
        mockSession = mock(WebSocketSession.class);
    }

    @Test
    public void testAddSession(CapturedOutput output) {
        String sessionId = "session1";
        when(mockSession.getId()).thenReturn(sessionId);

        gameService.addSession(mockSession);
        assertThat(output).contains("Session established: session1");
    }

    @Test
    public void testRemoveSession(CapturedOutput output) {
        String sessionId = "session1";
        when(mockSession.getId()).thenReturn(sessionId);

        gameService.addSession(mockSession);
        assertThat(output).contains("Session established: session1");
        gameService.removeSession(mockSession);
        assertThat(output).contains("Session closed: session1");
    }

    @Test
    public void testProcessMessage_ValidBet() throws IOException {
        String sessionId = "session1";
        when(mockSession.getId()).thenReturn(sessionId);
        gameService.addSession(mockSession);

        WebSocketRequest request = new WebSocketRequest("player1", 5, 10.0);
        String message = objectMapper.writeValueAsString(request);

        gameService.processMessage(mockSession, message);

        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(mockSession, times(1)).sendMessage(messageCaptor.capture());

        String expectedMessage = "PLACE BET => " + request;
        assertEquals(expectedMessage, messageCaptor.getValue().getPayload());
    }

    @Test
    public void testNewRound() throws IOException {
        String sessionId = "session1";
        when(mockSession.getId()).thenReturn(sessionId);
        gameService.addSession(mockSession);

        gameService.placeBet(sessionId, "player1", 5, 10.0);
        List<Winner> winners = gameService.newRound();

        assertNotNull(winners);
        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(mockSession, atLeastOnce()).sendMessage(messageCaptor.capture());
    }

    @Test
    public void testNotifyWinners_NoWinners() throws IOException {
        String sessionId = "session1";
        when(mockSession.getId()).thenReturn(sessionId);
        gameService.addSession(mockSession);

        gameService.notifyWinners(List.of());

        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(mockSession, times(1)).sendMessage(messageCaptor.capture());

        assertEquals("No Winners...", messageCaptor.getValue().getPayload());
    }

    @Test
    public void testNotifyWinners_WithWinners() throws IOException {
        String sessionId = "session1";
        when(mockSession.getId()).thenReturn(sessionId);
        gameService.addSession(mockSession);

        Winner winner = new Winner(sessionId, "player1", 99.0);
        gameService.notifyWinners(List.of(winner));

        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(mockSession, times(1)).sendMessage(messageCaptor.capture());

        assertEquals("Winners: [" + winner + "]", messageCaptor.getValue().getPayload());
    }
}
