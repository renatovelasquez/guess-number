package ee.revelsix.guessnumber.model;

public class Winner {
    private final String id;
    private final String nickname;
    private final double amount;

    public Winner(String id, String nickname, double amount) {
        this.id = id;
        this.nickname = nickname;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Winner{" +
                "nickname='" + nickname + '\'' +
                ", amount=" + amount +
                '}';
    }
}
