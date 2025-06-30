import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class Koneksi {
    private static Connection com;
    public static Connection connection() throws SQLException{
        if(com == null){
            try{
                String DB = "jdbc:mysql://localhost:3306/db_kasirbuku";
                String username = "root";
                String password = "";
                DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
                com = DriverManager.getConnection(DB, username, password);
            } 
            catch(SQLException e){
                JOptionPane.showMessageDialog(null, "Koneksi Gagal!" + e);
                throw e; // Agar exception dilemparkan kembali jika gagal
            }
        }
        return com; // Menambahkan return untuk mengembalikan koneksi
    }
}
