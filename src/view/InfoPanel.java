package view;

import model.Pharmacie;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable; // Importer Serializable si Pharmacie l'est

public class InfoPanel extends JPanel {
    private Pharmacie pharmacie;
    private PharmacieDataListener dataListener;

    private JTextField nomPharmacieField;
    private JTextArea adressePharmacieArea;
    private JButton enregistrerButton;
    private JLabel messageLabel;

    public InfoPanel(Pharmacie pharmacie, PharmacieDataListener listener) {
        this.pharmacie = pharmacie;
        this.dataListener = listener;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titre du panneau
        JLabel titleLabel = new JLabel("Informations de la Pharmacie");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(titleLabel, gbc);

        int row = 1;

        // Nom de la pharmacie
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Nom de la Pharmacie:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        nomPharmacieField = new JTextField(30);
        add(nomPharmacieField, gbc);
        row++;

        // Adresse de la pharmacie
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHEAST; // Aligner en haut à droite pour JTextArea
        add(new JLabel("Adresse:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        adressePharmacieArea = new JTextArea(3, 30); // 3 lignes, 30 colonnes
        adressePharmacieArea.setLineWrap(true);
        adressePharmacieArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(adressePharmacieArea);
        add(scrollPane, gbc);
        row++;

        // Bouton Enregistrer
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        enregistrerButton = new JButton("Enregistrer les modifications");
        enregistrerButton.addActionListener(_ -> enregistrerModifications());
        add(enregistrerButton, gbc);
        row++;

        // Message Label
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        messageLabel = new JLabel(" ");
        messageLabel.setForeground(Color.BLUE);
        add(messageLabel, gbc);

        // Mettre à jour les champs avec les données actuelles au chargement
        updatePharmacyInfo();
    }

    /**
     * Met à jour les champs du panneau avec les informations actuelles de la Pharmacie.
     * Appelé lors de l'initialisation du panneau et quand les données sont rafraîchies.
     */
    public void updatePharmacyInfo() {
        nomPharmacieField.setText(pharmacie.getNom());
        adressePharmacieArea.setText(pharmacie.getAdresse());
        messageLabel.setText(" "); // Efface tout message précédent
    }

    private void enregistrerModifications() {
        String nouveauNom = nomPharmacieField.getText().trim();
        String nouvelleAdresse = adressePharmacieArea.getText().trim();

        if (nouveauNom.isEmpty() || nouvelleAdresse.isEmpty()) {
            messageLabel.setText("Le nom et l'adresse de la pharmacie ne peuvent pas être vides.");
            messageLabel.setForeground(Color.RED);
            return;
        }

        pharmacie.setNom(nouveauNom);
        pharmacie.setAdresse(nouvelleAdresse);
        
        // Comme ces informations sont stockées dans le fichier de sérialisation,
        // nous allons appeler la sauvegarde explicitement.
        pharmacie.sauvegarderDansFichier("pharmacie.ser"); // Sauvegarde les infos de base

        messageLabel.setText("Informations de la pharmacie enregistrées !");
        messageLabel.setForeground(Color.GREEN);
        
        // Notifier la MainFrame que les données de la pharmacie ont changé (pour rafraîchir d'autres panels si nécessaire)
        if (dataListener != null) {
            dataListener.onPharmacieDataChanged();
        }
    }
}
