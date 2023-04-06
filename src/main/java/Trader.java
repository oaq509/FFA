import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.HashMap;
import java.util.Map;

public class Trader extends AbstractActor {

    private final String traderId;
    private double cash;
    private final Map<String, Integer> portfolio;
    private final ActorRef auditor; // Add auditor instance variable

    public Trader(String traderId, double cash, ActorRef auditor) { // Add auditor parameter
        this.traderId = traderId;
        this.cash = cash;
        this.portfolio = new HashMap<>();
        this.auditor = auditor; // Assign auditor parameter
    }

    public static Props props(String traderId, double cash, ActorRef auditor) { // Add auditor parameter
        return Props.create(Trader.class, () -> new Trader(traderId, cash, auditor)); // Pass auditor parameter
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Quote.class, this::handleQuote)
                .build();
    }

    private void handleQuote(Quote quote) {
        // Simple trading strategy: buy if price is below 50, sell if price is above 100
        double buyThreshold = 50.0;
        double sellThreshold = 60.0;

        String symbol = quote.getCompany().getSymbol();
        double price = quote.getPrice();
        int quantity = 1; // Trade one share at a time

        if (price < buyThreshold && cash >= price * quantity) {
            cash -= price * quantity;
            portfolio.put(symbol, portfolio.getOrDefault(symbol, 0) + quantity);
            System.out.printf("%s bought %d shares of %s at %.2f. Cash: %.2f%n", traderId, quantity, symbol, price, cash);
            Trade trade = new Trade(traderId, quote, quantity, true); // Store trade
            auditor.tell(trade, getSelf());
        } else if (price > sellThreshold && portfolio.getOrDefault(symbol, 0) >= quantity) {
            cash += price * quantity;
            portfolio.put(symbol, portfolio.get(symbol) - quantity);
            System.out.printf("%s sold %d shares of %s at %.2f. Cash: %.2f%n", traderId, quantity, symbol, price, cash);
            Trade trade = new Trade(traderId, quote, quantity, false); // Store trade
            auditor.tell(trade, getSelf());
        }
    }
}