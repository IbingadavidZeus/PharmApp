package view;

import model.Pharmacie;
import model.Utilisateur;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException; // Import SQLException

public class LoginPanel extends JPanel {
    private Pharmacie pharmacie;
    private LoginListener listener; // Interface pour notifier MainFrame
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel messageLabel;

    public LoginPanel(Pharmacie pharmacie, LoginListener listener) {
        this.pharmacie = pharmacie;
        this.listener = listener;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Marges entre les composants

        // Titre
        JLabel titleLabel = new JLabel("Connexion Pharmacie");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span across two columns
        add(titleLabel, gbc);

        // Nom d'utilisateur
        gbc.gridwidth = 1; // Reset gridwidth
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST; // Align text to the right
        add(new JLabel("Nom d'utilisateur:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST; // Align field to the left
        usernameField = new JTextField(20);
        add(usernameField, gbc);

        // Mot de passe
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Mot de passe:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        passwordField = new JPasswordField(20);
        add(passwordField, gbc);

        // Bouton de connexion
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // Span across two columns
        gbc.anchor = GridBagConstraints.CENTER; // Center the button
        loginButton = new JButton("Se connecter");
        loginButton.addActionListener(_ -> attemptLogin());
        add(loginButton, gbc);

        // Message label
        gbc.gridy = 4;
        messageLabel = new JLabel(" ");
        messageLabel.setForeground(Color.RED);
        add(messageLabel, gbc);
    }

    private void attemptLogin() {
        String username = usernameField.getText();
        char[] passwordChars = passwordField.getPassword(); // Get password as char array

        Utilisateur user = null;
        try {
            user = pharmacie.authentifierUtilisateur(username, new String(passwordChars)); // For now, convert to String as per existing Pharmacie method
            
           for (int i = 0; i < passwordChars.length; i++) {
                passwordChars[i] = 0; 
            }

            if (user != null) {
                messageLabel.setText("Connexion réussie !");
                messageLabel.setForeground(Color.GREEN);
                if (listener != null) {
                    listener.onLoginSuccess(user);
                }
            } else {
                messageLabel.setText("Nom d'utilisateur ou mot de passe incorrect.");
                messageLabel.setForeground(Color.RED);
                passwordField.setText(""); // Efface le champ mot de passe
            }
        } catch (SQLException e) {
            // Handle database-related errors
            messageLabel.setText("Erreur de connexion à la base de données: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace(); // Log the full stack trace for debugging
        } finally {
            // Ensure the char array is cleared even if an exception occurs
            if (passwordChars != null) {
                for (int i = 0; i < passwordChars.length; i++) {
                    passwordChars[i] = 0;
                }
            }
        }
    }
}