package ee.revelsix.guessnumber.handler;

import ee.revelsix.guessnumber.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class GameWebSocketHandlerTest {

    private GameService gameService;
    private GameWebSocketHandler handler;
    private WebSocketSession session;

    @BeforeEach
    public void setUp() {
        gameService = mock(GameService.class);
        handler = new GameWebSocketHandler(gameService);
        session = mock(WebSocketSession.class);
    }

    @Test
    public void testAfterConnectionEstablished() throws Exception {
        handler.afterConnectionEstablished(session);
        verify(gameService, times(1)).addSession(session);
    }

    @Test
    public void testHandleTextMessage() throws Exception {
        String messageContent = "Test message";
        TextMessage message = new TextMessage(messageContent);
        handler.handleTextMessage(session, message);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(gameService, times(1)).processMessage(eq(session), captor.capture());
        assertEquals(messageContent, captor.getValue());
    }

    @Test
    public void testAfterConnectionClosed() throws Exception {
        CloseStatus status = CloseStatus.NORMAL;
        handler.afterConnectionClosed(session, status);
        verify(gameService, times(1)).removeSession(session);
    }
}
