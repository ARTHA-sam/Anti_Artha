package demo;

public class ProductNotFoundException extends Exception {
    public ProductNotFoundException(String id) {
        super("Product with ID " + id + " not found");
    }
}
