package controller;

import authenticator.SessionManager;
import dao.UserDAO;
import model.User;
import util.InputValidator;

/**
 * FILE: controller/AuthController.java
 * ROLE: Handles login and logout logic between the UI and the DAO.
 *
 * OOP — Abstraction: the UI calls login() and doesn't know about
 * SQL, SessionManager, or validation details. It just gets a result.
 *
 * UI Usage:
 *   AuthController auth = new AuthController();
 *
 *   // In the Login button ActionListener:
 *   String result = auth.login(usernameField.getText(),
 *                              new String(passwordField.getPassword()));
 *   if (result.equals("OK")) {
 *       // open main window
 *   } else {
 *       JOptionPane.showMessageDialog(frame, result, "Login Failed",
 *           JOptionPane.WARNING_MESSAGE);
 *   }
 *
 *   // In Logout button:
 *   auth.logout();
 */
public class AuthController {

    private final UserDAO userDAO = new UserDAO();

    /**
     * Validates inputs, queries the database, and starts a session.
     *
     * @return "OK" on success, or a user-friendly error message string.
     */
    public String login(String username, String password) {

        // Input validation
        if (InputValidator.isEmpty(username)) {
            return "Please enter your username.";
        }
        if (InputValidator.isEmpty(password)) {
            return "Please enter your password.";
        }

        // Database lookup
        User user = userDAO.login(username, password);

        if (user == null) {
            return "Invalid username or password. Please try again.";
        }
        if (!user.isActive()) {
            return "Your account has been deactivated. Please contact the Admin.";
        }

        // Start session
        SessionManager.getInstance().setCurrentUser(user);
        System.out.println("[Auth] Login successful: " + user.getUsername()
                + " (" + user.getRole() + ")");
        return "OK";
    }

    /**
     * Ends the current session and clears stored user data.
     */
    public void logout() {
        SessionManager.getInstance().logout();
    }

    /**
     * Returns the currently logged-in user.
     * Returns null if no one is logged in.
     */
    public User getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser();
    }

    /**
     * Returns true if the current user is an Admin.
     * UI: use this to show or hide admin-only panels/buttons.
     *
     *   if (auth.isAdmin()) {
     *       manageUsersButton.setVisible(true);
     *   }
     */
    public boolean isAdmin() {
        return SessionManager.getInstance().isAdmin();
    }
}
