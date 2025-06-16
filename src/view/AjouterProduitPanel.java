package view;

import model.Pharmacie;
import model.Produit;
import model.Medicament;
import model.ProduitParaPharmacie;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener; // Importation pour ActionListener

public class AjouterProduitPanel extends JPanel {
    private Pharmacie pharmacie;
    private JTextField nomField;
    private JTextField referenceField;
    private JTextArea descriptionArea; // Utilisation de JTextArea pour la description (peut être longue)
    private JTextField prixHtField;
    private JTextField quantiteField;
    private JComboBox<String> typeProduitComboBox; // Pour choisir Medicament ou Parapharmacie

    // Champs spécifiques aux médicaments
    private JCheckBox generiqueCheckBox;
    private JCheckBox ordonnanceCheckBox;

    // Champs spécifiques à la parapharmacie
    private JTextField categorieParaField;

    private JPanel specificFieldsPanel; // Panel pour changer dynamiquement les champs

    private JButton ajouterButton;
    private JLabel messageLabel;
    private PharmacieDataListener dataListener; // Pour notifier la MainFrame des changements

    public AjouterProduitPanel(Pharmacie pharmacie, PharmacieDataListener listener) {
        this.pharmacie = pharmacie;
        this.dataListener = listener; // Initialisation du listener
        setLayout(new BorderLayout(10, 10)); // Utilisation d'un BorderLayout pour une meilleure structure

        // Panel principal pour le formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Espacement entre les composants
        gbc.fill = GridBagConstraints.HORIZONTAL; // Les champs s'étendent horizontalement

        int row = 0;

        // Nom du produit
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Nom:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        nomField = new JTextField(20);
        formPanel.add(nomField, gbc);
        row++;

        // Référence
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Référence:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        referenceField = new JTextField(20);
        formPanel.add(referenceField, gbc);
        row++;

        // NOUVEAU: Description du produit
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTHEAST; // Aligner en haut à droite pour JTextArea
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        descriptionArea = new JTextArea(3, 20); // 3 lignes, 20 colonnes
        descriptionArea.setLineWrap(true); // Permet le retour à la ligne automatique
        descriptionArea.setWrapStyleWord(true); // Coupe les lignes aux mots
        JScrollPane scrollPane = new JScrollPane(descriptionArea); // Ajoute une barre de défilement
        formPanel.add(scrollPane, gbc);
        row++;

        // Prix HT
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Prix HT:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        prixHtField = new JTextField(20);
        formPanel.add(prixHtField, gbc);
        row++;

        // Quantité
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Quantité:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        quantiteField = new JTextField(20);
        formPanel.add(quantiteField, gbc);
        row++;

        // Type de produit (ComboBox)
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Type de produit:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        typeProduitComboBox = new JComboBox<>(new String[]{"Medicament", "Parapharmacie"});
        typeProduitComboBox.addActionListener(_ -> updateSpecificFieldsPanel());
        formPanel.add(typeProduitComboBox, gbc);
        row++;

        // Panel pour les champs spécifiques (dynamique)
        specificFieldsPanel = new JPanel(new GridBagLayout()); // Utilise un GridBagLayout interne
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; // Ce panel prend 2 colonnes
        formPanel.add(specificFieldsPanel, gbc);
        row++;

        // Initialisation des champs spécifiques (cachés au début)
        generiqueCheckBox = new JCheckBox("Générique");
        ordonnanceCheckBox = new JCheckBox("Sur ordonnance");
        categorieParaField = new JTextField(20);

        // Appel initial pour afficher les champs du médicament par défaut
        updateSpecificFieldsPanel();

        // Bouton Ajouter
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        ajouterButton = new JButton("Ajouter Produit");
        ajouterButton.addActionListener(_ -> ajouterProduit());
        formPanel.add(ajouterButton, gbc);
        row++;

        // Message Label
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        messageLabel = new JLabel(" ");
        messageLabel.setForeground(Color.BLUE);
        formPanel.add(messageLabel, gbc);

        add(formPanel, BorderLayout.CENTER); // Ajoute le formulaire au centre du panel principal
        
        // Ajouter une bordure au panel pour l'esthétique
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void updateSpecificFieldsPanel() {
        specificFieldsPanel.removeAll(); // Supprime tous les anciens composants
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST; // Alignement des composants spécifiques

        String selectedType = (String) typeProduitComboBox.getSelectedItem();
        int innerRow = 0;

        if ("Medicament".equals(selectedType)) {
            gbc.gridx = 0; gbc.gridy = innerRow++;
            specificFieldsPanel.add(generiqueCheckBox, gbc);
            gbc.gridx = 0; gbc.gridy = innerRow++;
            specificFieldsPanel.add(ordonnanceCheckBox, gbc);
        } else if ("Parapharmacie".equals(selectedType)) {
            gbc.gridx = 0; gbc.gridy = innerRow; gbc.anchor = GridBagConstraints.EAST;
            specificFieldsPanel.add(new JLabel("Catégorie Parapharmacie:"), gbc); // Label plus explicite
            gbc.gridx = 1; gbc.gridy = innerRow; gbc.anchor = GridBagConstraints.WEST;
            specificFieldsPanel.add(categorieParaField, gbc);
        }

        specificFieldsPanel.revalidate(); // Revalider le layout pour prendre en compte les changements
        specificFieldsPanel.repaint(); // Redessiner le panel pour afficher les nouveaux composants
    }

    private void ajouterProduit() {
        try {
            // Récupération des champs communs à tous les produits
            String nom = nomField.getText().trim();
            String reference = referenceField.getText().trim();
            String description = descriptionArea.getText().trim(); // Récupération de la description
            double prixHt = Double.parseDouble(prixHtField.getText().trim());
            int quantite = Integer.parseInt(quantiteField.getText().trim());
            String typeProduit = (String) typeProduitComboBox.getSelectedItem();

            // Validation de base
            if (nom.isEmpty() || reference.isEmpty() || description.isEmpty()) {
                messageLabel.setText("Erreur: Tous les champs obligatoires (Nom, Référence, Description) doivent être remplis.");
                messageLabel.setForeground(Color.RED);
                return;
            }
            if (prixHt < 0) {
                messageLabel.setText("Erreur: Le prix HT ne peut pas être négatif.");
                messageLabel.setForeground(Color.RED);
                return;
            }
            if (quantite < 0) {
                messageLabel.setText("Erreur: La quantité ne peut pas être négative.");
                messageLabel.setForeground(Color.RED);
                return;
            }

            Produit nouveauProduit = null;

            // Création de l'objet Produit selon le type sélectionné
            if ("Medicament".equals(typeProduit)) {
                boolean generique = generiqueCheckBox.isSelected();
                boolean ordonnance = ordonnanceCheckBox.isSelected();
                // Utilise le constructeur Medicament mis à jour avec la description
                nouveauProduit = new Medicament(nom, reference, description, prixHt, quantite, generique, ordonnance);
            } else if ("Parapharmacie".equals(typeProduit)) {
                String categorie = categorieParaField.getText().trim();
                if (categorie.isEmpty()) {
                    messageLabel.setText("Erreur: La catégorie pour la parapharmacie est obligatoire.");
                    messageLabel.setForeground(Color.RED);
                    return;
                }
                // Utilise le constructeur ProduitParaPharmacie mis à jour avec la description
                nouveauProduit = new ProduitParaPharmacie(nom, reference, description, prixHt, quantite, categorie);
            }

            if (nouveauProduit != null) {
                // Utilise la nouvelle méthode ajouterProduit de Pharmacie qui utilise le DAO
                boolean succes = pharmacie.ajouterProduit(nouveauProduit);
                if (succes) {
                    messageLabel.setText("Produit ajouté avec succès à la base de données !");
                    messageLabel.setForeground(Color.GREEN);
                    // Effacer les champs après l'ajout
                    nomField.setText("");
                    referenceField.setText("");
                    descriptionArea.setText(""); // Efface la description
                    prixHtField.setText("");
                    quantiteField.setText("");
                    generiqueCheckBox.setSelected(false);
                    ordonnanceCheckBox.setSelected(false);
                    categorieParaField.setText("");
                    typeProduitComboBox.setSelectedIndex(0); // Réinitialise le combo box à "Medicament"

                    // Notifier le listener que les données ont changé (pour rafraîchir d'autres vues)
                    if (dataListener != null) {
                        dataListener.onPharmacieDataChanged();
                    }
                } else {
                    messageLabel.setText("Erreur: Impossible d'ajouter le produit. Une référence similaire existe peut-être déjà ou un problème est survenu.");
                    messageLabel.setForeground(Color.RED);
                }
            } else {
                messageLabel.setText("Erreur: Type de produit non valide ou non spécifié.");
                messageLabel.setForeground(Color.RED);
            }

        } catch (NumberFormatException e) {
            messageLabel.setText("Erreur de saisie: Veuillez entrer des nombres valides pour le prix HT et la quantité.");
            messageLabel.setForeground(Color.RED);
        } catch (Exception e) { // Capture toutes les exceptions pour un feedback général
            messageLabel.setText("Une erreur inattendue est survenue: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace(); // Affiche la pile d'appels pour le débogage
        }
    }
}