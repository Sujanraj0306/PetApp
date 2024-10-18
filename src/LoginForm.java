package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginForm extends JFrame {

    public LoginForm() {
        // Set frame properties
        setTitle("Pet Adoption - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // Create panel for login form
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Username label and field
        panel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField();
        panel.add(usernameField);

        // Password label and field
        panel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField();
        panel.add(passwordField);

        // Login button
        JButton loginButton = new JButton("Login");
        panel.add(loginButton);

        // Sign up button
        JButton signUpButton = new JButton("Sign Up");
        panel.add(signUpButton);

        add(panel, BorderLayout.CENTER);

        // Action listener for login
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (authenticateUser(username, password)) {
                    new PetAdoptionPage(username); // Pass the username to PetAdoptionPage
                    dispose(); // Close the login form
                } else {
                    JOptionPane.showMessageDialog(LoginForm.this, "Invalid username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Action listener for sign up
        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SignUpForm();
                dispose();
            }
        });

        setVisible(true);
    }

    private boolean authenticateUser(String username, String password) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/pet_adoption_db", "root", "password");
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true; // User authenticated
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // User not authenticated
    }

    public static void main(String[] args) {
        new LoginForm();
    }
}
