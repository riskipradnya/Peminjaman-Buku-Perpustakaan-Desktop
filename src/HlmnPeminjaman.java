import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class HlmnPeminjaman extends JFrame {
    private JTextField txtNamaPeminjam;
    private JComboBox<String> comboBuku;
    private JSpinner datePinjam, dateKembali;
    private JButton btnSimpan, btnEdit, btnDelete, btnBatal;
    private JTable tablePeminjaman;
    private DefaultTableModel tableModel;

    // Koneksi Database
    private Connection connectDB() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/db_kasirbuku", "root", "");
    }

    public HlmnPeminjaman() {
        setTitle("Halaman Peminjaman Buku");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));  // Menggunakan BorderLayout dengan sedikit jarak antar komponen
    
        // Membuat Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Navigasi");
        JMenuItem menuItemBookPage = new JMenuItem("Halaman Buku");
        JMenuItem menuItemLoanPage = new JMenuItem("Halaman Peminjaman");
        JMenuItem menuItemReturnPage = new JMenuItem("Halaman Pengembalian");
        
        // Menambahkan action listener untuk menu item
        menuItemBookPage.addActionListener(e -> navigateToBookPage());
        menuItemLoanPage.addActionListener(e -> JOptionPane.showMessageDialog(this, "Anda sudah berada di Halaman Peminjaman"));
        menuItemReturnPage.addActionListener(e -> navigateToReturnPage());
        
        menu.add(menuItemBookPage);
        menu.add(menuItemLoanPage);
        menu.add(menuItemReturnPage);
        menuBar.add(menu);
        setJMenuBar(menuBar);
    
        // Panel Input
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Form Peminjaman Buku"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
    
        // Nama Peminjam
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Nama Peminjam:"), gbc);
        txtNamaPeminjam = new JTextField();
        txtNamaPeminjam.setPreferredSize(new Dimension(650, 25));
        gbc.gridx = 1;
        inputPanel.add(txtNamaPeminjam, gbc);

        // Pilih Buku
        gbc.gridy = 1;
        gbc.gridx = 0;
        inputPanel.add(new JLabel("Pilih Buku:"), gbc);
        comboBuku = new JComboBox<>();
        gbc.gridx = 1;
        inputPanel.add(comboBuku, gbc);
    
        // Tanggal Pinjam
        gbc.gridy = 2;
        gbc.gridx = 0;
        inputPanel.add(new JLabel("Tanggal Pinjam:"), gbc);
        datePinjam = new JSpinner(new SpinnerDateModel());
        gbc.gridx = 1;
        inputPanel.add(datePinjam, gbc);
    
        // Tanggal Kembali
        gbc.gridy = 3;
        gbc.gridx = 0;
        inputPanel.add(new JLabel("Tanggal Kembali:"), gbc);
        dateKembali = new JSpinner(new SpinnerDateModel());
        gbc.gridx = 1;
        inputPanel.add(dateKembali, gbc);
    
        // Tombol Simpan, Edit, Hapus, Batal
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel();  // Panel untuk tombol
        btnSimpan = new JButton("Simpan");
        btnEdit = new JButton("Edit");
        btnDelete = new JButton("Hapus");
        btnBatal = new JButton("Batal");  // Tombol Batal
        buttonPanel.add(btnSimpan);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnBatal);  // Menambahkan tombol batal
        inputPanel.add(buttonPanel, gbc);
    
        // Tabel Peminjaman
        tableModel = new DefaultTableModel(new String[]{"ID", "Nama Peminjam", "Buku", "Tanggal Pinjam", "Tanggal Kembali"}, 0);
        tablePeminjaman = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(tablePeminjaman);
    
        // Menggunakan BorderLayout untuk form dan tabel
        add(inputPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
    
        // Listener
        btnSimpan.addActionListener(e -> savePeminjaman());
        btnEdit.addActionListener(e -> editPeminjaman());
        btnDelete.addActionListener(e -> deletePeminjaman());
        btnBatal.addActionListener(e -> cancelAction());  // Menambahkan action untuk tombol batal

        loadBuku();
        loadPeminjaman();
    }

    private void navigateToBookPage() {
        JOptionPane.showMessageDialog(this, "Navigasi ke Halaman Buku");
        new BookPage().setVisible(true);
        this.dispose();
    }

    private void navigateToReturnPage() {
        JOptionPane.showMessageDialog(this, "Navigasi ke Halaman Pengembalian");
        new HlmnPengembalian().setVisible(true);
        this.dispose();
    }

    private void loadBuku() {
        try (Connection conn = connectDB()) {
            String query = "SELECT judul FROM tbbuku";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            comboBuku.removeAllItems();
            while (rs.next()) {
                comboBuku.addItem(rs.getString("judul"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPeminjaman() {
        try (Connection conn = connectDB()) {
            String query = "SELECT * FROM tbpeminjaman";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            tableModel.setRowCount(0);
            while (rs.next()) {
                int id = rs.getInt("id");
                String namaPeminjam = rs.getString("namaPeminjam");
                String buku = rs.getString("judulBuku");
                Date tanggalPinjam = rs.getDate("tanggalPinjam");
                Date tanggalKembali = rs.getDate("tanggalKembali");

                tableModel.addRow(new Object[]{id, namaPeminjam, buku, tanggalPinjam, tanggalKembali});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading peminjaman data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void savePeminjaman() {
        String namaPeminjam = txtNamaPeminjam.getText();
        String buku = (String) comboBuku.getSelectedItem();
        java.util.Date pinjamDate = (java.util.Date) datePinjam.getValue();
        java.util.Date kembaliDate = (java.util.Date) dateKembali.getValue();
    
        if (namaPeminjam.isEmpty() || buku == null || pinjamDate == null || kembaliDate == null) {
            JOptionPane.showMessageDialog(this, "Isi semua field!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        java.util.Calendar pinjamCal = java.util.Calendar.getInstance();
        pinjamCal.setTime(pinjamDate);
        pinjamCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        pinjamCal.set(java.util.Calendar.MINUTE, 0);
        pinjamCal.set(java.util.Calendar.SECOND, 0);
        pinjamCal.set(java.util.Calendar.MILLISECOND, 0);

        java.util.Calendar kembaliCal = java.util.Calendar.getInstance();
        kembaliCal.setTime(kembaliDate);
        kembaliCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        kembaliCal.set(java.util.Calendar.MINUTE, 0);
        kembaliCal.set(java.util.Calendar.SECOND, 0);
        kembaliCal.set(java.util.Calendar.MILLISECOND, 0);

        if (!kembaliCal.after(pinjamCal)) {
            JOptionPane.showMessageDialog(this, "Tanggal pengembalian harus lebih besar dari tanggal peminjaman!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = connectDB()) {
            conn.setAutoCommit(false);

            String insertQuery = "INSERT INTO tbpeminjaman (namaPeminjam, judulBuku, tanggalPinjam, tanggalKembali) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setString(1, namaPeminjam);
                stmt.setString(2, buku);
                stmt.setDate(3, new java.sql.Date(pinjamDate.getTime()));
                stmt.setDate(4, new java.sql.Date(kembaliDate.getTime()));
                stmt.executeUpdate();
            }

            String updateStokQuery = "UPDATE tbbuku SET stok = stok - 1 WHERE judul = ? AND stok > 0";
            try (PreparedStatement stmt = conn.prepareStatement(updateStokQuery)) {
                stmt.setString(1, buku);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected == 0) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Stok buku habis!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Peminjaman berhasil disimpan!");
            loadPeminjaman();
            loadBuku();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving peminjaman: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editPeminjaman() {
        int selectedRow = tablePeminjaman.getSelectedRow();
        if (selectedRow != -1) {
            int selectedId = (int) tableModel.getValueAt(selectedRow, 0);
            String selectedNamaPeminjam = (String) tableModel.getValueAt(selectedRow, 1);
            String selectedBuku = (String) tableModel.getValueAt(selectedRow, 2);
            Date selectedTanggalPinjam = (Date) tableModel.getValueAt(selectedRow, 3);
            Date selectedTanggalKembali = (Date) tableModel.getValueAt(selectedRow, 4);

            txtNamaPeminjam.setText(selectedNamaPeminjam);
            comboBuku.setSelectedItem(selectedBuku);
            datePinjam.setValue(selectedTanggalPinjam);
            dateKembali.setValue(selectedTanggalKembali);

            btnSimpan.setEnabled(false);
            btnEdit.setEnabled(false);
            btnDelete.setEnabled(false);
        }
    }

    private void deletePeminjaman() {
        int selectedRow = tablePeminjaman.getSelectedRow();
        if (selectedRow != -1) {
            int idToDelete = (int) tableModel.getValueAt(selectedRow, 0);
            String buku = (String) tableModel.getValueAt(selectedRow, 2);
    
            try (Connection conn = connectDB()) {
                conn.setAutoCommit(false);
    
                String deleteQuery = "DELETE FROM tbpeminjaman WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                    stmt.setInt(1, idToDelete);
                    stmt.executeUpdate();
                }
    
                String updateStokQuery = "UPDATE tbbuku SET stok = stok + 1 WHERE judul = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateStokQuery)) {
                    stmt.setString(1, buku);
                    stmt.executeUpdate();
                }
    
                conn.commit();
                JOptionPane.showMessageDialog(this, "Peminjaman berhasil dihapus!");
                loadPeminjaman();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting peminjaman: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cancelAction() {
        // Mengembalikan semua form ke keadaan awal
        txtNamaPeminjam.setText("");
        comboBuku.setSelectedIndex(0);
        datePinjam.setValue(new java.util.Date());
        dateKembali.setValue(new java.util.Date());
        btnSimpan.setEnabled(true);
        btnEdit.setEnabled(true);
        btnDelete.setEnabled(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HlmnPeminjaman().setVisible(true));
    }
}
