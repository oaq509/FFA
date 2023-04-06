import akka.actor.AbstractActor;
import akka.actor.Props;

import java.sql.*;
import java.util.List;
import java.util.Random;

public class QuoteGenerator extends AbstractActor {
    private final List<Company> companies;
    private final Random random;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public QuoteGenerator(List<Company> companies, String dbUrl, String dbUser, String dbPassword) {
        this.companies = companies;
        this.random = new Random();
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public static Props props(List<Company> companies, String dbUrl, String dbUser, String dbPassword) {
        return Props.create(QuoteGenerator.class, () -> new QuoteGenerator(companies, dbUrl, dbUser, dbPassword));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    if (msg.equals("generate")) {
                        generateQuotes();
                    }
                })
                .build();
    }

    private void generateQuotes() {
        for (Company company : companies) {
            double price = 10 + random.nextDouble() * 90;
            Quote quote = new Quote(company, price);
            publishQuote(quote);
        }
    }

    private void publishQuote(Quote quote) {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String sql = "INSERT INTO quotes (symbol, price) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, quote.getCompany().getSymbol());
            pstmt.setDouble(2, quote.getPrice());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}