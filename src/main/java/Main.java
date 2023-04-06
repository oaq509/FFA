import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.sql.*;

public class Main {
    public static void main(String[] args) {

        System.out.println("###############################################");
        System.out.println("Welcome to the Fake Financial Application (FFA)");
        System.out.println("Powered By Owais Algarni ~ SDAIA");
        System.out.println("###############################################");

        // MySQL database configuration connection here
        String dbUrl = "jdbc:mysql://localhost:3306/ffa";
        String dbUser = "root";
        String dbPassword = "";

        // Create a list of companies
        List<Company> companies = Arrays.asList(
                new Company("SABIC", "Saudi Basic Industries Corporation"),
                new Company("STC", "Saudi Telecom Company"),
                new Company("MOBILY", "Etihad Etisalat Company"),
                new Company("RIBL", "Riyad Bank"),
                new Company("MAADEN", "Saudi Arabian Mining Company")
        );

        // Initialize the Akka system
        ActorSystem system = ActorSystem.create("FFA");

        // Create the auditor actor
        ActorRef auditor = system.actorOf(Auditor.props(dbUrl, dbUser, dbPassword), "auditor");

        // Create the trader actors
        ActorRef trader1 = system.actorOf(Trader.props("T1", 10500, auditor), "Owais");
        ActorRef trader2 = system.actorOf(Trader.props("T2", 4200, auditor), "Mohammed");
        ActorRef trader3 = system.actorOf(Trader.props("T3", 900, auditor), "Ali");

        // Create the quote generator actor
        ActorRef quoteGenerator = system.actorOf(QuoteGenerator.props(companies, dbUrl, dbUser, dbPassword), "quoteGenerator");

        // Schedule quote generation
        system.scheduler().scheduleAtFixedRate(
                Duration.ZERO, Duration.ofSeconds(1), quoteGenerator, "generate",
                system.dispatcher(), null);

        // Schedule quote retrieval and forwarding to traders
        system.scheduler().scheduleAtFixedRate(
                Duration.ofMillis(500), Duration.ofSeconds(1), () -> {
                    List<Quote> quotes = fetchQuotes(dbUrl, dbUser, dbPassword);
                    for (Quote quote : quotes) {
                        trader1.tell(quote, ActorRef.noSender());
                        trader2.tell(quote, ActorRef.noSender());
                        trader3.tell(quote, ActorRef.noSender());
                    }
                }, system.dispatcher());
    }

    private static List<Quote> fetchQuotes(String dbUrl, String dbUser, String dbPassword) {
        List<Quote> quotes = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String sql = "SELECT q.id, q.symbol, q.price, q.timestamp, c.name " +
                    "FROM quotes q " +
                    "JOIN companies c ON q.symbol = c.symbol " +
                    "WHERE q.timestamp = (SELECT MAX(timestamp) FROM quotes WHERE symbol = q.symbol)";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            ResultSet resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String symbol = resultSet.getString("symbol");
                double price = resultSet.getDouble("price");
                LocalDateTime timestamp = resultSet.getTimestamp("timestamp").toLocalDateTime();
                String companyName = resultSet.getString("name");

                Company company = new Company(symbol, companyName);
                Quote quote = new Quote(company, price);

                quotes.add(quote);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return quotes;
    }
}