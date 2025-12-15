import java.sql.*;

public class TestMySQL {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/artha_social";
        String username = "root";
        String password = "Sam@2006";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL Driver loaded");

            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Connected to MySQL!");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");
            rs.next();
            System.out.println("✅ Users table accessible! Count: " + rs.getInt("count"));

            conn.close();
            System.out.println("✅ ALL WORKING!");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
