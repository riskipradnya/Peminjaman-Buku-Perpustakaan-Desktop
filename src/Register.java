import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Register {
    public void showRegisterForm() {
        JFrame registerFrame = new JFrame("Register Form");
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Spacing antar elemen
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Sans-Serif", Font.PLAIN, 14);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(labelFont);
        usernameLabel.setForeground(Color.BLACK);

        JTextField usernameField = new JTextField(12); // Lebih pendek

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(labelFont);
        emailLabel.setForeground(Color.BLACK);

        JTextField emailField = new JTextField(12);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        passwordLabel.setForeground(Color.BLACK);

        JPasswordField passwordField = new JPasswordField(12);

        JButton registerButton = new JButton("Register");
        registerButton.setBackground(new Color(0, 123, 255));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Sans-Serif", Font.BOLD, 14));
        registerButton.setFocusPainted(false);

        JButton loginButton = new JButton("Already have an account? Login");
        loginButton.setBackground(new Color(108, 117, 125));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Sans-Serif", Font.PLAIN, 12));
        loginButton.setFocusPainted(false);

        // Menambahkan komponen ke panel
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(emailLabel, gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(registerButton, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        registerFrame.add(panel);
        registerFrame.setSize(300, 300); // Dimensi frame lebih kecil
        registerFrame.setLocationRelativeTo(null);
        registerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        registerFrame.setVisible(true);

        // Aksi Tombol Register
        // Aksi Tombol Register
        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(registerFrame, "Please fill in all fields.");
            } else {
                try {
                    Connection conn = Koneksi.connection();
                    String sql = "INSERT INTO tbusers (username, email, password) VALUES (?, ?, ?)";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setString(1, username);
                    pst.setString(2, email);
                    pst.setString(3, password);

                    int rowsInserted = pst.executeUpdate();
                    if (rowsInserted > 0) {
                        JOptionPane.showMessageDialog(registerFrame, "Registration Successful!");
                        
                        // Mengosongkan semua field
                        usernameField.setText("");
                        emailField.setText("");
                        passwordField.setText("");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(registerFrame, "Database error: " + ex.getMessage());
                }
            }
        });



        // Aksi Tombol Login
        loginButton.addActionListener(e -> {
            registerFrame.dispose();
            new Login().showFrame();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Register().showRegisterForm());
    }
}
