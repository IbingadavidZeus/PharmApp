package view;

import model.Pharmacie;
import model.Utilisateur;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays; 

public class LoginPanel extends JPanel {
    private Pharmacie pharmacie;
    private LoginListener listener; 
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
        gbc.insets = new Insets(10, 10, 10, 10);

        // Titre
        JLabel titleLabel = new JLabel("Connexion Pharmacie");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; 
        add(titleLabel, gbc);

        // Nom d'utilisateur
        gbc.gridwidth = 1; 
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Nom d'utilisateur:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST; 
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
        gbc.gridwidth = 2; 
        gbc.anchor = GridBagConstraints.CENTER;
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
        char[] passwordChars = passwordField.getPassword(); 
        String plainPassword = new String(passwordChars); 

        Utilisateur user = null;
        try {
            user = pharmacie.authentifier(username, plainPassword); 
            
            if (user != null) {
                messageLabel.setText("Connexion réussie !");
                messageLabel.setForeground(Color.GREEN);
                if (listener != null) {
                    listener.onLoginSuccess(user);
                }
            } else {
                messageLabel.setText("Nom d'utilisateur ou mot de passe incorrect.");
                messageLabel.setForeground(Color.RED);
                passwordField.setText(""); 
            }
        } catch (Exception e) { 
            messageLabel.setText("Une erreur inattendue est survenue: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace();
        } finally {
            if (passwordChars != null) {
                Arrays.fill(passwordChars, ' '); 
            }
        }
    }
}
