package com.vit.sales.ui;

import com.vit.sales.data.MasterEntity;
import com.vit.sales.db.DatabaseManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Reusable CRUD panel. SQL identifiers come only from fixed MasterEntity metadata. */
public final class MasterCrudPanel extends JPanel {
    private final DatabaseManager db;
    private final MasterEntity entity;
    private final boolean canEdit;
    private final JTable table = new JTable();
    private final DefaultTableModel model = new DefaultTableModel();
    private final JLabel status = new JLabel(" ");
    private final JTextField search = new JTextField(18);
    private List<String> columns = new ArrayList<>();
    private List<Object> selectedRow = List.of();

    public MasterCrudPanel(DatabaseManager db, MasterEntity entity, String role) {
        super(new BorderLayout(10, 10));
        this.db = db;
        this.entity = entity;
        this.canEdit = "ADMIN".equalsIgnoreCase(role)
                || (entity.staffCanEdit() && entity.table().equals("Customer"));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        table.setModel(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) captureSelectedRow();
        });

        JPanel top = new JPanel(new BorderLayout(8, 8));
        JLabel heading = new JLabel(entity.title());
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 20f));
        top.add(heading, BorderLayout.WEST);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchPanel.add(new JLabel("Search")); searchPanel.add(search);
        JButton find = new JButton("Search"); find.addActionListener(e -> loadRows());
        JButton reset = new JButton("Refresh"); reset.addActionListener(e -> { search.setText(""); loadRows(); });
        searchPanel.add(find); searchPanel.add(reset); top.add(searchPanel, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Add"); add.addActionListener(e -> editRow(false));
        JButton edit = new JButton("Edit selected"); edit.addActionListener(e -> editRow(true));
        JButton delete = new JButton("Delete selected"); delete.addActionListener(e -> deleteRow());
        JButton refresh = new JButton("Refresh"); refresh.addActionListener(e -> loadRows());
        if (canEdit) { actions.add(add); actions.add(edit); actions.add(delete); }
        actions.add(refresh);
        if (!canEdit) actions.add(new JLabel("Read-only access"));
        bottom.add(actions, BorderLayout.NORTH);
        status.setForeground(new Color(80, 80, 80));
        bottom.add(status, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);
        loadRows();
    }

    private void loadRows() {
        String sql = "SELECT * FROM " + entity.table();
        String term = search.getText().trim();
        try (Connection c = db.getConnection()) {
            if (!term.isEmpty()) {
                DatabaseMetaData md = c.getMetaData();
                List<String> textCols = new ArrayList<>();
                try (ResultSet rs = md.getColumns(c.getCatalog(), null, entity.table(), "%")) {
                    while (rs.next()) {
                        String type = rs.getString("TYPE_NAME").toUpperCase();
                        if (type.contains("CHAR") || type.contains("TEXT") || type.contains("DATE")) textCols.add(rs.getString("COLUMN_NAME"));
                    }
                }
                if (!textCols.isEmpty()) {
                    StringBuilder where = new StringBuilder(" WHERE ");
                    for (int i = 0; i < textCols.size(); i++) { if (i > 0) where.append(" OR "); where.append(textCols.get(i)).append(" LIKE ?"); }
                    sql += where;
                }
            }
            sql += " ORDER BY 1 DESC";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                if (!term.isEmpty()) {
                    int p = 1;
                    DatabaseMetaData md = c.getMetaData();
                    try (ResultSet rs = md.getColumns(c.getCatalog(), null, entity.table(), "%")) {
                        while (rs.next()) {
                            String type = rs.getString("TYPE_NAME").toUpperCase();
                            if (type.contains("CHAR") || type.contains("TEXT") || type.contains("DATE")) ps.setString(p++, "%" + term + "%");
                        }
                    }
                }
                try (ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData rm = rs.getMetaData();
                    columns = new ArrayList<>(); model.setRowCount(0); model.setColumnCount(0);
                    for (int i = 1; i <= rm.getColumnCount(); i++) { columns.add(rm.getColumnName(i)); model.addColumn(rm.getColumnLabel(i)); }
                    while (rs.next()) {
                        Object[] row = new Object[rm.getColumnCount()];
                        for (int i = 1; i <= row.length; i++) row[i - 1] = rs.getObject(i);
                        model.addRow(row);
                    }
                    status.setText(model.getRowCount() + " record(s) loaded.");
                }
            }
        } catch (SQLException ex) { showDbError("Could not load " + entity.title().toLowerCase(), ex); }
    }

    private void captureSelectedRow() {
        int view = table.getSelectedRow();
        if (view < 0) { selectedRow = List.of(); return; }
        int row = table.convertRowIndexToModel(view);
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < model.getColumnCount(); i++) values.add(model.getValueAt(row, i));
        selectedRow = values;
    }

    private void editRow(boolean editing) {
        if (!canEdit) return;
        if (editing && (selectedRow.isEmpty() || columns.isEmpty())) {
            JOptionPane.showMessageDialog(this, "Select a record first."); return;
        }
        JPanel form = new JPanel(new GridLayout(entity.fields().size(), 2, 8, 7));
        List<JTextField> inputs = new ArrayList<>();
        for (MasterEntity.Field f : entity.fields()) {
            form.add(new JLabel(f.label() + (f.required() ? " *" : "")));
            JTextField field = new JTextField(24);
            if (editing) {
                int idx = columns.indexOf(f.name());
                if (idx >= 0 && selectedRow.get(idx) != null) field.setText(selectedRow.get(idx).toString());
            }
            inputs.add(field); form.add(field);
        }
        int result = JOptionPane.showConfirmDialog(this, form, (editing ? "Edit " : "Add ") + entity.title(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        try (Connection c = db.getConnection()) {
            if (editing) update(c, inputs); else insert(c, inputs);
            loadRows();
            status.setText("Saved successfully.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Check input", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) { showDbError("Could not save record", ex); }
    }

    private List<Object> readValues(List<JTextField> inputs) {
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < entity.fields().size(); i++) {
            MasterEntity.Field f = entity.fields().get(i); String raw = inputs.get(i).getText().trim();
            if (raw.isEmpty()) {
                if (f.required()) throw new IllegalArgumentException(f.label() + " is required.");
                values.add(null); continue;
            }
            try {
                values.add(switch (f.kind()) {
                    case TEXT -> raw;
                    case INTEGER -> Integer.valueOf(raw);
                    case DECIMAL -> new BigDecimal(raw);
                    case DATE -> Date.valueOf(LocalDate.parse(raw));
                });
            } catch (Exception ex) { throw new IllegalArgumentException("Invalid value for " + f.label() + ". Check the expected format."); }
        }
        return values;
    }

    private void insert(Connection c, List<JTextField> inputs) throws SQLException {
        List<Object> values = readValues(inputs);
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(entity.table()).append(" (");
        for (int i = 0; i < entity.fields().size(); i++) { if (i > 0) sql.append(','); sql.append(entity.fields().get(i).name()); }
        sql.append(") VALUES (").append("?,".repeat(entity.fields().size())); sql.setLength(sql.length() - 1); sql.append(')');
        try (PreparedStatement ps = c.prepareStatement(sql.toString())) { bind(ps, values); ps.executeUpdate(); }
    }

    private void update(Connection c, List<JTextField> inputs) throws SQLException {
        List<Object> values = readValues(inputs);
        String idColumn = columns.get(0); Object id = selectedRow.get(0);
        StringBuilder sql = new StringBuilder("UPDATE ").append(entity.table()).append(" SET ");
        for (int i = 0; i < entity.fields().size(); i++) { if (i > 0) sql.append(','); sql.append(entity.fields().get(i).name()).append("=?"); }
        sql.append(" WHERE ").append(idColumn).append("=?");
        try (PreparedStatement ps = c.prepareStatement(sql.toString())) { bind(ps, values); ps.setObject(values.size() + 1, id); ps.executeUpdate(); }
    }

    private void deleteRow() {
        if (!canEdit) return;
        if (selectedRow.isEmpty() || columns.isEmpty()) { JOptionPane.showMessageDialog(this, "Select a record first."); return; }
        int ok = JOptionPane.showConfirmDialog(this, "Delete the selected record from " + entity.title() + "? Related records may prevent deletion.", "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        String sql = "DELETE FROM " + entity.table() + " WHERE " + columns.get(0) + "=?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, selectedRow.get(0)); ps.executeUpdate(); loadRows(); status.setText("Delete completed.");
        } catch (SQLException ex) { showDbError("Delete was blocked or failed", ex); }
    }

    private static void bind(PreparedStatement ps, List<Object> values) throws SQLException {
        for (int i = 0; i < values.size(); i++) { Object value = values.get(i); if (value == null) ps.setNull(i + 1, Types.NULL); else ps.setObject(i + 1, value); }
    }

    private void showDbError(String message, SQLException ex) {
        status.setText(message + ". Check constraints and database connection.");
        JOptionPane.showMessageDialog(this, message + ":\n" + ex.getMessage(), "Database operation failed", JOptionPane.ERROR_MESSAGE);
    }
}
