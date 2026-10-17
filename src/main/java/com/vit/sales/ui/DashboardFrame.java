package com.vit.sales.ui;

import com.vit.sales.data.MasterEntity;
import com.vit.sales.db.DatabaseManager;
import com.vit.sales.security.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** Main authenticated workspace. Orders/payments/reports are added in Stage 4. */
public final class DashboardFrame extends JFrame {
    private final DatabaseManager db;
    private final AuthService.AuthenticatedUser user;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final Map<String, JPanel> cards = new LinkedHashMap<>();
    private final JLabel connectionStatus = new JLabel(" ");

    public DashboardFrame(DatabaseManager db, AuthService.AuthenticatedUser user) {
        super("Sales and Customer Management System");
        this.db = db; this.user = user;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650)); setSize(1180, 760); setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        content.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        content.add(buildHome(), "Home");
        for (MasterEntity entity : MasterEntity.ALL) {
            if (entity.table().equals("Employee") && !isAdmin()) continue;
            MasterCrudPanel panel = new MasterCrudPanel(db, entity, user.role());
            cards.put(entity.title(), panel);
            content.add(panel, entity.title());
        }
        // Stage 4 functional modules.
        cards.put("Orders", new OrdersPanel(db, user.role())); content.add(cards.get("Orders"), "Orders");
        cards.put("Payments", new PaymentsPanel(db)); content.add(cards.get("Payments"), "Payments");
        cards.put("Reports", new ReportsPanel(db)); content.add(cards.get("Reports"), "Reports");
        if (isAdmin()) { cards.put("User Management", new UserManagementPanel(db)); content.add(cards.get("User Management"), "User Management"); }
        add(content, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
        showCard("Home");
    }

    private boolean isAdmin() { return "ADMIN".equalsIgnoreCase(user.role()); }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout()); p.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        p.setBackground(new Color(27, 48, 76));
        JLabel title = new JLabel("SALES & CUSTOMER MANAGEMENT"); title.setForeground(Color.WHITE); title.setFont(title.getFont().deriveFont(Font.BOLD, 17f));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); right.setOpaque(false);
        JLabel who = new JLabel(user.username() + "  |  " + user.role()); who.setForeground(Color.WHITE);
        JButton logout = new JButton("Log out"); logout.addActionListener(e -> logout());
        right.add(who); right.add(logout); p.add(title, BorderLayout.WEST); p.add(right, BorderLayout.EAST); return p;
    }

    private JPanel buildSidebar() {
        JPanel p = new JPanel(); p.setBackground(new Color(239, 243, 248)); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10)); p.setPreferredSize(new Dimension(190, 0));
        addNav(p, "Dashboard", "Home");
        for (MasterEntity entity : MasterEntity.ALL) {
            if (entity.table().equals("Employee") && !isAdmin()) continue;
            addNav(p, entity.title(), entity.title());
        }
        addNav(p, "Orders", "Orders"); addNav(p, "Payments", "Payments"); addNav(p, "Reports", "Reports");
        if (isAdmin()) addNav(p, "User Management", "User Management");
        p.add(Box.createVerticalGlue());
        JLabel role = new JLabel(isAdmin() ? "Administrator access" : "Staff access"); role.setForeground(new Color(80, 90, 100)); role.setBorder(BorderFactory.createEmptyBorder(10, 6, 4, 6)); p.add(role);
        return p;
    }

    private void addNav(JPanel parent, String label, String key) {
        JButton b = new JButton(label); b.setAlignmentX(Component.LEFT_ALIGNMENT); b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        b.setHorizontalAlignment(SwingConstants.LEFT); b.setFocusPainted(false); b.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        b.addActionListener(e -> showCard(key)); parent.add(b); parent.add(Box.createVerticalStrut(4));
    }

    private JPanel buildHome() {
        JPanel p = new JPanel(new BorderLayout(12, 16)); p.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        JPanel intro = new JPanel(new BorderLayout()); intro.setOpaque(false);
        JLabel heading = new JLabel("Dashboard overview"); heading.setFont(heading.getFont().deriveFont(Font.BOLD, 24f));
        JLabel sub = new JLabel("A quick view of customers, products, orders and sales."); sub.setForeground(new Color(90, 100, 110));
        JPanel titles = new JPanel(); titles.setOpaque(false); titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS)); titles.add(heading); titles.add(Box.createVerticalStrut(5)); titles.add(sub); intro.add(titles, BorderLayout.WEST);
        JButton refresh = new JButton("Refresh overview"); refresh.addActionListener(e -> refreshHome()); intro.add(refresh, BorderLayout.EAST); p.add(intro, BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(12, 14)); body.setOpaque(false);
        JPanel metrics = new JPanel(new GridLayout(1, 4, 12, 12)); metrics.setOpaque(false);
        metrics.add(metric("Customers", countQuery("SELECT COUNT(*) FROM Customer")));
        metrics.add(metric("Products", countQuery("SELECT COUNT(*) FROM Product")));
        metrics.add(metric("Orders", countQuery("SELECT COUNT(*) FROM Orders")));
        metrics.add(metric("Recorded payments", moneyQuery("SELECT COALESCE(SUM(p.amount),0) FROM Payment p JOIN Orders o ON o.order_id=p.order_id WHERE o.order_status <> 'Cancelled'")));
        body.add(metrics, BorderLayout.NORTH);
        JPanel recent = new JPanel(new BorderLayout(6, 6)); recent.setBorder(BorderFactory.createTitledBorder("Recent orders"));
        JTable recentTable = new JTable(); recentTable.setFillsViewportHeight(true); recentTable.setAutoCreateRowSorter(true);
        JButton refreshRecent = new JButton("Refresh table"); refreshRecent.addActionListener(e -> loadRecentOrders(recentTable));
        recent.add(new JScrollPane(recentTable), BorderLayout.CENTER); recent.add(refreshRecent, BorderLayout.SOUTH);
        body.add(recent, BorderLayout.CENTER);
        JPanel note = new JPanel(new BorderLayout()); note.setOpaque(false);
        JLabel lowStock = new JLabel("Low-stock products (quantity ≤ 10)"); lowStock.setFont(lowStock.getFont().deriveFont(Font.BOLD, 15f));
        JTable lowStockTable = new JTable(); lowStockTable.setFillsViewportHeight(true);
        JPanel lowPanel = new JPanel(new BorderLayout(6, 6)); lowPanel.setBorder(BorderFactory.createTitledBorder("Inventory watch"));
        lowPanel.add(lowStock, BorderLayout.NORTH); lowPanel.add(new JScrollPane(lowStockTable), BorderLayout.CENTER);
        body.add(lowPanel, BorderLayout.SOUTH); p.add(body, BorderLayout.CENTER);
        p.putClientProperty("recentTable", recentTable); p.putClientProperty("lowStockTable", lowStockTable); p.putClientProperty("metrics", metrics);
        refreshHomeComponents(p);
        return p;
    }

    private JPanel metric(String label, String value) {
        JPanel p = new JPanel(new BorderLayout(5, 8)); p.setBackground(Color.WHITE); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 226, 234)), BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        JLabel name = new JLabel(label); name.setForeground(new Color(90, 100, 110)); JLabel val = new JLabel(value); val.setFont(val.getFont().deriveFont(Font.BOLD, 23f)); val.setForeground(new Color(27, 48, 76));
        p.add(name, BorderLayout.NORTH); p.add(val, BorderLayout.CENTER); return p;
    }

    private void refreshHome() {
        JPanel home = (JPanel) cards.getOrDefault("__home_unused", null);
        Component[] children = content.getComponents();
        for (Component child : children) if (child instanceof JPanel panel && panel.getComponentCount() > 0) {
            // Find the home card by its first north component label.
            if (panel.getClientProperty("recentTable") instanceof JTable) refreshHomeComponents(panel);
        }
    }

    private void refreshHomeComponents(JPanel home) {
        Object recent = home.getClientProperty("recentTable"); if (recent instanceof JTable t) loadRecentOrders(t);
        Object low = home.getClientProperty("lowStockTable"); if (low instanceof JTable t) loadLowStock(t);
        Object m = home.getClientProperty("metrics");
        if (m instanceof JPanel metrics) {
            String[] values = {countQuery("SELECT COUNT(*) FROM Customer"), countQuery("SELECT COUNT(*) FROM Product"), countQuery("SELECT COUNT(*) FROM Orders"), moneyQuery("SELECT COALESCE(SUM(p.amount),0) FROM Payment p JOIN Orders o ON o.order_id=p.order_id WHERE o.order_status <> 'Cancelled'")};
            for (int i = 0; i < Math.min(metrics.getComponentCount(), values.length); i++) {
                JPanel card = (JPanel) metrics.getComponent(i); ((JLabel) card.getComponent(1)).setText(values[i]);
            }
        }
    }

    private String countQuery(String sql) {
        try (Connection c = db.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) { return rs.next() ? rs.getString(1) : "0"; }
        catch (SQLException ex) { connectionStatus.setText("Database unavailable: " + ex.getMessage()); return "—"; }
    }
    private String moneyQuery(String sql) {
        try (Connection c = db.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) { return rs.next() ? String.format("₹%,.2f", rs.getBigDecimal(1)) : "₹0.00"; }
        catch (SQLException ex) { connectionStatus.setText("Database unavailable: " + ex.getMessage()); return "—"; }
    }

    private void loadRecentOrders(JTable target) {
        String sql = "SELECT o.order_id, c.customer_name, o.order_date, o.order_status, COALESCE(SUM(od.quantity * od.unit_price),0) AS order_total " +
                "FROM Orders o JOIN Customer c ON c.customer_id=o.customer_id LEFT JOIN Order_Details od ON od.order_id=o.order_id " +
                "GROUP BY o.order_id,c.customer_name,o.order_date,o.order_status ORDER BY o.order_date DESC,o.order_id DESC LIMIT 10";
        loadTable(target, sql);
    }
    private void loadLowStock(JTable target) {
        loadTable(target, "SELECT product_id, product_name, stock_quantity FROM Product WHERE stock_quantity <= 10 ORDER BY stock_quantity, product_name");
    }
    private void loadTable(JTable target, String sql) {
        DefaultTableModel tm = new DefaultTableModel();
        try (Connection c = db.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData(); for (int i = 1; i <= md.getColumnCount(); i++) tm.addColumn(md.getColumnLabel(i));
            while (rs.next()) { Object[] row = new Object[md.getColumnCount()]; for (int i = 1; i <= row.length; i++) row[i - 1] = rs.getObject(i); tm.addRow(row); }
        } catch (SQLException ex) { connectionStatus.setText("Dashboard query unavailable: " + ex.getMessage()); }
        target.setModel(tm);
    }

    private JPanel placeholder(String title) {
        JPanel p = new JPanel(new GridBagLayout()); JPanel inner = new JPanel(); inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        JLabel heading = new JLabel(title); heading.setFont(heading.getFont().deriveFont(Font.BOLD, 24f)); heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel text = new JLabel("This module is scheduled for Stage 4."); text.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(heading); inner.add(Box.createVerticalStrut(10)); inner.add(text); p.add(inner); return p;
    }

    private JPanel buildStatusBar() {
        JPanel p = new JPanel(new BorderLayout()); p.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        connectionStatus.setText("Connected user: " + user.username() + " | Role: " + user.role()); p.add(connectionStatus, BorderLayout.WEST);
        JLabel dbLabel = new JLabel("MySQL · JDBC"); dbLabel.setForeground(new Color(90, 100, 110)); p.add(dbLabel, BorderLayout.EAST); return p;
    }

    private void showCard(String key) { cardLayout.show(content, key); }
    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this, "Log out of the application?", "Confirm logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dispose(); SwingUtilities.invokeLater(() -> LoginFrame.showLogin(db));
        }
    }
}
