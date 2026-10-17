package com.vit.sales.ui;

import com.vit.sales.db.DatabaseManager;
import com.vit.sales.security.AuthService;
import com.vit.sales.security.UserDao;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;

/** Minimal working login UI. Dashboard and business modules are Stage 3 onward. */
public final class LoginFrame extends JFrame {
    private final JTextField username = new JTextField(20);
    private final JPasswordField password = new JPasswordField(20);
    private final JLabel status = new JLabel(" ");
    private final AuthService auth;
    private final DatabaseManager databaseManager;

    private LoginFrame(DatabaseManager databaseManager, AuthService auth) {
        super("Sales and Customer Management System | Sign in");
        this.databaseManager = databaseManager;
        this.auth = auth;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(440, 280); setLocationRelativeTo(null);
        JPanel root = new JPanel(new GridBagLayout()); root.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(7, 5, 7, 5); g.fill = GridBagConstraints.HORIZONTAL;
        JLabel heading = new JLabel("Sales & Customer Management", SwingConstants.CENTER); heading.setFont(heading.getFont().deriveFont(Font.BOLD, 18f));
        g.gridx=0; g.gridy=0; g.gridwidth=2; root.add(heading,g);
        g.gridwidth=1; g.gridy++; g.gridx=0; root.add(new JLabel("Username"),g); g.gridx=1; root.add(username,g);
        g.gridy++; g.gridx=0; root.add(new JLabel("Password"),g); g.gridx=1; root.add(password,g);
        JCheckBox show = new JCheckBox("Show password"); show.addActionListener(e -> password.setEchoChar(show.isSelected() ? (char)0 : '\u2022'));
        g.gridy++; g.gridx=1; root.add(show,g);
        JButton signIn = new JButton("Sign in"); signIn.addActionListener(e -> attemptLogin());
        g.gridy++; g.gridx=0; root.add(signIn,g); g.gridx=1; root.add(status,g);
        status.setForeground(new Color(160, 30, 30));
        getRootPane().setDefaultButton(signIn); setContentPane(root);
    }

    private void attemptLogin() {
        char[] secret = password.getPassword();
        try {
            AuthService.AuthenticatedUser user = auth.login(username.getText(), secret);
            status.setText(" ");
            password.setText("");
            dispose();
            SwingUtilities.invokeLater(() -> new DashboardFrame(databaseManager, user).setVisible(true));
        } catch (AuthService.AuthenticationException ex) {
            status.setText("Login failed"); password.setText("");
            JOptionPane.showMessageDialog(this, AuthService.GENERIC_FAILURE, "Unable to sign in", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            status.setText("Database unavailable"); password.setText("");
            JOptionPane.showMessageDialog(this, "Could not connect to the database. Check MySQL and config/db.properties.", "Database error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void showLogin(DatabaseManager database) {
        AuthService auth = new AuthService(new UserDao(database));
        SwingUtilities.invokeLater(() -> new LoginFrame(database, auth).setVisible(true));
    }

    public static void main(String[] args) {
        try {
            DatabaseManager database = DatabaseManager.fromLocalConfig();
            showLogin(database);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Configuration required", JOptionPane.ERROR_MESSAGE);
        }
    }
}
