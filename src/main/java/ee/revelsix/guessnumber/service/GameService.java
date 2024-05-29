package ee.revelsix.guessnumber.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.revelsix.guessnumber.model.Bet;
import ee.revelsix.guessnumber.model.WebSocketRequest;
import ee.revelsix.guessnumber.model.Winner;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class GameService {
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final Random random = new Random();
    private final List<Bet> bets = new CopyOnWriteArrayList<>();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    public GameService(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    public void addSession(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        logger.info("Session established: {}", sessionId);
    }

    public void removeSession(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        logger.info("Session closed: {}", sessionId);
    }

    public void processMessage(WebSocketSession session, String message) {
        String textMessage;
        try {
            logger.info("Received message: {}", message);
            WebSocketRequest request = objectMapper.readValue(message, WebSocketRequest.class);
            placeBet(session.getId(), request.getNickname(), request.getNumber(), request.getAmount());
            textMessage = "PLACE BET => " + request;
            logger.info(textMessage);
        } catch (IOException | IllegalArgumentException e) {
            textMessage = e.getMessage();
        }
        sendMessageToSession(session.getId(), textMessage);
    }

    public void placeBet(String id, String nickname, int number, double amount) {
        Bet bet = new Bet(id, nickname, number, amount);
        Set<ConstraintViolation<Bet>> violations = validator.validate(bet);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<Bet> violation : violations) {
                sb.append(violation.getMessage()).append(" | ");
            }
            logger.error("Request validation failed: {}", violations);
            throw new IllegalArgumentException("Invalid bet: " + sb);
        }
        bets.add(bet);
    }

    public List<Winner> newRound() {
        String message = String.format("NEW ROUND AT: %s", new Date());
        logger.info(message);
        sendMessageToAllSessions(message);
        int winningNumber = random.nextInt(10) + 1;
        List<Winner> res = processBets(winningNumber);
        bets.clear();
        return res;
    }

    public List<Winner> processBets(int winningNumber) {
        String message;
        List<Winner> winners = new ArrayList<>();
        for (Bet bet : bets) {
            logger.info(bet.toString());
            if (bet.getNumber() == winningNumber) {
                double winnings = bet.getAmount() * 9.9;
                winners.add(new Winner(bet.getId(), bet.getNickname(), winnings));
                message = "YOU WIN";
            } else
                message = "YOU LOSE";
            sendMessageToSession(bet.getId(), message);
        }
        notifyWinners(winners);
        return winners;
    }

    public void notifyWinners(List<Winner> winners) {
        String message = winners.isEmpty() ? "No Winners..." : "Winners: " + winners;
        sendMessageToAllSessions(message);
        logger.info(message);
    }

    private void sendMessageToSession(String id, String message) {
        try {
            synchronized (sessions.get(id)) {
                sessions.get(id).sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            logger.error("Failed to send message to session: {} - {}", id, e.getMessage());
        }
    }

    private void sendMessageToAllSessions(String message) {
        sessions.values().forEach(s -> sendMessageToSession(s.getId(), message));
    }
}
