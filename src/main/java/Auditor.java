import akka.actor.AbstractActor;
import akka.actor.Props;

import java.sql.*;

public class Auditor extends AbstractActor {
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public Auditor(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public static Props props(String dbUrl, String dbUser, String dbPassword) {
        return Props.create(Auditor.class, () -> new Auditor(dbUrl, dbUser, dbPassword));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Trade.class, this::handleTrade).build();
    }

    private void handleTrade(Trade trade) {
        storeTrade(trade);
    }

    private void storeTrade(Trade trade) {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String sql = "INSERT INTO trades (trader_id, symbol, price, quantity, buy) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, trade.getTraderId());
            pstmt.setString(2, trade.getQuote().getCompany().getSymbol());
            pstmt.setDouble(3, trade.getQuote().getPrice());
            pstmt.setInt(4, trade.getQuantity());
            pstmt.setBoolean(5, trade.isBuy());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}