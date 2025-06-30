import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Login {
    public void showFrame() {
        JFrame textFrame = new JFrame("Login Form");
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        Font labelFont = new Font("Sans-Serif", Font.PLAIN, 14);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(labelFont);
        usernameLabel.setForeground(Color.BLACK);

        JTextField usernameField = new JTextField(15);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        passwordLabel.setForeground(Color.BLACK);

        JPasswordField passwordField = new JPasswordField(15);

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0, 120, 215)); // Biru muda
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Sans-Serif", Font.BOLD, 14));
        loginButton.setFocusPainted(false);

        JLabel registerLabel = new JLabel("Register");
        registerLabel.setFont(new Font("Sans-Serif", Font.PLAIN, 12));
        registerLabel.setForeground(new Color(0, 120, 215)); // Biru
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new Register().showRegisterForm();
                textFrame.dispose();
            }
        });

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(loginButton, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(registerLabel, gbc);

        textFrame.add(panel);
        textFrame.setSize(350, 220);
        textFrame.setLocationRelativeTo(null);
        textFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        textFrame.setVisible(true);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(textFrame, "Please enter both username and password.");
            } else {
                try {
                    Connection conn = Koneksi.connection(); // Koneksi ke database
                    String sql = "SELECT * FROM tbusers WHERE username = ? AND password = ?";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setString(1, username);
                    pst.setString(2, password);
                    ResultSet rs = pst.executeQuery();

                    if (rs.next()) {
                        JOptionPane.showMessageDialog(textFrame, "Login Successful!");
                        textFrame.dispose(); // Tutup form login
                        new BookPage().setVisible(true); // Tampilkan BookPage
                    } else {
                        JOptionPane.showMessageDialog(textFrame, "Invalid username or password.");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(textFrame, "Database error: " + ex.getMessage());
                }
            }
        });
    }

    public static void main(String[] args) {
        Login login = new Login();
        login.showFrame();
    }
}
