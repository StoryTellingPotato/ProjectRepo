package main;

import controller.ProductController;
import model.Product;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SimpleUI extends JFrame {
    private ProductController controller;
    private JTable table;
    private JTextField nameField, priceField, stockField, unitField, searchField;
    private JComboBox<String> categoryCombo, supplierCombo;
    private DefaultTableModel tableModel;
    
    public SimpleUI() {
        controller = new ProductController();
        setTitle("Coffee Shop Inventory");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create table (7 columns)
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Category", "Supplier", "Price", "Stock", "Unit"}, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 4, 5, 5));
        
        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);
        
        inputPanel.add(new JLabel("Category ID:"));
        categoryCombo = new JComboBox<>(new String[]{"1", "2", "3", "4", "5"});
        categoryCombo.setToolTipText("1=Hot Drinks, 2=Cold Drinks, 3=Pastries, 4=Syrups, 5=Supplies");
        inputPanel.add(categoryCombo);
        
        inputPanel.add(new JLabel("Supplier ID:"));
        supplierCombo = new JComboBox<>(new String[]{"1", "2", "3"});
        supplierCombo.setToolTipText("1=Bean Brothers, 2=Sweet Supply, 3=Pack and Go");
        inputPanel.add(supplierCombo);
        
        inputPanel.add(new JLabel("Price:"));
        priceField = new JTextField();
        inputPanel.add(priceField);
        
        inputPanel.add(new JLabel("Stock:"));
        stockField = new JTextField();
        inputPanel.add(stockField);
        
        inputPanel.add(new JLabel("Unit:"));
        unitField = new JTextField("pcs");
        inputPanel.add(unitField);
        
        JButton addBtn = new JButton("Add Product");
        addBtn.addActionListener(e -> addProduct());
        inputPanel.add(addBtn);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshTable());
        inputPanel.add(refreshBtn);
        
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteProduct());
        inputPanel.add(deleteBtn);
        
        add(inputPanel, BorderLayout.NORTH);
        
        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchField = new JTextField();
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                searchProducts();
            }
        });
        searchPanel.add(searchField, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.SOUTH);
        
        // Load data
        refreshTable();
        setVisible(true);
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Product> products = controller.getAllProducts();
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                p.getId(), 
                p.getName(), 
                p.getCategoryId(),
                p.getSupplierId(), 
                p.getPrice(), 
                p.getStock(), 
                p.getUnit()
            });
        }
    }
    
    private void addProduct() {
        try {
            // Get values from UI
            String name = nameField.getText();
            int categoryId = Integer.parseInt(categoryCombo.getSelectedItem().toString());
            int supplierId = Integer.parseInt(supplierCombo.getSelectedItem().toString());
            String priceText = priceField.getText();
            String stockText = stockField.getText();
            String unit = unitField.getText();
            
            // Call controller - returns String message
            String result = controller.addProduct(name, categoryId, supplierId, priceText, stockText, unit);
            
            if (result.equals("OK")) {
                refreshTable();
                clearFields();
                JOptionPane.showMessageDialog(this, "Product added successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + result);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please select valid category and supplier IDs!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
    private void deleteProduct() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete!");
            return;
        }
        
        int id = (int) table.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Delete product #" + id + "?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Call controller - returns String message
            String result = controller.deleteProduct(id);
            
            if (result.equals("OK")) {
                refreshTable();
                JOptionPane.showMessageDialog(this, "Product deleted!");
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + result);
            }
        }
    }
    
    private void searchProducts() {
        String keyword = searchField.getText().trim();
        List<Product> results = controller.searchProducts(keyword);
        
        tableModel.setRowCount(0);
        for (Product p : results) {
            tableModel.addRow(new Object[]{
                p.getId(), p.getName(), p.getCategoryId(), 
                p.getSupplierId(), p.getPrice(), p.getStock(), p.getUnit()
            });
        }
    }
    
    private void clearFields() {
        nameField.setText("");
        priceField.setText("");
        stockField.setText("");
        unitField.setText("pcs");
        categoryCombo.setSelectedIndex(0);
        supplierCombo.setSelectedIndex(0);
        table.clearSelection();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimpleUI());
    }
}