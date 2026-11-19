package OrderManagement;
import java.sql.*;
import java.util.Scanner;
public class OrderManagement {
    static final String URL = "jdbc:mysql://localhost:3306/ecommerce";
    static final String USER = "root";
    static final String PASSWORD = "YourPassword";
    static Scanner sc = new Scanner(System.in);
    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("=================================");
            System.out.println("   ORDER MANAGEMENT SYSTEM");
            System.out.println("=================================");
            int choice;
            do {
                System.out.println("\n------ MENU ------");
                System.out.println("1. Insert Orders");
                System.out.println("2. Search Orders by Status and Quantity");
                System.out.println("3. Increase Order Quantity");
                System.out.println("4. Update Order Status");
                System.out.println("5. Calculate Total Order Value");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");
                choice = sc.nextInt();
                switch (choice) {
                    case 1:
                        insertOrders(con);
                        break;
                    case 2:
                        searchOrders(con);
                        break;
                    case 3:
                        increaseQuantity(con);
                        break;
                    case 4:
                        updateStatus(con);
                        break;
                    case 5:
                        totalOrderValue(con);
                        break;
                    case 6:
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice!");
                }

            } while (choice != 6);

            con.close();

        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }

        sc.close();
    }
    static void insertOrders(Connection con) {

        String sql = "INSERT INTO orders " +
                "(order_id, customer_name, product_name, quantity, unit_price, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = con.prepareStatement(sql);

            for (int i = 1; i <= 4; i++) {

                System.out.println("\nEnter details for Order " + i);

                System.out.print("Order ID: ");
                int id = sc.nextInt();

                sc.nextLine();

                System.out.print("Customer Name: ");
                String customer = sc.nextLine();

                System.out.print("Product Name: ");
                String product = sc.nextLine();

                System.out.print("Quantity: ");
                int quantity = sc.nextInt();

                System.out.print("Unit Price: ");
                double price = sc.nextDouble();

                sc.nextLine();

                System.out.print("Status: ");
                String status = sc.nextLine();

                ps.setInt(1, id);
                ps.setString(2, customer);
                ps.setString(3, product);
                ps.setInt(4, quantity);
                ps.setDouble(5, price);
                ps.setString(6, status);

                try {
                    ps.executeUpdate();
                    System.out.println("Order inserted successfully!");

                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println(
                            "Duplicate Order ID! Order " + id +
                            " already exists."
                    );
                }
            }

            ps.close();

        } catch (SQLException e) {
            System.out.println("Error inserting orders: " + e.getMessage());
        }
    }
    static void searchOrders(Connection con) {
        sc.nextLine();
        System.out.print("Enter order status: ");
        String status = sc.nextLine();
        System.out.print("Enter minimum quantity: ");
        int quantity = sc.nextInt();
        String sql = "SELECT * FROM orders " +
                "WHERE status = ? AND quantity > ? " +
                "ORDER BY unit_price DESC";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, quantity);
            ResultSet rs = ps.executeQuery();
            boolean found = false;
            System.out.println("\n------ ORDERS ------");
            while (rs.next()) {
                found = true;
                displayOrder(rs);
            }
            if (!found) {
                System.out.println("No orders found.");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error searching orders: " + e.getMessage());
        }
    }
    static void increaseQuantity(Connection con) {
        System.out.print("Enter Order ID: ");
        int orderId = sc.nextInt();
        System.out.print("Enter additional quantity: ");
        int additionalQty = sc.nextInt();
        String sql = "UPDATE orders " +
                "SET quantity = quantity + ? " +
                "WHERE order_id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, additionalQty);
            ps.setInt(2, orderId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Quantity updated successfully.");
                String selectSql = "SELECT * FROM orders WHERE order_id = ?";
                PreparedStatement selectPs =
                        con.prepareStatement(selectSql);
                selectPs.setInt(1, orderId);
                ResultSet rs = selectPs.executeQuery();
                if (rs.next()) {
                    System.out.println("\nUpdated Order:");
                    displayOrder(rs);
                }
                rs.close();
                selectPs.close();
            } else {
                System.out.println("Order does not exist.");
            }

            ps.close();

        } catch (SQLException e) {
            System.out.println("Error updating quantity: " + e.getMessage());
        }
    }
    static void updateStatus(Connection con) {
        System.out.print("Enter Order ID: ");
        int orderId = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter new status: ");
        String newStatus = sc.nextLine();
        if (!(newStatus.equalsIgnoreCase("Pending") ||
                newStatus.equalsIgnoreCase("Shipped") ||
                newStatus.equalsIgnoreCase("Delivered"))) {
            System.out.println(
                    "Invalid status! Only Pending, Shipped or Delivered allowed."
            );
            return;
        }
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Status updated successfully.");
                String selectSql =
                        "SELECT * FROM orders WHERE order_id = ?";
                PreparedStatement selectPs =
                        con.prepareStatement(selectSql);
                selectPs.setInt(1, orderId);
                ResultSet rs = selectPs.executeQuery();
                if (rs.next()) {
                    System.out.println("\nUpdated Order:");
                    displayOrder(rs);
                }
                rs.close();
                selectPs.close();
            } else {
                System.out.println("Order does not exist.");
            }
            ps.close();

        } catch (SQLException e) {
            System.out.println("Error updating status: " + e.getMessage());
        }
    }
    static void totalOrderValue(Connection con) {
        String sql = "SELECT SUM(quantity * unit_price) AS total " +
                "FROM orders";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                double total = rs.getDouble("total");
                System.out.println(
                        "\nTotal Order Value = Rs. " + total
                );
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println(
                    "Error calculating total: " + e.getMessage()
            );
        }
        System.out.print("\nEnter Order ID to delete: ");
        int orderId = sc.nextInt();

        String checkSql =
                "SELECT status FROM orders WHERE order_id = ?";

        String deleteSql =
                "DELETE FROM orders WHERE order_id = ? " +
                "AND status = 'Cancelled'";

        try {

            PreparedStatement checkPs =
                    con.prepareStatement(checkSql);

            checkPs.setInt(1, orderId);

            ResultSet rs = checkPs.executeQuery();

            if (!rs.next()) {
                System.out.println("Order does not exist.");
                rs.close();
                checkPs.close();
                return;
            }

            String status = rs.getString("status");

            rs.close();
            checkPs.close();

            if (!status.equalsIgnoreCase("Cancelled")) {

                System.out.println(
                        "Order cannot be deleted because its status is "
                                + status + "."
                );

                return;
            }

            PreparedStatement deletePs =
                    con.prepareStatement(deleteSql);

            deletePs.setInt(1, orderId);

            int rows = deletePs.executeUpdate();

            if (rows > 0) {
                System.out.println("Cancelled order deleted successfully.");
            } else {
                System.out.println("Order could not be deleted.");
            }

            deletePs.close();

        } catch (SQLException e) {
            System.out.println(
                    "Error deleting order: " + e.getMessage()
            );
        }
    }
    static void displayOrder(ResultSet rs) throws SQLException {
        System.out.println("--------------------------------");
        System.out.println("Order ID      : " +
                rs.getInt("order_id"));
        System.out.println("Customer Name : " +
                rs.getString("customer_name"));
        System.out.println("Product Name  : " +
                rs.getString("product_name"));
        System.out.println("Quantity      : " +
                rs.getInt("quantity"));
        System.out.println("Unit Price    : " +
                rs.getDouble("unit_price"));
        System.out.println("Status        : " +
                rs.getString("status"));
        System.out.println("--------------------------------");
    }
}