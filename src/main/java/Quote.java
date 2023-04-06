public class Quote {
    private final Company company;
    private final double price;

    public Quote(Company company, double price) {
        this.company = company;
        this.price = price;
    }

    public Company getCompany() {
        return company;
    }

    public double getPrice() {
        return price;
    }
}