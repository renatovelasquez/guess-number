package ee.revelsix.guessnumber.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class WebSocketRequest {
    @NotNull(message = "Nickname cannot be null")
    private String nickname;

    @NotNull(message = "Bet number cannot be null")
    @Min(value = 1, message = "Bet number must be at least 1")
    @Max(value = 10, message = "Bet number must be at most 10")
    private int number;

    @NotNull(message = "Bet amount cannot be null")
    @Positive(message = "Bet amount must be positive")
    private double amount;

    public WebSocketRequest() {
    }

    public WebSocketRequest(String nickname, int number, double amount) {
        this.nickname = nickname;
        this.number = number;
        this.amount = amount;
    }

    public String getNickname() {
        return nickname;
    }

    public int getNumber() {
        return number;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "WebSocketRequest{" +
                "\"nickname\":\"" + nickname + "\"" +
                ", \"number\":" + number +
                ", \"amount\":" + amount +
                "}";
    }
}
