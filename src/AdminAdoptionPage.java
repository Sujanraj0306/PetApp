package src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class AdminAdoptionPage extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusComboBox;
    private JButton updateButton;
    private JButton refreshButton;
    private JButton addPetButton;
    private JButton deleteRequestButton;
    private JButton deleteAllRequestsButton;
    private JButton deletePetButton;
    private String[] columnNames = {"Request ID", "Pet Name", "Adopter Name", "Email", "Phone Number", "Gov ID", "Address", "Status", "Request Date"};
    private Connection conn;

    // SQL Queries
    private static final String SELECT_ADOPTION_REQUESTS = "SELECT * FROM adoption_requests";
    private static final String UPDATE_ADOPTION_STATUS = "UPDATE adoption_requests SET request_status = ? WHERE request_id = ?";
    private static final String MARK_PET_ADOPTED = "UPDATE adoption_requests SET is_adopted = TRUE WHERE pet_name = ?";
    private static final String REJECT_OTHER_REQUESTS = "UPDATE adoption_requests SET request_status = 'Rejected' WHERE pet_name = ? AND request_id != ? AND request_status = 'Pending'";
    private static final String DELETE_ADOPTION_REQUEST = "DELETE FROM adoption_requests WHERE request_id = ?";
    private static final String DELETE_ALL_ADOPTION_REQUESTS = "DELETE FROM adoption_requests";
    private static final String DELETE_PET = "DELETE FROM pet_details WHERE pet_id = ?";
    private static final String SELECT_PETS = "SELECT pet_id, pet_name, type FROM pet_details";

    public AdminAdoptionPage() {
        setTitle("Admin - Manage Adoption Requests");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(192, 57, 43));
        JLabel headerLabel = new JLabel("Adoption Requests Management");
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerPanel.add(headerLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel controlPanel = new JPanel(new FlowLayout());

        addPetButton = new JButton("Add New Pet");
        addPetButton.addActionListener(this::openAddPetDialog);
        controlPanel.add(addPetButton);

        deletePetButton = new JButton("Delete Pet");
        deletePetButton.addActionListener(e -> openDeletePetDialog());
        controlPanel.add(deletePetButton);

        JLabel statusLabel = new JLabel("Change Request Status:");
        controlPanel.add(statusLabel);
        statusComboBox = new JComboBox<>(new String[]{"Pending", "Confirmed", "Rejected"});
        controlPanel.add(statusComboBox);

        updateButton = new JButton("Update Status");
        updateButton.addActionListener(e -> updateAdoptionStatus());
        controlPanel.add(updateButton);

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshAdoptionRequests());
        controlPanel.add(refreshButton);

        deleteRequestButton = new JButton("Delete Request");
        deleteRequestButton.addActionListener(e -> deleteSelectedRequest());
        controlPanel.add(deleteRequestButton);

        deleteAllRequestsButton = new JButton("Delete All Requests");
        deleteAllRequestsButton.addActionListener(e -> deleteAllRequests());
        controlPanel.add(deleteAllRequestsButton);

        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        fetchAdoptionRequests();

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/pet_adoption_db", "root", "password");
    }

    private void fetchAdoptionRequests() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ADOPTION_REQUESTS)) {

            tableModel.setRowCount(0); // Clear existing rows
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("request_id"),
                    rs.getString("pet_name"),
                    rs.getString("adopter_name"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getString("gov_id_number"),
                    rs.getString("address"),
                    rs.getString("request_status"),
                    rs.getTimestamp("request_date")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to fetch adoption requests.");
        }
    }

    private void refreshAdoptionRequests() {
        fetchAdoptionRequests();
    }

    private void updateAdoptionStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a row first.");
            return;
        }

        int requestId = (int) tableModel.getValueAt(selectedRow, 0);
        String newStatus = (String) statusComboBox.getSelectedItem();
        String petName = (String) tableModel.getValueAt(selectedRow, 1);

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(UPDATE_ADOPTION_STATUS)) {
                ps.setString(1, newStatus);
                ps.setInt(2, requestId);
                ps.executeUpdate();
            }

            tableModel.setValueAt(newStatus, selectedRow, 7);

            if ("Confirmed".equals(newStatus)) {
                rejectOtherRequests(conn, petName, requestId);
                markPetAsAdopted(conn, petName);
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Adoption status updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace(); // This will print the full stack trace to help identify the issue
            showError("Failed to update adoption status: " + e.getMessage());
        }
        
    }

    private void markPetAsAdopted(Connection conn, String petName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(MARK_PET_ADOPTED)) {
            ps.setString(1, petName);
            ps.executeUpdate();
        }
    }

    private void rejectOtherRequests(Connection conn, String petName, int confirmedRequestId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(REJECT_OTHER_REQUESTS)) {
            ps.setString(1, petName);
            ps.setInt(2, confirmedRequestId);
            ps.executeUpdate();
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 1).equals(petName) && (int) tableModel.getValueAt(i, 0) != confirmedRequestId) {
                tableModel.setValueAt("Rejected", i, 7);
            }
        }
    }

    // Method to open the add pet dialog
private void openAddPetDialog(ActionEvent e) {
     JDialog addPetDialog = new JDialog(this, "Add New Pet", true);
     addPetDialog.setSize(400, 400);
     addPetDialog.setLocationRelativeTo(this);
     
     // Create a panel for the dialog with a GridBagLayout for better arrangement
     JPanel panel = new JPanel();
     panel.setLayout(new GridBagLayout());
     panel.setBackground(new Color(240, 240, 240)); // Light gray background
     GridBagConstraints gbc = new GridBagConstraints();
     gbc.insets = new Insets(5, 5, 5, 5); // Add padding around components
     
     // Create and set up components
     JLabel petNameLabel = new JLabel("Pet Name:");
     JTextField petNameField = new JTextField(15);
     
     JLabel breedLabel = new JLabel("Breed:");
     JTextField breedField = new JTextField(15); // Changed to JTextField for breed input
     
     JLabel ageLabel = new JLabel("Age:");
     JTextField ageField = new JTextField(15);
     
     JLabel typeLabel = new JLabel("Type:");
     JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Dog", "Cat", "Rat", "Rabbit", "Bird"});
     
     JLabel natureLabel = new JLabel("Nature:");
     JComboBox<String> natureComboBox = new JComboBox<>(new String[]{"Gentle", "Energetic", "Curious", "Loyal", "Protective", "Talkative", "Skittish", "Friendly", "Calm", "Playful"});
     
     JLabel imageNameLabel = new JLabel("Image Name:");
     JTextField imageNameField = new JTextField(15);
     
     // Button to add pet
     JButton addButton = new JButton("Add Pet");
     addButton.setBackground(new Color(46, 204, 113)); // Green background
     addButton.setForeground(Color.WHITE);
     addButton.addActionListener(event -> {
         String petName = petNameField.getText();
         String breed = breedField.getText();
         int age = Integer.parseInt(ageField.getText());
         String type = (String) typeComboBox.getSelectedItem();
         String nature = (String) natureComboBox.getSelectedItem();
         String imageName = imageNameField.getText();
 
         // Insert new pet into the database
         addNewPetToDatabase(petName, breed, age, type, nature, imageName);
         addPetDialog.dispose();
     });
 
     // Arrange components in the panel using GridBagLayout
     gbc.gridx = 0;
     gbc.gridy = 0;
     panel.add(petNameLabel, gbc);
     gbc.gridx = 1;
     panel.add(petNameField, gbc);
 
     gbc.gridx = 0;
     gbc.gridy = 1;
     panel.add(breedLabel, gbc);
     gbc.gridx = 1;
     panel.add(breedField, gbc);
 
     gbc.gridx = 0;
     gbc.gridy = 2;
     panel.add(ageLabel, gbc);
     gbc.gridx = 1;
     panel.add(ageField, gbc);
 
     gbc.gridx = 0;
     gbc.gridy = 3;
     panel.add(typeLabel, gbc);
     gbc.gridx = 1;
     panel.add(typeComboBox, gbc);
 
     gbc.gridx = 0;
     gbc.gridy = 4;
     panel.add(natureLabel, gbc);
     gbc.gridx = 1;
     panel.add(natureComboBox, gbc);
 
     gbc.gridx = 0;
     gbc.gridy = 5;
     panel.add(imageNameLabel, gbc);
     gbc.gridx = 1;
     panel.add(imageNameField, gbc);
 
     gbc.gridx = 0;
     gbc.gridy = 6;
     gbc.gridwidth = 2; // Span across two columns
     panel.add(addButton, gbc);
     
     // Add panel to dialog
     addPetDialog.add(panel);
     addPetDialog.setVisible(true);
 }
 
 private void addNewPetToDatabase(String petName, String breed, int age, String type, String nature, String imageName) {
     try (Connection conn = getConnection(); // Get a connection
          PreparedStatement ps = conn.prepareStatement("INSERT INTO pet_details (pet_name, breed, age, type, nature, image_name) VALUES (?, ?, ?, ?, ?, ?)")) {
         
         ps.setString(1, petName);
         ps.setString(2, breed);
         ps.setInt(3, age);
         ps.setString(4, type);
         ps.setString(5, nature);
         ps.setString(6, imageName);
         
         int rowsAffected = ps.executeUpdate();
         if (rowsAffected > 0) {
             JOptionPane.showMessageDialog(this, "Pet added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
         } else {
             JOptionPane.showMessageDialog(this, "Failed to add pet.", "Error", JOptionPane.ERROR_MESSAGE);
         }
     } catch (SQLException e) {
         e.printStackTrace();
         JOptionPane.showMessageDialog(this, "Failed to add pet: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
     }
 }
 
 
    private void openDeletePetDialog() {
        JDialog deletePetDialog = new JDialog(this, "Delete Pet", true);
        deletePetDialog.setSize(400, 300);
        deletePetDialog.setLocationRelativeTo(this);

        JTable petTable = new JTable();
        DefaultTableModel petTableModel = new DefaultTableModel(new String[]{"Pet ID", "Pet Name", "Type"}, 0);
        petTable.setModel(petTableModel);

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_PETS)) {

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("pet_id"),
                    rs.getString("pet_name"),
                    rs.getString("type")
                };
                petTableModel.addRow(row);
            }
        } catch (SQLException e) {
            showError("Failed to fetch pets.");
        }

        deletePetDialog.add(new JScrollPane(petTable), BorderLayout.CENTER);
        JButton confirmDeleteButton = new JButton("Confirm Delete");
        confirmDeleteButton.addActionListener(e -> {
            int selectedRow = petTable.getSelectedRow();
            if (selectedRow != -1) {
                int petId = (int) petTableModel.getValueAt(selectedRow, 0);
                deletePet(petId);
                deletePetDialog.dispose();
            } else {
                showError("Please select a pet to delete.");
            }
        });
        deletePetDialog.add(confirmDeleteButton, BorderLayout.SOUTH);
        deletePetDialog.setVisible(true);
    }

    private void deletePet(int petId) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_PET)) {
            ps.setInt(1, petId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Pet deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            showError("Failed to delete pet.");
        }
    }

    private void deleteSelectedRequest() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a request to delete.");
            return;
        }

        int requestId = (int) tableModel.getValueAt(selectedRow, 0);

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_ADOPTION_REQUEST)) {
            ps.setInt(1, requestId);
            ps.executeUpdate();
            tableModel.removeRow(selectedRow);
            JOptionPane.showMessageDialog(this, "Request deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            showError("Failed to delete adoption request.");
        }
    }

    private void deleteAllRequests() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete all adoption requests?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(DELETE_ALL_ADOPTION_REQUESTS)) {
                ps.executeUpdate();
                tableModel.setRowCount(0); // Clear the table
                JOptionPane.showMessageDialog(this, "All requests deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                showError("Failed to delete all requests.");
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminAdoptionPage::new);
    }
}
