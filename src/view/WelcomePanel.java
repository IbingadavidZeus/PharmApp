package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage; // Importation manquante ajoutée ici
import java.util.Objects;

public class WelcomePanel extends JPanel {
    private JLabel welcomeLabel;
    private JLabel mottoLabel;
    private JLabel iconLabel; // Label pour l'icône

    public WelcomePanel() {
        // Utilise un GridBagLayout pour un centrage et un espacement flexibles
        setLayout(new GridBagLayout());
        // Ajoutez une bordure vide pour un peu d'espace autour du contenu
        setBorder(new EmptyBorder(50, 50, 50, 50));

        // Personnalisation du fond avec un dégradé
        // Nous allons surcharger la méthode paintComponent pour dessiner un dégradé
        setOpaque(false); // Rendre le panneau non opaque pour que le dégradé s'affiche

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Chaque composant sur sa propre ligne
        gbc.anchor = GridBagConstraints.CENTER;      // Centrer les composants horizontalement
        gbc.insets = new Insets(15, 0, 15, 0);       // Espacement vertical entre les composants

        // 1. Icône de la pharmacie
        ImageIcon pharmacyIcon = null;
        try {
            Image img = new ImageIcon(Objects.requireNonNull(getClass().getResource("/image/app_icon.png"))).getImage();
            Image scaledImg = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH); // Taille de l'icône
            pharmacyIcon = new ImageIcon(scaledImg);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'icône de bienvenue: " + e.getMessage());
            // Utiliser une icône par défaut ou ne rien afficher si l'image n'est pas trouvée
            // Correction ici: Utilisation directe de BufferedImage
            pharmacyIcon = new ImageIcon(new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB)); // Icône vide transparente
        }
        iconLabel = new JLabel(pharmacyIcon);
        gbc.insets = new Insets(0, 0, 20, 0); // Plus d'espace après l'icône
        add(iconLabel, gbc);
        gbc.insets = new Insets(15, 0, 15, 0); // Restaurer l'espacement par défaut

        // 2. Message de bienvenue (déjà implémenté)
        welcomeLabel = new JLabel("Bienvenue dans votre système de gestion de pharmacie !");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(46, 139, 87));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(welcomeLabel, gbc);

        // 3. Slogan de la pharmacie (déjà implémenté)
        mottoLabel = new JLabel("Simplifiez votre gestion, optimisez votre service.");
        mottoLabel.setFont(new Font("Serif", Font.ITALIC, 18));
        mottoLabel.setForeground(new Color(70, 130, 180));
        mottoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(mottoLabel, gbc);
    }

    /**
     * Définit le message de bienvenue affiché sur le panneau.
     * @param message Le message à afficher.
     */
    public void setWelcomeMessage(String message) {
        welcomeLabel.setText(message);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int w = getWidth();
        int h = getHeight();
        
        Color color1 = new Color(245, 245, 250);
        Color color2 = new Color(220, 230, 245);
        
        GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
    }
}
