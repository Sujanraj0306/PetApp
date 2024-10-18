package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class PetAdoptionPage extends JFrame {

    private ArrayList<String[]> petData = new ArrayList<>();
    private JPanel centerPanel;
    private JScrollPane scrollPane;
    private String username;

    // Combo boxes for advanced filters
    private JComboBox<String> breedFilter;
    private JComboBox<String> typeFilter;
    private JComboBox<String> natureFilter; // Declared at class level

    public PetAdoptionPage(String username) {
        // Fetch data from the database view
        fetchDataFromDatabase();
        this.username = username;
        setTitle("Welcome " + username);
        // Set frame properties
        setTitle("Pet Adoption E-Commerce");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // Main panel with black background
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.BLACK);
        mainPanel.setLayout(new BorderLayout());

        // Header panel with white text and red background
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(192, 57, 43)); // Darker Red
        JLabel headerLabel = new JLabel("Welcome to Pet Adoption!");
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerPanel.add(headerLabel);

        // Advanced Filter Panel as a navigation bar
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // Align to the left
        filterPanel.setBackground(new Color(192, 57, 43)); // Darker Red

        // Pet type dropdown
        filterPanel.add(new JLabel("Type:"));
        typeFilter = new JComboBox<>(new String[]{"All", "Dog", "Cat", "Others"}); // Add more types as needed
        filterPanel.add(typeFilter);

        // Pet nature dropdown
        filterPanel.add(new JLabel("Nature:"));
        natureFilter = new JComboBox<>(new String[]{"All", "Gentle", "Energetic", "Curious", "Loyal", "Protective", "Talkative", "Skittish", "Friendly", "Calm", "Playful"}); // Now class-level variable
        filterPanel.add(natureFilter);

        // Filter button
        JButton filterButton = new JButton("Apply Filter");
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilters(); // Call method to apply filters
            }
        });
        filterPanel.add(filterButton);

        JButton statusButton = new JButton("Adoption Status");
        statusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AdoptionStatusPage(username); // Open the adoption status page for the logged-in user
            }
        });
        filterPanel.add(statusButton);

        // **Log Out Button**
        JButton logOutButton = new JButton("Log Out");
        logOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logOut(); // Log out and return to login page
            }
        });
        filterPanel.add(logOutButton); // Add the log out button to the filter panel

        // Center panel for displaying pet images and details
        centerPanel = new JPanel();
        centerPanel.setBackground(Color.BLACK);
        centerPanel.setLayout(new GridLayout(7, 3, 10, 10)); // GridLayout for 21 pets

        // Adding pets dynamically to the center panel
        updatePetDisplay(); // Populate the center panel with pet information

        // Adding the center panel to a JScrollPane for scrolling functionality
        scrollPane = new JScrollPane(centerPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(1000, 600)); // Set preferred size for scrolling

        // Footer panel with some information
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(192, 57, 43)); // Darker Red
        JLabel footerLabel = new JLabel("Find your perfect pet today!");
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        footerPanel.add(footerLabel);

        // Adding panels to the main frame
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(filterPanel, BorderLayout.NORTH); // Add the filter panel above the scrollable area
        mainPanel.add(scrollPane, BorderLayout.CENTER); // Add the scrollable panel
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        add(mainPanel);

        // Set the frame visible
        setVisible(true);
    }

    // Method to log out the user and return to the login form
    private void logOut() {
        // Close current window and open the login form
        new LoginForm(); // Redirect to login form
        dispose(); // Close the PetAdoptionPage
    }

    // Method to update the pet display
    private void updatePetDisplay() {
        centerPanel.removeAll(); // Clear previous components
        for (int i = 0; i < petData.size(); i++) {
            centerPanel.add(createPetPanel(i)); // Create and add a new pet panel
        }
        centerPanel.revalidate(); // Refresh the panel to show new components
        centerPanel.repaint(); // Repaint the panel to reflect changes
    }

    // Method to create a pet panel
    private JPanel createPetPanel(int index) {
        JPanel petPanel = new JPanel();
        petPanel.setBackground(Color.WHITE);
        petPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding
        petPanel.setLayout(new BorderLayout());
        petPanel.setPreferredSize(new Dimension(300, 300)); // Set preferred size for each pet panel

        // Load the pet image from the images folder
        ImageIcon petImageIcon = new ImageIcon("image/pet" + (index % 10 + 1) + ".jpg"); // Using same images
        Image petImage = petImageIcon.getImage(); // Get the image
        Image scaledPetImage = petImage.getScaledInstance(200, 200, Image.SCALE_SMOOTH); // Scale the image
        JLabel imageLabel = new JLabel(new ImageIcon(scaledPetImage));
        petPanel.add(imageLabel, BorderLayout.CENTER);

        // Pet name and buttons
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(192, 57, 43)); // Darker Red
        String[] petInfo = petData.get(index);
        JLabel petName = new JLabel(petInfo[0] + " (" + petInfo[3] + ")");
        petName.setForeground(Color.WHITE);
        petName.setFont(new Font("Arial", Font.BOLD, 16));

        JButton moreInfoButton = new JButton("More Info");
        moreInfoButton.setForeground(new Color(231, 76, 60));
        moreInfoButton.setBackground(new Color(231, 76, 60)); // Lighter Red
        moreInfoButton.setFocusPainted(false); // Remove focus border
        moreInfoButton.setBorderPainted(true);
        moreInfoButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Action for More Info button
        int petIndex = index;
        moreInfoButton.addActionListener(e -> showPetDetails(petIndex));

        JButton adoptButton = new JButton("Adopt");
        adoptButton.setForeground(new Color(231, 76, 60));
        adoptButton.setBackground(new Color(231, 76, 60)); // Lighter Red
        adoptButton.setFocusPainted(false); // Remove focus border
        adoptButton.setBorderPainted(true);
        adoptButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Action for Adopt button
        adoptButton.addActionListener(e -> openAdoptionForm(petIndex));

        infoPanel.add(petName);
        infoPanel.add(moreInfoButton);
        infoPanel.add(adoptButton);
        petPanel.add(infoPanel, BorderLayout.SOUTH);

        return petPanel;
    }

    // Method to show pet details in a dialog
    private void showPetDetails(int index) {
        String[] petInfo = petData.get(index);
        String details = "Name: " + petInfo[0] + "\n"
                + "Breed: " + petInfo[1] + "\n"
                + "Age: " + petInfo[2] + "\n"
                + "Type: " + petInfo[3] + "\n"
                + "Nature: " + petInfo[4];

        JOptionPane.showMessageDialog(this, details, "Pet Details", JOptionPane.INFORMATION_MESSAGE);
    }

    // Method to apply filters and update the pet display
    private void applyFilters() {
        String selectedType = typeFilter.getSelectedItem().toString();
        String selectedNature = natureFilter.getSelectedItem().toString(); // Get selected nature

        centerPanel.removeAll(); // Clear the panel for new filtered results

        for (int i = 0; i < petData.size(); i++) {
            String[] pet = petData.get(i);
            boolean matches = true;

            // Check for pet type filter
            if (selectedType.equals("Dog") && !pet[3].equalsIgnoreCase("Dog")) {
                matches = false;
            } else if (selectedType.equals("Cat") && !pet[3].equalsIgnoreCase("Cat")) {
                matches = false;
            } else if (selectedType.equals("Others") && (pet[3].equalsIgnoreCase("Dog") || pet[3].equalsIgnoreCase("Cat"))) {
                matches = false;
            }

            // Check for pet nature filter
            if (!selectedNature.equals("All") && !pet[4].equalsIgnoreCase(selectedNature)) {
                matches = false;
            }

            // Add to display if it matches the filters
            if (matches) {
                centerPanel.add(createPetPanel(i));
            }
        }

        centerPanel.revalidate(); // Refresh the panel to show filtered components
        centerPanel.repaint(); // Repaint the panel to reflect changes
    }

    // Method to open the adoption form
    private void openAdoptionForm(int petIndex) {
        // Pass the username or user ID when opening the adoption form
        new AdoptionForm(petData.get(petIndex), this.username); // Pass username
    }

    // Method to fetch data from the database view
    private void fetchDataFromDatabase() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/pet_adoption_db", "root", "password");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM pet_view");

            while (rs.next()) {
                String[] petInfo = new String[5];
                petInfo[0] = rs.getString("pet_name");
                petInfo[1] = rs.getString("breed");
                petInfo[2] = rs.getString("age");
                petInfo[3] = rs.getString("type");
                petInfo[4] = rs.getString("nature");
                petData.add(petInfo); // Store pet information in the list
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Assume username is passed from another part of your application
        String username = "User"; // Replace with actual username
        new PetAdoptionPage(username); // Create and show the pet adoption page
    }
}
