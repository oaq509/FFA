public class Trade {
    private final String traderId;
    private final Quote quote;
    private final int quantity;
    private final boolean buy;

    public Trade(String traderId, Quote quote, int quantity, boolean buy) {
        this.traderId = traderId;
        this.quote = quote;
        this.quantity = quantity;
        this.buy = buy;
    }

    public String getTraderId() {
        return traderId;
    }

    public Quote getQuote() {
        return quote;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isBuy() {
        return buy;
    }
}
