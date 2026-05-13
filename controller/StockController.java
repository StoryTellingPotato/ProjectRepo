package controller;

import authenticator.SessionManager;
import dao.StockInDAO;
import dao.StockOutDAO;
import model.StockIn;
import model.StockOut;
import util.InputValidator;

import java.util.List;

/**
 * FILE: controller/StockController.java
 * ROLE: Business logic for Stock In and Stock Out operations.
 *
 * UI Usage:
 *   StockController sc = new StockController();
 *
 *   // Stock In button:
 *   String result = sc.recordStockIn(productId, quantityField.getText(), remarksField.getText());
 *   if (result.equals("OK")) {
 *       JOptionPane.showMessageDialog(frame, "Stock added successfully!");
 *       refreshProductTable();
 *   } else {
 *       JOptionPane.showMessageDialog(frame, result, "Error", JOptionPane.ERROR_MESSAGE);
 *   }
 */
public class StockController {

    private final StockInDAO  stockInDAO  = new StockInDAO();
    private final StockOutDAO stockOutDAO = new StockOutDAO();

    // ── RECORD STOCK IN ───────────────────────────────────────────────
    /**
     * @return "OK" on success, or a user-friendly error message.
     */
    public String recordStockIn(int productId, String quantityText, String remarks) {

        if (productId <= 0)                             return "Please select a product.";
        if (!InputValidator.isPositiveInt(quantityText)) return "Quantity must be a positive whole number.";

        int userId = getCurrentUserId();
        int qty    = Integer.parseInt(quantityText.trim());

        StockIn record = new StockIn(productId, qty, remarks, userId);
        boolean ok = stockInDAO.recordStockIn(record);

        return ok ? "OK" : "Failed to record stock in. Please try again.";
    }

    // ── RECORD STOCK OUT ──────────────────────────────────────────────
    /**
     * @return "OK" on success, "Insufficient stock." if not enough,
     *         or another error message.
     */
    public String recordStockOut(int productId, String quantityText, String remarks) {

        if (productId <= 0)                             return "Please select a product.";
        if (!InputValidator.isPositiveInt(quantityText)) return "Quantity must be a positive whole number.";

        int userId = getCurrentUserId();
        int qty    = Integer.parseInt(quantityText.trim());

        StockOut record = new StockOut(productId, qty, remarks, userId);
        boolean ok = stockOutDAO.recordStockOut(record);

        // StockOutDAO prints the reason to console; return friendly message here
        return ok ? "OK" : "Insufficient stock or database error. Please check the quantity.";
    }

    // ── GET HISTORY ───────────────────────────────────────────────────
    public List<StockIn>  getAllStockIn()  { return stockInDAO.getAllStockIn(); }
    public List<StockOut> getAllStockOut() { return stockOutDAO.getAllStockOut(); }

    public List<StockIn>  getStockInByProduct(int productId)  {
        return stockInDAO.getStockInByProduct(productId);
    }
    public List<StockOut> getStockOutByProduct(int productId) {
        return stockOutDAO.getStockOutByProduct(productId);
    }

    // ── PRIVATE HELPER ────────────────────────────────────────────────
    private int getCurrentUserId() {
        if (SessionManager.getInstance().isLoggedIn()) {
            return SessionManager.getInstance().getCurrentUser().getId();
        }
        return 0;   // 0 = no user logged in (FK allows NULL via ON DELETE SET NULL)
    }
}
