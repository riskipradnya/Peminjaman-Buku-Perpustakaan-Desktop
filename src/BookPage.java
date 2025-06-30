import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class BookPage extends JFrame {
    private JTextField bookTitleField, authorField, genreField, stockField;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private int selectedBookId = -1;  // Menyimpan ID buku yang dipilih untuk edit

    public BookPage() {
        setTitle("Manajemen Buku - Toko Buku");
        setSize(800, 600);  // Lebar diperbesar agar form lebih lebar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Membuat Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Navigasi");
        JMenuItem menuItemBookPage = new JMenuItem("Halaman Buku");
        JMenuItem menuItemLoanPage = new JMenuItem("Halaman Peminjaman");
        JMenuItem menuItemReturnPage = new JMenuItem("Halaman Pengembalian");

        // Menambahkan action listener untuk menu item
        menuItemBookPage.addActionListener(e -> JOptionPane.showMessageDialog(this, "Anda sudah berada di Halaman Buku"));
        menuItemLoanPage.addActionListener(e -> navigateToLoanPage());
        menuItemReturnPage.addActionListener(e -> navigateToReturnPage());

        menu.add(menuItemBookPage);
        menu.add(menuItemLoanPage);
        menu.add(menuItemReturnPage);  // Menambahkan item menu Halaman Pengembalian
        menuBar.add(menu);  // Menambahkan menu ke menu bar
        setJMenuBar(menuBar);  // Set menu bar ke frame

        // Panel Input
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Form Tambah Buku"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Judul Buku:"), gbc);
        bookTitleField = new JTextField();
        bookTitleField.setPreferredSize(new Dimension(650, 25));  // Mengatur lebar kolom input
        gbc.gridx = 1;
        inputPanel.add(bookTitleField, gbc);

        // Pengarang
        gbc.gridy = 1;
        gbc.gridx = 0;
        inputPanel.add(new JLabel("Pengarang:"), gbc);
        authorField = new JTextField();
        gbc.gridx = 1;
        authorField.setColumns(30);  // Set lebar kolom
        inputPanel.add(authorField, gbc);

        // Genre
        gbc.gridy = 2;
        gbc.gridx = 0;
        inputPanel.add(new JLabel("Genre:"), gbc);
        genreField = new JTextField();
        gbc.gridx = 1;
        genreField.setColumns(30);  // Set lebar kolom
        inputPanel.add(genreField, gbc);

        // Stok
        gbc.gridy = 4;
        gbc.gridx = 0;
        inputPanel.add(new JLabel("Stok:"), gbc);
        stockField = new JTextField();
        gbc.gridx = 1;
        stockField.setColumns(30);  // Set lebar kolom
        inputPanel.add(stockField, gbc);

        // Tombol
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Tambah");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Hapus");
        JButton cancelButton = new JButton("Batal");  // Tombol Batal
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);  // Tambahkan tombol Batal ke panel tombol

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        inputPanel.add(buttonPanel, gbc);

        // Tabel Buku
        tableModel = new DefaultTableModel(new String[]{"ID", "Judul", "Pengarang", "Genre", "Stok"}, 0);
        bookTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(bookTable);

        // Set ukuran kolom tabel agar memenuhi lebar
        bookTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        bookTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        bookTable.getColumnModel().getColumn(4).setPreferredWidth(50);

        // Layout
        setLayout(new BorderLayout(10, 10));
        add(inputPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);

        // Listener
        addButton.addActionListener(e -> addBook());
        editButton.addActionListener(e -> editBook());
        deleteButton.addActionListener(e -> deleteBook());
        cancelButton.addActionListener(e -> cancelAction());  // Listener untuk tombol Batal

        loadBooks();
    }

    private void navigateToLoanPage() {
        // Membuka halaman peminjaman
        JOptionPane.showMessageDialog(this, "Navigasi ke Halaman Peminjaman");
        new HlmnPeminjaman().setVisible(true);  // Contoh membuka frame LoanPage
        this.dispose();  // Menutup halaman buku setelah berpindah
    }

    private void navigateToReturnPage() {
        // Membuka halaman pengembalian
        JOptionPane.showMessageDialog(this, "Navigasi ke Halaman Pengembalian");
        new HlmnPengembalian().setVisible(true);  // Membuka Halaman Pengembalian
        this.dispose();  // Menutup halaman buku setelah berpindah
    }

    private void cancelAction() {
        // Reset form input ke keadaan awal
        clearFields();
        selectedBookId = -1;  // Reset ID buku yang dipilih
        bookTable.clearSelection();  // Menghapus seleksi baris tabel
    }

    private void addBook() {
        // Menambahkan buku baru atau mengupdate buku yang ada
        String title = bookTitleField.getText();
        String author = authorField.getText();
        String genre = genreField.getText();
        String stockText = stockField.getText();
    
        // Validasi input
        if (title.isEmpty() || author.isEmpty() || genre.isEmpty() || stockText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi semua field!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        try {
            int stock = Integer.parseInt(stockText);
            if (stock < 0) {
                JOptionPane.showMessageDialog(this, "Stok tidak boleh negatif!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
    
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_kasirbuku", "root", "");
    
            // Proses tambah atau update buku
            if (selectedBookId == -1) {  // Tambah buku baru
                String query = "INSERT INTO tbbuku (judul, pengarang, genre, stok) VALUES (?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, title);
                preparedStatement.setString(2, author);
                preparedStatement.setString(3, genre);
                preparedStatement.setInt(4, stock);
                preparedStatement.executeUpdate();
                JOptionPane.showMessageDialog(this, "Buku berhasil ditambahkan!");
            } else {  // Update buku yang ada
                String query = "UPDATE tbbuku SET judul = ?, pengarang = ?, genre = ?, stok = ? WHERE id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, title);
                preparedStatement.setString(2, author);
                preparedStatement.setString(3, genre);
                preparedStatement.setInt(4, stock);
                preparedStatement.setInt(5, selectedBookId);
                preparedStatement.executeUpdate();
                JOptionPane.showMessageDialog(this, "Buku berhasil diperbarui!");
            }
    
            connection.close();
            clearFields();
            selectedBookId = -1;  // Reset ID setelah operasi selesai
            loadBooks();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Masukkan angka yang valid untuk stok!", "Kesalahan", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Fungsi untuk memuat data buku dari database ke tabel
    private void loadBooks() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_kasirbuku", "root", "");
            String query = "SELECT * FROM tbbuku";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
    
            tableModel.setRowCount(0);  // Reset data tabel
    
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("judul");
                String author = resultSet.getString("pengarang");
                String genre = resultSet.getString("genre");
                int stock = resultSet.getInt("stok");
                tableModel.addRow(new Object[]{id, title, author, genre, stock});
            }
    
            connection.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Fungsi untuk membersihkan field input
    private void clearFields() {
        bookTitleField.setText("");
        authorField.setText("");
        genreField.setText("");
        stockField.setText("");
    }
    
    // Fungsi untuk mengedit buku
    private void editBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedBookId = (int) tableModel.getValueAt(selectedRow, 0);
            bookTitleField.setText((String) tableModel.getValueAt(selectedRow, 1));
            authorField.setText((String) tableModel.getValueAt(selectedRow, 2));
            genreField.setText((String) tableModel.getValueAt(selectedRow, 3));
            stockField.setText(tableModel.getValueAt(selectedRow, 4).toString());
        } else {
            JOptionPane.showMessageDialog(this, "Pilih buku yang ingin diedit.", "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    // Fungsi untuk menghapus buku
    private void deleteBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow != -1) {
            int bookId = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus buku ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_kasirbuku", "root", "");
                    String query = "DELETE FROM tbbuku WHERE id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setInt(1, bookId);
                    preparedStatement.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Buku berhasil dihapus.");
                    loadBooks();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih buku yang ingin dihapus.", "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BookPage().setVisible(true));
    }
}
