package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdoptionForm extends JFrame {
    private String username;

    public AdoptionForm(String[] petInfo, String username) {
        this.username = username; 
        setTitle("Adoption Form for " + petInfo[0]);
        setSize(700, 500);
        setLocationRelativeTo(null); // Center the window
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close this window but keep the main app running

        // Create the form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add form fields
        formPanel.add(new JLabel("Adopting Pet:"));
        JLabel petNameLabel = new JLabel(petInfo[0]); // Pet name from the clicked pet panel
        formPanel.add(petNameLabel);

        formPanel.add(new JLabel("Your Name:"));
        JTextField nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Your Email:"));
        JTextField emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Phone Number:"));
        JTextField phoneField = new JTextField();
        formPanel.add(phoneField);

        formPanel.add(new JLabel("Government ID Number:"));
        JTextField govIdField = new JTextField();
        formPanel.add(govIdField);

        formPanel.add(new JLabel("Address:"));
        JTextArea addressArea = new JTextArea(2, 20);
        formPanel.add(new JScrollPane(addressArea));

        // Submit button
        JButton submitButton = new JButton("Submit Adoption");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String email = emailField.getText();
                String phone = phoneField.getText();
                String govId = govIdField.getText();
                String address = addressArea.getText();

                // Validate inputs
                if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                    JOptionPane.showMessageDialog(AdoptionForm.this, "Please fill in all the fields.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // Save data to the database
                    saveAdoptionRequest(petInfo[0], name, email, phone, govId, address);
                    // Show a confirmation message
                    JOptionPane.showMessageDialog(AdoptionForm.this, "Thank you for showing interest in " + petInfo[0] + "!", "Adoption Request Sent", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); // Close the form after successful submission
                }
            }
        });

        // Add the panel and button to the frame
        add(formPanel, BorderLayout.CENTER);
        add(submitButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void saveAdoptionRequest(String petName, String adopterName, String email, String phoneNumber, String govIdNumber, String address) {
        String url = "jdbc:mysql://localhost:3306/pet_adoption_db";
        String dbUsername = "root"; // Replace with your MySQL username
        String dbPassword = "password"; // Replace with your MySQL password

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            // Fetch the user ID based on the username
            String userIdQuery = "SELECT user_id FROM users WHERE username = ?";
            try (PreparedStatement userIdStmt = connection.prepareStatement(userIdQuery)) {
                userIdStmt.setString(1, this.username);
                try (ResultSet userIdRs = userIdStmt.executeQuery()) {
                    if (userIdRs.next()) {
                        int userId = userIdRs.getInt("user_id");

                        // Insert the adoption request with the user's ID
                        String query = "INSERT INTO adoption_requests (pet_name, adopter_name, email, phone_number, gov_id_number, address, user_id, request_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                            preparedStatement.setString(1, petName);
                            preparedStatement.setString(2, adopterName);
                            preparedStatement.setString(3, email);
                            preparedStatement.setString(4, phoneNumber);
                            preparedStatement.setString(5, govIdNumber);
                            preparedStatement.setString(6, address);
                            preparedStatement.setInt(7, userId); // Set the user ID in the adoption request
                            preparedStatement.setString(8, "Pending");

                            preparedStatement.executeUpdate();
                        }
                    } else {
                        // Handle case where user is not found
                        JOptionPane.showMessageDialog(this, "User not found!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
