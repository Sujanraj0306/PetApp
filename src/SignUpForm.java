package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class SignUpForm extends JFrame {

    public SignUpForm() {
        setTitle("Sign Up");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField();
        panel.add(passwordField);

        panel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        panel.add(emailField);

        panel.add(new JLabel("Phone Number:"));
        JTextField phoneField = new JTextField();
        panel.add(phoneField);

        panel.add(new JLabel("Address:"));
        JTextArea addressArea = new JTextArea();
        panel.add(new JScrollPane(addressArea));

        JButton signUpButton = new JButton("Sign Up");
        panel.add(signUpButton);

        add(panel, BorderLayout.CENTER);

        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String email = emailField.getText();
                String phone = phoneField.getText();
                String address = addressArea.getText();

                if (registerUser(username, password, email, phone, address)) {
                    JOptionPane.showMessageDialog(SignUpForm.this, "User registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    new LoginForm();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(SignUpForm.this, "Error in registration.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setVisible(true);
    }

    private boolean registerUser(String username, String password, String email, String phone, String address) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/pet_adoption_db", "root", "password");
            String query = "INSERT INTO users (username, password, email, phone_number, address) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setString(5, address);

            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
