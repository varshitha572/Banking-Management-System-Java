import java.sql.*;
import java.util.Scanner;

public class BankingSystem {

    static final String URL = "jdbc:mysql://localhost:3306/bankdb";
    static final String USER = "bankuser";
    static final String PASSWORD = "1234";

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try (Connection conn =
                     DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.println("===== BANKING SYSTEM =====");

            System.out.print("Username: ");
            String username = sc.nextLine();

            System.out.print("Password: ");
            String password = sc.nextLine();

            String query =
                    "SELECT * FROM users WHERE username=? AND password=?";

            PreparedStatement ps =
                    conn.prepareStatement(query);

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                System.out.println("\nLogin Successful!");

                int userId = rs.getInt("id");

                while (true) {

                    System.out.println("\n===== MENU =====");
                    System.out.println("1. Check Balance");
                    System.out.println("2. Deposit");
                    System.out.println("3. Withdraw");
                    System.out.println("4. Transaction History");
                    System.out.println("5. Exit");

                    System.out.print("Enter Choice: ");
                    int choice = sc.nextInt();

                    switch (choice) {

                        case 1:
                            checkBalance(conn, userId);
                            break;

                        case 2:
                            deposit(conn, userId);
                            break;

                        case 3:
                            withdraw(conn, userId);
                            break;

                        case 4:
                            transactionHistory(conn, userId);
                            break;

                        case 5:
                            System.out.println("Thank You!");
                            return;

                        default:
                            System.out.println("Invalid Choice!");
                    }
                }

            } else {

                System.out.println("Invalid Login");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void checkBalance(Connection conn, int userId)
            throws SQLException {

        String query =
                "SELECT balance FROM users WHERE id=?";

        PreparedStatement ps =
                conn.prepareStatement(query);

        ps.setInt(1, userId);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            System.out.println(
                    "Current Balance: ₹" +
                            rs.getDouble("balance"));
        }
    }

    static void deposit(Connection conn, int userId)
            throws SQLException {

        System.out.print("Enter Amount: ");
        double amount = sc.nextDouble();

        if (amount <= 0) {
            System.out.println("Invalid Amount!");
            return;
        }

        String query =
                "UPDATE users SET balance = balance + ? WHERE id=?";

        PreparedStatement ps =
                conn.prepareStatement(query);

        ps.setDouble(1, amount);
        ps.setInt(2, userId);

        ps.executeUpdate();

        PreparedStatement trans =
                conn.prepareStatement(
                        "INSERT INTO transactions(user_id,type,amount) VALUES(?,?,?)");

        trans.setInt(1, userId);
        trans.setString(2, "Deposit");
        trans.setDouble(3, amount);

        trans.executeUpdate();

        System.out.println("Deposit Successful!");

        checkBalance(conn, userId);
    }

    static void withdraw(Connection conn, int userId)
            throws SQLException {

        System.out.print("Enter Amount: ");
        double amount = sc.nextDouble();

        if (amount <= 0) {
            System.out.println("Invalid Amount!");
            return;
        }

        String balanceQuery =
                "SELECT balance FROM users WHERE id=?";

        PreparedStatement balancePs =
                conn.prepareStatement(balanceQuery);

        balancePs.setInt(1, userId);

        ResultSet rs = balancePs.executeQuery();

        if (rs.next()) {

            double currentBalance =
                    rs.getDouble("balance");

            if (currentBalance >= amount) {

                String withdrawQuery =
                        "UPDATE users SET balance = balance - ? WHERE id=?";

                PreparedStatement withdrawPs =
                        conn.prepareStatement(withdrawQuery);

                withdrawPs.setDouble(1, amount);
                withdrawPs.setInt(2, userId);

                withdrawPs.executeUpdate();

                PreparedStatement trans =
                        conn.prepareStatement(
                                "INSERT INTO transactions(user_id,type,amount) VALUES(?,?,?)");

                trans.setInt(1, userId);
                trans.setString(2, "Withdraw");
                trans.setDouble(3, amount);

                trans.executeUpdate();

                System.out.println("Withdrawal Successful!");

                checkBalance(conn, userId);

            } else {

                System.out.println("Insufficient Balance!");
            }
        }
    }

    static void transactionHistory(Connection conn, int userId)
            throws SQLException {

        String query =
                "SELECT * FROM transactions WHERE user_id=? ORDER BY date DESC";

        PreparedStatement ps =
                conn.prepareStatement(query);

        ps.setInt(1, userId);

        ResultSet rs = ps.executeQuery();

        System.out.println("\n===== TRANSACTION HISTORY =====");

        while (rs.next()) {

            System.out.println(
                    rs.getString("type")
                            + " ₹"
                            + rs.getDouble("amount")
                            + " | "
                            + rs.getTimestamp("date"));
        }
    }
}