package ee.revelsix.guessnumber.model;

import jakarta.validation.constraints.*;

public class Bet {
    @NotNull(message = "id cannot be null")
    private String id;
    @NotBlank(message = "Nickname cannot be empty")
    private String nickname;
    @NotNull(message = "Bet number cannot be null")
    @Min(value = 1, message = "Bet number must be at least 1")
    @Max(value = 10, message = "Bet number must be at most 10")
    private int number;
    @NotNull(message = "Bet amount cannot be null")
    @Positive(message = "Bet amount must be positive")
    private double amount;

    public Bet() {
    }

    public Bet(String id, String nickname, int number, double amount) {
        this.id = id;
        this.nickname = nickname;
        this.number = number;
        this.amount = amount;
    }

    public @NotNull(message = "id cannot be null") String getId() {
        return id;
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
        return "Bet{" +
                "nickname='" + nickname + '\'' +
                ", number=" + number +
                ", amount=" + amount +
                '}';
    }
}
