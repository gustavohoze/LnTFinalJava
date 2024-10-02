package main;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

import javax.swing.*;

public class Main {
	
	static Scanner sc = new Scanner(System.in);
	static String db = "jdbc:mysql://localhost:3306/pt_pudding";
	static String dbUN = "root";
	static String dbPW = "root";
	static Random rd = new Random();
	JFrame frame;
	JLabel label;
	JButton buttonUpdate,buttonView,buttonCreate,buttonDelete;
	JPanel panel;
	static JTextArea outputArea;
	
	 public Main() {
	        frame = new JFrame();
	        panel = new JPanel();
	        buttonCreate = new JButton("1. Insert Menu");
	        buttonView = new JButton("2. View Menu");
	        buttonUpdate = new JButton("3. Update Menu");
	        buttonDelete = new JButton("4. Delete Menu");
	        outputArea = new JTextArea(10, 30);
	        outputArea.setEditable(false);

	        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
	        panel.setLayout(new GridLayout(0, 1));
	        panel.add(buttonCreate);
	        panel.add(buttonView);
	        panel.add(buttonUpdate);
	        panel.add(buttonDelete);

	        frame.add(panel, BorderLayout.NORTH);
	        frame.add(new JScrollPane(outputArea), BorderLayout.CENTER);
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.setTitle("PT PUDDING");
	        frame.pack();
	        frame.setVisible(true);

	        addActionListeners();
	    }

	    private void addActionListeners() {
	        buttonCreate.addActionListener(e -> insertMenu());
	        buttonView.addActionListener(e -> viewMenu());
	        buttonUpdate.addActionListener(e -> updateMenu());
	        buttonDelete.addActionListener(e -> deleteMenu());
	    }
	
	
//	public static void Menu() {
//		new Main();
//		System.out.println("1. Insert Menu");
//		System.out.println("2. View Menu");
//		System.out.println("3. Update Menu");
//		System.out.println("4. Delete Menu");
//		System.out.println("5. Exit Apps");
//		System.out.print(">> ");
//	}
	
	
	public static void DBConnection() {
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			try(Connection conn = DriverManager.getConnection(db,dbUN,dbPW)) {
				String syntax = "CREATE TABLE IF NOT EXISTS MENU(ID VARCHAR(6), NAME VARCHAR(255), PRICE INT, STOCK INT)";
				try(Statement stmt = conn.createStatement()){
					stmt.execute(syntax);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	private void insertMenu() {
        String name = JOptionPane.showInputDialog(frame, "Enter menu name:");
        String priceStr = JOptionPane.showInputDialog(frame, "Enter price:");
        String stockStr = JOptionPane.showInputDialog(frame, "Enter stock:");

        try {
            int price = Integer.parseInt(priceStr);
            int stock = Integer.parseInt(stockStr);
            String id = "PD-";
            int intID = rd.nextInt(999);
            id += String.format("%03d", intID);

            String sql = "INSERT INTO MENU (ID, NAME, PRICE, STOCK) VALUES (?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(db, dbUN, dbPW);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                pstmt.setString(2, name);
                pstmt.setInt(3, price);
                pstmt.setInt(4, stock);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    outputArea.append("Menu item inserted successfully.\n");
                }
            }
        } catch (NumberFormatException ex) {
            outputArea.append("Invalid price or stock. Please enter numbers.\n");
        } catch (SQLException ex) {
            outputArea.append("Error inserting menu item: " + ex.getMessage() + "\n");
        }
    }
	
	private static void viewMenu() {
        outputArea.setText(""); // Clear previous output
        NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String sql = "SELECT * FROM MENU ORDER BY PRICE DESC";
        try (Connection conn = DriverManager.getConnection(db, dbUN, dbPW);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                outputArea.append(String.format("%s - %s - %s - %d\n",
                    rs.getString("ID"),
                    rs.getString("NAME"),
                   rupiahFormat.format(rs.getInt("PRICE")),
                    rs.getInt("STOCK")));
            }
        } catch (SQLException ex) {
            outputArea.append("Error viewing menu: " + ex.getMessage() + "\n");
        }
    }
	
	public static boolean findMenu(String id) {
	    String syntax = "SELECT COUNT(*) FROM MENU WHERE ID = ?";
	    try (Connection conn = DriverManager.getConnection(db, dbUN, dbPW);
	         PreparedStatement psyntax = conn.prepareStatement(syntax)) {
	        psyntax.setString(1, id);
	        try (ResultSet rs = psyntax.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt(1) > 0;
	            }
	        }
	    } catch (SQLException e) {
	        System.out.println("Error finding menu: " + e.getMessage());
	        e.printStackTrace();
	    }
	    return false;
	}
	
	public static void updateMenuItem(String id) {
	    String input;
	    int updatePrice = 0;
	    int updateStock = 0;
	    
	    do {
	        System.out.print("Which properties do you wish to update? [PRICE | STOCK | BOTH]: ");
	        input = sc.nextLine().toUpperCase();
	    } while (!input.equals("PRICE") && !input.equals("STOCK") && !input.equals("BOTH"));

	    if (input.equals("PRICE") || input.equals("BOTH")) {
	        updatePrice = getValidIntInput("Input price: ");
	    }
	    
	    if (input.equals("STOCK") || input.equals("BOTH")) {
	        updateStock = getValidIntInput("Input stock: ");
	    }

	    String priceSyntax = "UPDATE MENU SET PRICE = ? WHERE ID = ?";
	    String stockSyntax = "UPDATE MENU SET STOCK = ? WHERE ID = ?";

	    try (Connection conn = DriverManager.getConnection(db, dbUN, dbPW)) {
	        if (input.equals("PRICE") || input.equals("BOTH")) {
	            updateField(conn, priceSyntax, updatePrice, id, "price");
	        }
	        if (input.equals("STOCK") || input.equals("BOTH")) {
	            updateField(conn, stockSyntax, updateStock, id, "stock");
	        }
	        System.out.println("Successfully updated item!");
	    } catch (SQLException e) {
	        System.out.println("Error updating menu item: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	private static int getValidIntInput(String prompt) {
	    while (true) {
	        System.out.print(prompt);
	        try {
	            return Integer.parseInt(sc.nextLine());
	        } catch (NumberFormatException e) {
	            System.out.println("Invalid input. Please enter a valid number.");
	        }
	    }
	}

	private static void updateField(Connection conn, String sql, int value, String id, String fieldName) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, value);
            pstmt.setString(2, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                outputArea.append("Successfully updated " + fieldName + "!\n");
            } else {
                outputArea.append("No rows updated for " + fieldName + ". Please check the ID.\n");
            }
        }
    }
	
	private void updateMenu() {
		viewMenu();
        String id = JOptionPane.showInputDialog(frame, "Enter Menu's ID to update:");
        if (id == null || id.trim().isEmpty()) return;

        String[] options = {"PRICE", "STOCK", "BOTH"};
        int choice = JOptionPane.showOptionDialog(frame, "Which properties do you wish to update?",
            "Update Menu", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);

        try {
            Connection conn = DriverManager.getConnection(db, dbUN, dbPW);
            if (choice == 0 || choice == 2) { // Update PRICE
                String priceStr = JOptionPane.showInputDialog(frame, "Enter new price:");
                int price = Integer.parseInt(priceStr);
                updateField(conn, "UPDATE MENU SET PRICE = ? WHERE ID = ?", price, id, "price");
            }
            if (choice == 1 || choice == 2) { // Update STOCK
                String stockStr = JOptionPane.showInputDialog(frame, "Enter new stock:");
                int stock = Integer.parseInt(stockStr);
                updateField(conn, "UPDATE MENU SET STOCK = ? WHERE ID = ?", stock, id, "stock");
            }
            conn.close();
        } catch (NumberFormatException ex) {
            outputArea.append("Invalid input. Please enter numbers for price and stock.\n");
        } catch (SQLException ex) {
            outputArea.append("Error updating menu: " + ex.getMessage() + "\n");
        }
    }
	
	
	public static void deleteMenuItem(String id) {
	    String syntax = "DELETE FROM MENU WHERE ID = ?";
	    try (Connection conn = DriverManager.getConnection(db, dbUN, dbPW);
	         PreparedStatement psyntax = conn.prepareStatement(syntax)) {
	        
	        psyntax.setString(1, id);
	        int rowsAffected = psyntax.executeUpdate();
	        
	        if (rowsAffected > 0) {
	            System.out.println("Menu item with ID " + id + " has been successfully deleted!");
	        } else {
	            System.out.println("No menu item found with ID " + id + ". No deletion occurred.");
	        }
	    } catch (SQLException e) {
	        System.out.println("Error deleting menu item: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	private void deleteMenu() {
		viewMenu();
        String id = JOptionPane.showInputDialog(frame, "Enter Menu's ID to delete:");
        if (id == null || id.trim().isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(frame,
            "Are you sure you want to delete this item?",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM MENU WHERE ID = ?";
            try (Connection conn = DriverManager.getConnection(db, dbUN, dbPW);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    outputArea.append("Menu item with ID " + id + " has been successfully deleted!\n");
                } else {
                    outputArea.append("No menu item found with ID " + id + ". No deletion occurred.\n");
                }
            } catch (SQLException ex) {
                outputArea.append("Error deleting menu item: " + ex.getMessage() + "\n");
            }
        } else {
            outputArea.append("Deletion cancelled.\n");
        }
    }
	

	public static void main(String[] args) {
		new Main();

	}
	
	
	

}
