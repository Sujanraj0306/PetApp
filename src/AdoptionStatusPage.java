package src;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class AdoptionStatusPage extends JFrame {
    private String username;
    private JTextArea statusArea;

    public AdoptionStatusPage(String username) {
        this.username = username;
        setTitle("Adoption Status");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        statusArea = new JTextArea();
        statusArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(statusArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Fetch and display initial status
        refreshAdoptionStatus();

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshAdoptionStatus());
        panel.add(refreshButton, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    private void refreshAdoptionStatus() {
        statusArea.setText(""); // Clear current text
        ArrayList<String> statusList = fetchAdoptionStatus(username);
        for (String status : statusList) {
            statusArea.append(status + "\n\n"); // Append each status to the text area
        }
    }

    private ArrayList<String> fetchAdoptionStatus(String username) {
        ArrayList<String> statusList = new ArrayList<>();
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/pet_adoption_db", "root", "password");
            Statement stmt = conn.createStatement();
            String query = "SELECT ar.pet_name, ar.request_status, ar.request_date " +
                           "FROM adoption_requests ar " +
                           "JOIN users u ON ar.user_id = u.user_id " +
                           "WHERE u.username = '" + username + "'";

            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String petName = rs.getString("pet_name");
                String requestStatus = rs.getString("request_status");
                String requestDate = rs.getString("request_date");

                statusList.add("Pet: " + petName + "\nStatus: " + requestStatus + "\nRequest Date: " + requestDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusList; // Return the list of adoption statuses
    }
}
