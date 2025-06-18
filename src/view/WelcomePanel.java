package view;

import javax.swing.*;
import java.awt.*;

public class WelcomePanel extends JPanel {
    private JLabel welcomeLabel;

    public WelcomePanel() {
        setLayout(new GridBagLayout());
        welcomeLabel = new JLabel("Bienvenue dans le système de gestion de Pharmacie !");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(welcomeLabel);
    }

    public void setWelcomeMessage(String message) {
        welcomeLabel.setText(message);
    }
}