import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class HlmnPengembalian extends JFrame {
    private JTextField txtCariPeminjam;
    private JButton btnCari;
    private JTable tablePeminjaman;
    private DefaultTableModel tableModel;
    private JButton btnKembalikan;
    
    // Koneksi Database
    private Connection connectDB() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/db_kasirbuku", "root", "");
    }

    public HlmnPengembalian() {
        setTitle("Halaman Pengembalian Buku");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Membuat Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Navigasi");
        JMenuItem menuItemBookPage = new JMenuItem("Halaman Buku");
        JMenuItem menuItemLoanPage = new JMenuItem("Halaman Peminjaman");
        JMenuItem menuItemReturnPage = new JMenuItem("Halaman Pengembalian");

        // Menambahkan action listener untuk menu item
        menuItemBookPage.addActionListener(e -> navigateToBookPage());
        menuItemLoanPage.addActionListener(e -> navigateToLoanPage());
        menuItemReturnPage.addActionListener(e -> JOptionPane.showMessageDialog(this, "Anda sudah berada di Halaman Pengembalian"));

        menu.add(menuItemBookPage);
        menu.add(menuItemLoanPage);
        menu.add(menuItemReturnPage);
        menuBar.add(menu);  
        setJMenuBar(menuBar);

        // Panel Pencarian
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
        txtCariPeminjam = new JTextField(20);
        btnCari = new JButton("Cari");
        btnKembalikan = new JButton("Kembalikan Buku");
        btnKembalikan.setEnabled(false); // Tombol tidak aktif awalnya

        // Menambahkan tombol cari dan kembalikan ke panel pencarian
        panelSearch.add(new JLabel("Nama Peminjam:"));
        panelSearch.add(txtCariPeminjam);
        panelSearch.add(btnCari);
        panelSearch.add(btnKembalikan); 

        // Tabel Daftar Peminjaman
        tableModel = new DefaultTableModel(new String[]{"ID", "Nama Peminjam", "Judul Buku", "Tanggal Pinjam", "Tanggal Kembali"}, 0);
        tablePeminjaman = new JTable(tableModel);

        // Sembunyikan kolom ID
        tablePeminjaman.getColumnModel().getColumn(0).setMinWidth(0);
        tablePeminjaman.getColumnModel().getColumn(0).setMaxWidth(0);
        tablePeminjaman.getColumnModel().getColumn(0).setWidth(0);

        // Tambahkan Listener untuk Tombol Cari
        btnCari.addActionListener(e -> cariPeminjam());

        // Tombol Proses Pengembalian
        btnKembalikan.addActionListener(e -> prosesPengembalian());

        // Layout Komponen
        add(panelSearch, BorderLayout.NORTH);
        add(new JScrollPane(tablePeminjaman), BorderLayout.CENTER);
        
        // Menambahkan listener untuk memilih baris pada tabel
        tablePeminjaman.getSelectionModel().addListSelectionListener(e -> {
            btnKembalikan.setEnabled(!tablePeminjaman.getSelectionModel().isSelectionEmpty());
        });
    }

    private void navigateToBookPage() {
        // Navigasi ke Halaman Buku
        JOptionPane.showMessageDialog(this, "Navigasi ke Halaman Buku");
        new BookPage().setVisible(true);  // Pindah ke halaman buku
        this.dispose();  // Menutup Halaman Peminjaman
    }

    private void navigateToLoanPage() {
        // Arahkan ke Halaman Peminjaman (buat metode atau kelas baru sesuai kebutuhan)
        JOptionPane.showMessageDialog(this, "Navigasi ke Halaman Peminjaman");
        new HlmnPeminjaman().setVisible(true);  // Pindah ke halaman pengembalian
        this.dispose();  // Menutup Halaman Peminjaman
    }

    private void cariPeminjam() {
        String namaPeminjam = txtCariPeminjam.getText().trim();

        if (namaPeminjam.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama peminjam tidak boleh kosong!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = connectDB()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id, namaPeminjam, judulBuku, tanggalPinjam, tanggalKembali FROM tbpeminjaman WHERE namaPeminjam LIKE ?"
            );
            ps.setString(1, "%" + namaPeminjam + "%");
            ResultSet rs = ps.executeQuery();

            tableModel.setRowCount(0); 
            while (rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(String.valueOf(rs.getInt("id"))); // ID
                row.add(rs.getString("namaPeminjam"));
                row.add(rs.getString("judulBuku"));
                row.add(rs.getString("tanggalPinjam"));
                row.add(rs.getString("tanggalKembali"));
                tableModel.addRow(row);
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Tidak ada data peminjaman ditemukan!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data dari database!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void prosesPengembalian() {
        int selectedRow = tablePeminjaman.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data peminjaman terlebih dahulu!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0)); // Ambil ID
        String judulBuku = (String) tableModel.getValueAt(selectedRow, 2); // Judul Buku

        try (Connection conn = connectDB()) {
            // Update stok buku
            PreparedStatement psUpdateStok = conn.prepareStatement("UPDATE tbbuku SET stok = stok + 1 WHERE judul = ?");
            psUpdateStok.setString(1, judulBuku);
            psUpdateStok.executeUpdate();

            // Hapus data peminjaman
            PreparedStatement psDelete = conn.prepareStatement("DELETE FROM tbpeminjaman WHERE id = ?");
            psDelete.setInt(1, id);
            psDelete.executeUpdate();

            JOptionPane.showMessageDialog(this, "Buku berhasil dikembalikan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            cariPeminjam(); // Perbarui tabel
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memproses pengembalian!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HlmnPengembalian().setVisible(true));
    }
}
