package controller;

import dao.ProductDAO;
import model.Product;
import util.InputValidator;

import java.util.List;

/**
 * FILE: controller/ProductController.java
 * ROLE: Business logic and validation for product operations.
 *
 * OOP — Abstraction: the UI calls these methods and gets back either
 * a success message, an error message, or a list. It never touches SQL.
 *
 * UI Usage:
 *   ProductController pc = new ProductController();
 *
 *   String result = pc.addProduct(name, categoryId, supplierId,
 *                                  priceText, stockText, unit);
 *   if (result.equals("OK")) { refreshTable(); }
 *   else { JOptionPane.showMessageDialog(frame, result); }
 */
public class ProductController {

    private final ProductDAO productDAO = new ProductDAO();

    // ── ADD ───────────────────────────────────────────────────────────
    /**
     * Validates raw string inputs from text fields, then saves.
     *
     * @return "OK" on success, or an error message string.
     */
    public String addProduct(String name, int categoryId, int supplierId,
                             String priceText, String stockText, String unit) {

        String error = validateInputs(name, priceText, stockText);
        if (error != null) return error;

        Product p = new Product(
                name.trim(),
                categoryId,
                supplierId,
                Double.parseDouble(priceText.trim()),
                Integer.parseInt(stockText.trim()),
                unit.trim().isEmpty() ? "pcs" : unit.trim()
        );

        return productDAO.addProduct(p) ? "OK" : "Failed to save product. Please try again.";
    }

    // ── UPDATE ────────────────────────────────────────────────────────
    public String updateProduct(int id, String name, int categoryId, int supplierId,
                                String priceText, String stockText, String unit) {

        String error = validateInputs(name, priceText, stockText);
        if (error != null) return error;

        Product p = new Product(
                id,
                name.trim(),
                categoryId,
                supplierId,
                Double.parseDouble(priceText.trim()),
                Integer.parseInt(stockText.trim()),
                unit.trim().isEmpty() ? "pcs" : unit.trim()
        );

        return productDAO.updateProduct(p) ? "OK" : "Failed to update product. Please try again.";
    }

    // ── DELETE ────────────────────────────────────────────────────────
    public String deleteProduct(int id) {
        if (id <= 0) return "Invalid product selected.";
        return productDAO.deleteProduct(id) ? "OK" : "Failed to delete product.";
    }

    // ── GET ALL ───────────────────────────────────────────────────────
    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    // ── SEARCH ────────────────────────────────────────────────────────
    public List<Product> searchProducts(String keyword) {
        if (InputValidator.isEmpty(keyword)) return getAllProducts();
        return productDAO.searchProducts(keyword);
    }

    // ── LOW STOCK ─────────────────────────────────────────────────────
    public List<Product> getLowStockProducts(int threshold) {
        return productDAO.getLowStockProducts(threshold);
    }

    // ── RESTOCK ───────────────────────────────────────────────────────
    public String restockProduct(int productId, String quantityText) {
        if (!InputValidator.isPositiveInt(quantityText)) {
            return "Quantity must be a positive whole number.";
        }
        int qty = Integer.parseInt(quantityText.trim());
        return productDAO.restockProduct(productId, qty) ? "OK" : "Failed to restock product.";
    }

    // ── PRIVATE: shared validation ────────────────────────────────────
    private String validateInputs(String name, String priceText, String stockText) {
        if (InputValidator.isEmpty(name))          return "Product name cannot be empty.";
        if (!InputValidator.isPositiveNumber(priceText)) return "Price must be a positive number (e.g. 185.00).";
        if (!InputValidator.isNonNegativeInt(stockText)) return "Stock must be a whole number of 0 or more.";
        return null;  // null = no error
    }
}
