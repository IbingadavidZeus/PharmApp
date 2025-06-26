package view;

import model.Pharmacie;
import model.Produit;
import model.Medicament;
import model.ProduitParaPharmacie;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;

public class AjouterProduitPanel extends JPanel {
    private Pharmacie pharmacie;
    private PharmacieDataListener dataListener;

    // Champs de formulaire
    private JTextField referenceField;
    private JTextField nomField;
    private JTextArea descriptionArea;
    private JFormattedTextField prixHtField;
    private JFormattedTextField quantiteField;
    private JComboBox<String> typeProduitComboBox;

    private JPanel typeSpecificPanel;
    private JPanel medicamentPanel;
    private JCheckBox generiqueCheckBox;
    private JCheckBox surOrdonnanceCheckBox;
    private JPanel parapharmaciePanel;
    private JTextField categorieParapharmacieField;

    // Boutons
    private JButton ajouterButton;
    private JButton resetButton;
    private JLabel messageLabel;

    public AjouterProduitPanel(Pharmacie pharmacie, PharmacieDataListener dataListener) {
        this.pharmacie = pharmacie;
        this.dataListener = dataListener;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Panel Principal du Formulaire (Centre) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Section Détails Généraux du Produit ---
        JPanel generalDetailsPanel = new JPanel(new GridBagLayout());
        generalDetailsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237)),
                "Détails Généraux du Produit",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), new Color(70, 130, 180)));
        GridBagConstraints gbcGen = new GridBagConstraints();
        gbcGen.insets = new Insets(5, 5, 5, 5);
        gbcGen.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbcGen.gridx = 0;
        gbcGen.gridy = row;
        gbcGen.anchor = GridBagConstraints.EAST;
        generalDetailsPanel.add(new JLabel("Référence:"), gbcGen);
        gbcGen.gridx = 1;
        gbcGen.gridy = row;
        gbcGen.weightx = 1.0;
        referenceField = new JTextField(20);
        generalDetailsPanel.add(referenceField, gbcGen);
        row++;

        gbcGen.gridx = 0;
        gbcGen.gridy = row;
        gbcGen.anchor = GridBagConstraints.EAST;
        generalDetailsPanel.add(new JLabel("Nom:"), gbcGen);
        gbcGen.gridx = 1;
        gbcGen.gridy = row;
        gbcGen.weightx = 1.0;
        nomField = new JTextField(20);
        generalDetailsPanel.add(nomField, gbcGen);
        row++;

        gbcGen.gridx = 0;
        gbcGen.gridy = row;
        gbcGen.anchor = GridBagConstraints.NORTHEAST;
        generalDetailsPanel.add(new JLabel("Description:"), gbcGen);
        gbcGen.gridx = 1;
        gbcGen.gridy = row;
        gbcGen.weightx = 1.0;
        gbcGen.weighty = 0.5;
        gbcGen.fill = GridBagConstraints.BOTH; // Permettre au JTextArea de s'étendre
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        generalDetailsPanel.add(scrollPane, gbcGen);
        row++;

        // Formatter pour les nombres (prix et quantité)
        NumberFormat priceFormat = new DecimalFormat("#0.00");
        NumberFormatter priceFormatter = new NumberFormatter(priceFormat);
        priceFormatter.setValueClass(Double.class);
        priceFormatter.setAllowsInvalid(false);
        priceFormatter.setMinimum(0.0);

        NumberFormat quantityFormat = NumberFormat.getIntegerInstance();
        NumberFormatter quantityFormatter = new NumberFormatter(quantityFormat);
        quantityFormatter.setValueClass(Integer.class);
        quantityFormatter.setAllowsInvalid(false);
        quantityFormatter.setMinimum(0);

        gbcGen.gridx = 0;
        gbcGen.gridy = row;
        gbcGen.anchor = GridBagConstraints.EAST;
        generalDetailsPanel.add(new JLabel("Prix HT:"), gbcGen);
        gbcGen.gridx = 1;
        gbcGen.gridy = row;
        gbcGen.weightx = 1.0;
        gbcGen.weighty = 0.0;
        gbcGen.fill = GridBagConstraints.HORIZONTAL;
        prixHtField = new JFormattedTextField(priceFormatter);
        prixHtField.setValue(0.00);
        generalDetailsPanel.add(prixHtField, gbcGen);
        row++;

        gbcGen.gridx = 0;
        gbcGen.gridy = row;
        gbcGen.anchor = GridBagConstraints.EAST;
        generalDetailsPanel.add(new JLabel("Quantité (Stock):"), gbcGen);
        gbcGen.gridx = 1;
        gbcGen.gridy = row;
        gbcGen.weightx = 1.0;
        quantiteField = new JFormattedTextField(quantityFormatter);
        quantiteField.setValue(0);
        generalDetailsPanel.add(quantiteField, gbcGen);
        row++;

        // Ajouter le panneau des détails généraux au panneau principal du formulaire
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.6;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(generalDetailsPanel, gbc);

        // --- Section Type de Produit et Spécificités ---
        JPanel typeSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typeSelectionPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        typeSelectionPanel.add(new JLabel("Type de Produit:"));
        typeProduitComboBox = new JComboBox<>(new String[] { "MEDICAMENT", "PARAPHARMACIE" });
        typeProduitComboBox.setPreferredSize(new Dimension(200, 30));
        typeProduitComboBox.addActionListener(e -> updateTypeSpecificFields());
        typeSelectionPanel.add(typeProduitComboBox);

        gbc.gridy = 1;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(typeSelectionPanel, gbc);

        // --- Panneau pour les champs spécifiques au type (avec CardLayout) ---
        typeSpecificPanel = new JPanel(new CardLayout());
        typeSpecificPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237)), "Spécificités du Type", TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), new Color(70, 130, 180)));

        // Panneau pour les champs Médicament
        medicamentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcMed = new GridBagConstraints();
        gbcMed.insets = new Insets(5, 5, 5, 5);
        gbcMed.anchor = GridBagConstraints.WEST;
        gbcMed.gridx = 0;
        gbcMed.gridy = 0;
        generiqueCheckBox = new JCheckBox("Est Générique");
        medicamentPanel.add(generiqueCheckBox, gbcMed);
        gbcMed.gridx = 0;
        gbcMed.gridy = 1;
        surOrdonnanceCheckBox = new JCheckBox("Est sur Ordonnance");
        medicamentPanel.add(surOrdonnanceCheckBox, gbcMed);
        typeSpecificPanel.add(medicamentPanel, "MEDICAMENT");

        // Panneau pour les champs Parapharmacie
        parapharmaciePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcPara = new GridBagConstraints();
        gbcPara.insets = new Insets(5, 5, 5, 5);
        gbcPara.fill = GridBagConstraints.HORIZONTAL;
        gbcPara.gridx = 0;
        gbcPara.gridy = 0;
        gbcPara.anchor = GridBagConstraints.EAST;
        parapharmaciePanel.add(new JLabel("Catégorie Parapharmacie:"), gbcPara);
        gbcPara.gridx = 1;
        gbcPara.gridy = 0;
        gbcPara.weightx = 1.0;
        gbcPara.anchor = GridBagConstraints.WEST;
        categorieParapharmacieField = new JTextField(20);
        parapharmaciePanel.add(categorieParapharmacieField, gbcPara);
        typeSpecificPanel.add(parapharmaciePanel, "PARAPHARMACIE");

        gbc.gridy = 2;
        gbc.weighty = 0.4;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(typeSpecificPanel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // --- Panel Boutons et Message (Sud) ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        ajouterButton = new JButton("Ajouter Produit");
        ajouterButton.setFont(new Font("Arial", Font.BOLD, 16));
        ajouterButton.setBackground(new Color(60, 179, 113));
        ajouterButton.setForeground(Color.WHITE);
        ajouterButton.addActionListener(e -> addProduct());
        buttonPanel.add(ajouterButton);

        resetButton = new JButton("Réinitialiser");
        resetButton.setFont(new Font("Arial", Font.PLAIN, 14));
        resetButton.setBackground(new Color(255, 165, 0));
        resetButton.setForeground(Color.WHITE);
        resetButton.addActionListener(e -> resetFields());
        buttonPanel.add(resetButton);

        bottomPanel.add(buttonPanel, BorderLayout.NORTH);

        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setForeground(Color.BLACK);
        bottomPanel.add(messageLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        updateTypeSpecificFields();
    }

    /**
     * Met à jour l'affichage des champs spécifiques au type de produit.
     * Utilise CardLayout pour basculer entre les panneaux "MEDICAMENT" et
     * "PARAPHARMACIE".
     */
    private void updateTypeSpecificFields() {
        String selectedType = (String) typeProduitComboBox.getSelectedItem();
        CardLayout cl = (CardLayout) (typeSpecificPanel.getLayout());
        cl.show(typeSpecificPanel, Objects.requireNonNull(selectedType)); // Utilise Objects.requireNonNull pour éviter
                                                                          // NullPointerException

        if ("MEDICAMENT".equals(selectedType)) {
            categorieParapharmacieField.setText("");
        } else if ("PARAPHARMACIE".equals(selectedType)) {
            generiqueCheckBox.setSelected(false);
            surOrdonnanceCheckBox.setSelected(false);
        }
    }

    /**
     * Ajoute un nouveau produit à la base de données.
     * Effectue des validations et gère la création du
     * Produit/Medicament/ProduitParaPharmacie.
     */
    private void addProduct() {
        String reference = referenceField.getText().trim();
        String nom = nomField.getText().trim();
        String description = descriptionArea.getText().trim();
        Double prixHt;
        Integer quantite;

        try {
            prixHt = ((Number) prixHtField.getValue()).doubleValue();
            quantite = ((Number) quantiteField.getValue()).intValue();
        } catch (ClassCastException | NullPointerException e) {
            setMessage("Veuillez entrer des valeurs numériques valides pour le prix et la quantité.", Color.RED);
            return;
        }

        if (reference.isEmpty() || nom.isEmpty() || prixHt <= 0 || quantite < 0) {
            setMessage(
                    "Veuillez remplir tous les champs obligatoires (Référence, Nom, Prix HT > 0) et assurez-vous que la quantité est positive.",
                    Color.RED);
            return;
        }

        if (description.isEmpty()) {
            setMessage("Veuillez entrer une description pour le produit.", Color.RED);
            return;
        }

        try {
            if (pharmacie.getProduitByReference(reference) != null) {
                setMessage(
                        "Erreur: La référence '" + reference + "' existe déjà. Veuillez utiliser une référence unique.",
                        Color.RED);
                return;
            }

            Produit produit;
            String selectedType = (String) typeProduitComboBox.getSelectedItem();

            if ("MEDICAMENT".equals(selectedType)) {
                boolean generique = generiqueCheckBox.isSelected();
                boolean surOrdonnance = surOrdonnanceCheckBox.isSelected();

                produit = new Medicament(nom, reference, description, prixHt, quantite, generique, surOrdonnance);
            } else if ("PARAPHARMACIE".equals(selectedType)) {
                String categorie = categorieParapharmacieField.getText().trim();
                if (categorie.isEmpty()) {
                    setMessage("Erreur: La catégorie pour la parapharmacie est obligatoire.", Color.RED);
                    return;
                }
                produit = new ProduitParaPharmacie(nom, reference, description, prixHt, quantite, categorie);
            } else {
                setMessage("Type de produit inconnu ou non sélectionné.", Color.RED);
                return;
            }

            boolean success = pharmacie.ajouterProduit(produit);

            if (success) {
                setMessage("Produit '" + nom + "' ajouté avec succès !", Color.GREEN);
                resetFields();
                if (dataListener != null) {
                    dataListener.onPharmacieDataChanged();
                }
            } else {
                setMessage("Échec de l'ajout du produit. Veuillez réessayer. La référence existe peut-être déjà.",
                        Color.RED);
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                setMessage("Erreur: La référence '" + reference + "' existe déjà dans la base de données.", Color.RED);
            } else {
                setMessage("Erreur de base de données lors de l'ajout: " + e.getMessage(), Color.RED);
            }
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            setMessage("Erreur de validation: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        } catch (Exception e) {
            setMessage("Une erreur inattendue est survenue lors de l'ajout du produit: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        }
    }

    /**
     * Réinitialise tous les champs du formulaire à leurs valeurs par défaut.
     */
    private void resetFields() {
        referenceField.setText("");
        nomField.setText("");
        descriptionArea.setText("");
        prixHtField.setValue(0.00);
        quantiteField.setValue(0);
        typeProduitComboBox.setSelectedIndex(0);
        generiqueCheckBox.setSelected(false);
        surOrdonnanceCheckBox.setSelected(false);
        categorieParapharmacieField.setText("");
        setMessage(" ", Color.BLACK);
    }

    /**
     * Affiche un message à l'utilisateur avec la couleur spécifiée.
     * 
     * @param msg   Le message à afficher.
     * @param color La couleur du texte du message.
     */
    private void setMessage(String msg, Color color) {
        messageLabel.setText(msg);
        messageLabel.setForeground(color);
    }
}
