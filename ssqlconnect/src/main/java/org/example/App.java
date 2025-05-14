import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;


public class App extends JFrame {

    private static final String URL = "";
    private static final String USER = "";
    private static final String PASSWORD = "";

    private JLabel statusLabel;
    private JComboBox<String> comboTablesBrowse;
    private DefaultTableModel browseTableModel;
    private JTable browseTable;

    private JComboBox<String> comboTablesAdmin;
    private JPanel insertFieldsPanel;
    private JButton btnInsert, btnUpdate, btnDelete;
    private List<JTextField> columnFields;
    private List<String> columnNames;
    private List<Integer> columnTypes;

    public App() {
        super("XYZ Company DB - CRUD Viewer");
        initUI();
    }

    private void initUI() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Browse", createBrowsePanel());
        tabbedPane.addTab("Admin", createAdminPanel());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(statusLabel, BorderLayout.SOUTH);

        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loadTableNames();
    }

    private JPanel createBrowsePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboTablesBrowse = new JComboBox<>();
        JButton btnLoadTable = new JButton("Load Table");
        btnLoadTable.addActionListener(e -> {
            String tableName = (String) comboTablesBrowse.getSelectedItem();
            if (tableName != null) loadTableData(tableName);
        });
        topPanel.add(new JLabel("Select Table:"));
        topPanel.add(comboTablesBrowse);
        topPanel.add(btnLoadTable);

        browseTableModel = new DefaultTableModel();
        browseTable = new JTable(browseTableModel);
        JScrollPane scrollPane = new JScrollPane(browseTable);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboTablesAdmin = new JComboBox<>();
        comboTablesAdmin.addActionListener(e -> {
            String tableName = (String) comboTablesAdmin.getSelectedItem();
            if (tableName != null) buildInsertFormForTable(tableName);
        });
        topPanel.add(new JLabel("Select Table:"));
        topPanel.add(comboTablesAdmin);

        insertFieldsPanel = new JPanel(new GridBagLayout());
        JScrollPane centerScroll = new JScrollPane(insertFieldsPanel);

        btnInsert = new JButton("Insert");
        btnInsert.addActionListener(e -> performInsert());
        btnUpdate = new JButton("Update");
        btnUpdate.addActionListener(e -> performUpdate());
        btnDelete = new JButton("Delete");
        btnDelete.addActionListener(e -> performDelete());

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnInsert);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(centerScroll, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadTableNames() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SHOW TABLES");
             ResultSet rs = stmt.executeQuery()) {
            comboTablesBrowse.removeAllItems();
            comboTablesAdmin.removeAllItems();
            while (rs.next()) {
                String tableName = rs.getString(1);
                comboTablesBrowse.addItem(tableName);
                comboTablesAdmin.addItem(tableName);
            }
            statusLabel.setText("Tables loaded.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            statusLabel.setText("Error loading table names.");
        }
    }

    private void loadTableData(String tableName) {
        browseTableModel.setRowCount(0);
        browseTableModel.setColumnCount(0);
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + " LIMIT 500")) {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            for (int i = 1; i <= cols; i++) browseTableModel.addColumn(md.getColumnLabel(i));
            while (rs.next()) {
                Object[] row = new Object[cols];
                for (int i = 0; i < cols; i++) row[i] = rs.getObject(i + 1);
                browseTableModel.addRow(row);
            }
            statusLabel.setText("Loaded " + browseTableModel.getRowCount() + " rows from " + tableName);
        } catch (SQLException ex) {
            ex.printStackTrace();
            statusLabel.setText("Load failed.");
        }
    }

    private void buildInsertFormForTable(String tableName) {
        insertFieldsPanel.removeAll();
        columnFields = new ArrayList<>();
        columnNames = new ArrayList<>();
        columnTypes = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + " LIMIT 1")) {
            ResultSetMetaData md = rs.getMetaData();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            for (int i = 1; i <= md.getColumnCount(); i++) {
                String col = md.getColumnLabel(i);
                int type = md.getColumnType(i);
                columnNames.add(col);
                columnTypes.add(type);

                gbc.gridx = 0; gbc.gridy = i - 1;
                insertFieldsPanel.add(new JLabel(col + ":"), gbc);

                gbc.gridx = 1;
                JTextField field = new JTextField(20);
                insertFieldsPanel.add(field, gbc);

                columnFields.add(field);
            }
            insertFieldsPanel.revalidate();
            insertFieldsPanel.repaint();
            statusLabel.setText("Form built for: " + tableName);
        } catch (SQLException ex) {
            ex.printStackTrace();
            statusLabel.setText("Form build failed.");
        }
    }

    private void performInsert() {
        String table = (String) comboTablesAdmin.getSelectedItem();
        if (table == null) return;

        StringBuilder cols = new StringBuilder();
        StringBuilder vals = new StringBuilder();
        for (int i = 0; i < columnNames.size(); i++) {
            cols.append(columnNames.get(i)).append(i < columnNames.size() - 1 ? ", " : "");
            vals.append("?").append(i < columnNames.size() - 1 ? ", " : "");
        }
        String sql = "INSERT INTO " + table + " (" + cols + ") VALUES (" + vals + ")";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < columnFields.size(); i++) {
                String val = columnFields.get(i).getText().trim();
                if (val.isEmpty()) pstmt.setNull(i + 1, columnTypes.get(i));
                else setPreparedStatementValue(pstmt, i + 1, columnTypes.get(i), val);
            }
            pstmt.executeUpdate();
            statusLabel.setText("Row inserted.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            statusLabel.setText("Insert failed.");
        }
    }

    private void performUpdate() {
        String table = (String) comboTablesAdmin.getSelectedItem();
        if (table == null) return;
        String pk = columnNames.get(0);
        String pkVal = columnFields.get(0).getText().trim();
        if (pkVal.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primary key is required for update.");
            return;
        }

        StringBuilder sets = new StringBuilder();
        for (int i = 1; i < columnNames.size(); i++)
            sets.append(columnNames.get(i)).append(" = ?").append(i < columnNames.size() - 1 ? ", " : "");

        String sql = "UPDATE " + table + " SET " + sets + " WHERE " + pk + " = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 1; i < columnFields.size(); i++) {
                String val = columnFields.get(i).getText().trim();
                if (val.isEmpty()) pstmt.setNull(i, columnTypes.get(i));
                else setPreparedStatementValue(pstmt, i, columnTypes.get(i), val);
            }
            setPreparedStatementValue(pstmt, columnFields.size(), columnTypes.get(0), pkVal);
            pstmt.executeUpdate();
            statusLabel.setText("Row updated.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            statusLabel.setText("Update failed.");
        }
    }

    private void performDelete() {
        String table = (String) comboTablesAdmin.getSelectedItem();
        if (table == null) return;
        String pk = columnNames.get(0);
        String pkVal = columnFields.get(0).getText().trim();
        if (pkVal.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primary key is required for delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this row?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM " + table + " WHERE " + pk + " = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setPreparedStatementValue(pstmt, 1, columnTypes.get(0), pkVal);
            pstmt.executeUpdate();
            statusLabel.setText("Row deleted.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            statusLabel.setText("Delete failed.");
        }
    }

    private void setPreparedStatementValue(PreparedStatement pstmt, int index, int sqlType, String val) throws SQLException {
        switch (sqlType) {
            case Types.INTEGER: case Types.SMALLINT: case Types.TINYINT: case Types.BIGINT:
                pstmt.setLong(index, Long.parseLong(val)); break;
            case Types.FLOAT: case Types.DOUBLE: case Types.DECIMAL: case Types.NUMERIC:
                pstmt.setDouble(index, Double.parseDouble(val)); break;
            case Types.DATE: case Types.TIMESTAMP:
                pstmt.setString(index, val); break;
            default:
                pstmt.setString(index, val); break;
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception ignored) {}
        try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "MySQL Driver not found."); return;
        }
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}
