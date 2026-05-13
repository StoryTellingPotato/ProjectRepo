package dao;

import model.Product;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FILE: dao/ProductDAO.java
 * ROLE: All database operations for the `products` table.
 *
 * CHANGES from original:
 *   - SQL queries now include category_id, supplier_id, unit columns
 *   - Product constructor calls updated to match new 6/7-field model
 *   - searchProducts and getLowStockProducts retained and improved
 *   - restockProduct retained
 *
 * UI Usage:
 *   ProductDAO dao = new ProductDAO();
 *   dao.addProduct(p)               → INSERT
 *   dao.getAllProducts()            → List<Product> for JTable
 *   dao.updateProduct(p)           → UPDATE
 *   dao.deleteProduct(id)          → DELETE
 *   dao.searchProducts(keyword)    → LIKE search
 *   dao.getLowStockProducts(10)    → restock alert
 *   dao.restockProduct(id, qty)    → quick stock add
 */
public class ProductDAO {

    // ── ADD ───────────────────────────────────────────────────────────
    /**
     * UI: collect name, category, supplier, price, stock, unit from form fields.
     *   Product p = new Product(name, categoryId, supplierId, price, stock, unit);
     *   boolean ok = dao.addProduct(p);
     *   if (ok) refreshTable();
     */
    public boolean addProduct(Product p) {
        String sql = "INSERT INTO products (name, category_id, supplier_id, price, stock, unit) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getName().trim());
            stmt.setInt(2, p.getCategoryId());
            stmt.setInt(3, p.getSupplierId());
            stmt.setDouble(4, p.getPrice());
            stmt.setInt(5, p.getStock());
            stmt.setString(6, p.getUnit() != null ? p.getUnit().trim() : "pcs");

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ProductDAO] addProduct failed: " + e.getMessage());
            return false;
        }
    }

    // ── GET ALL ───────────────────────────────────────────────────────
    /**
     * UI: call on window open and after every add/update/delete.
     *   List<Product> list = dao.getAllProducts();
     *   tableModel.setRowCount(0);
     *   for (Product p : list) {
     *       tableModel.addRow(new Object[]{
     *           p.getId(), p.getName(), p.getPrice(), p.getStock(), p.getUnit()
     *       });
     *   }
     */
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT id, name, category_id, supplier_id, price, stock, unit "
                + "FROM products ORDER BY name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] getAllProducts failed: " + e.getMessage());
        }
        return list;
    }

    // ── UPDATE ────────────────────────────────────────────────────────
    public boolean updateProduct(Product p) {
        String sql = "UPDATE products SET name = ?, category_id = ?, supplier_id = ?, "
                + "price = ?, stock = ?, unit = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getName().trim());
            stmt.setInt(2, p.getCategoryId());
            stmt.setInt(3, p.getSupplierId());
            stmt.setDouble(4, p.getPrice());
            stmt.setInt(5, p.getStock());
            stmt.setString(6, p.getUnit() != null ? p.getUnit().trim() : "pcs");
            stmt.setInt(7, p.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ProductDAO] updateProduct failed: " + e.getMessage());
            return false;
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ProductDAO] deleteProduct failed: " + e.getMessage());
            return false;
        }
    }

    // ── SEARCH ────────────────────────────────────────────────────────
    /**
     * UI: wire to a search JTextField.
     *   List<Product> results = dao.searchProducts(searchField.getText());
     */
    public List<Product> searchProducts(String keyword) {
        List<Product> results = new ArrayList<>();
        String sql = "SELECT id, name, category_id, supplier_id, price, stock, unit "
                + "FROM products WHERE name LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword.trim() + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] searchProducts failed: " + e.getMessage());
        }
        return results;
    }

    // ── LOW STOCK ─────────────────────────────────────────────────────
    /**
     * UI: check on app startup and show a warning dialog.
     *   List<Product> low = dao.getLowStockProducts(10);
     *   if (!low.isEmpty()) {
     *       JOptionPane.showMessageDialog(frame,
     *           low.size() + " item(s) need restocking!",
     *           "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
     *   }
     */
    public List<Product> getLowStockProducts(int threshold) {
        List<Product> results = new ArrayList<>();
        String sql = "SELECT id, name, category_id, supplier_id, price, stock, unit "
                + "FROM products WHERE stock <= ? ORDER BY stock ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, threshold);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] getLowStockProducts failed: " + e.getMessage());
        }
        return results;
    }

    // ── RESTOCK ───────────────────────────────────────────────────────
    /**
     * Adds qty to current stock without touching other fields.
     * UI: use for a dedicated "Restock" button.
     */
    public boolean restockProduct(int productId, int quantity) {
        if (quantity <= 0) {
            System.err.println("[ProductDAO] Restock quantity must be positive.");
            return false;
        }
        String sql = "UPDATE products SET stock = stock + ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ProductDAO] restockProduct failed: " + e.getMessage());
            return false;
        }
    }

    // ── PRIVATE HELPER ────────────────────────────────────────────────
    // Maps one ResultSet row to a Product object.
    // Avoids duplicating rs.getInt/getString calls across every method.
    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("category_id"),
                rs.getInt("supplier_id"),
                rs.getDouble("price"),
                rs.getInt("stock"),
                rs.getString("unit")
        );
    }
}
