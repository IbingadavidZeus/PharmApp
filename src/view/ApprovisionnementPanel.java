package view;

import model.Approvisionnement;
import model.Fournisseur;
import model.LigneApprovisionnement;
import model.Pharmacie;
import model.Produit;
import model.Medicament; // Import pour Medicament si nécessaire dans le futur
import model.ProduitParaPharmacie; // Import pour ProduitParaPharmacie si nécessaire dans le futur

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID; // Pour générer des références uniques

public class ApprovisionnementPanel extends JPanel {
    private Pharmacie pharmacie;
    private PharmacieDataListener dataListener;

    // UI - Gestion du Fournisseur
    private JComboBox<Fournisseur> fournisseurComboBox;
    private JButton addFournisseurButton;
    private JTextField referenceBonCommandeField;

    // UI - Recherche et sélection de produits à ajouter à l'approvisionnement
    private JTextField searchField; // <-- Ce champ est ici
    private JButton searchButton;
    private JTable productSelectionTable;
    private DefaultTableModel productSelectionTableModel;
    private JTextField quantityToAddField;
    private JTextField prixUnitaireAchatField; // Nouveau champ pour le prix d'achat unitaire HT
    private JButton addProductToApproButton;

    // UI - Lignes de l'approvisionnement en cours
    private JTable currentApproLinesTable;
    private DefaultTableModel currentApproLinesTableModel;
    private JButton removeApproLineButton;
    private JButton clearApproButton;

    // UI - Totaux et Finalisation
    private JLabel totalHtLabel;
    private JLabel totalTvaLabel;
    private JLabel totalTtcLabel;
    private JButton finalizeApproButton;
    private JLabel messageLabel;

    // Modèle d'approvisionnement en cours de construction
    private Approvisionnement currentApprovisionnement;

    // Colonnes pour les tables
    private final String[] productSelectionColumns = {"ID", "Référence", "Nom", "Prix U. HT (Vente)", "Stock", "Type"};
    private final String[] currentApproLinesColumns = {"ID Produit", "Référence", "Nom Produit", "Qté", "Prix U. Achat HT", "Sous-Total HT"};

    public ApprovisionnementPanel(Pharmacie pharmacie, PharmacieDataListener dataListener) {
        this.pharmacie = pharmacie;
        this.dataListener = dataListener;
        this.currentApprovisionnement = new Approvisionnement(null, "TEMP-REF"); // Initialisation temporaire, sera remplacée
        initUI(); // <-- initUI est appelé ici
        refreshAllData(); // <-- refreshAllData est appelé ici
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- Panel de gestion Fournisseur et Référence BC (NORTH) ---
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(173, 216, 230)), // Bleu clair
            "Détails de l'Approvisionnement",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14), new Color(70, 130, 180)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fournisseur
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        headerPanel.add(new JLabel("Fournisseur:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        fournisseurComboBox = new JComboBox<>();
        fournisseurComboBox.setPreferredSize(new Dimension(200, 28));
        headerPanel.add(fournisseurComboBox, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0; // Reset weightx
        addFournisseurButton = new JButton("Ajouter Nouveau Fournisseur");
        addFournisseurButton.setBackground(new Color(95, 158, 160)); // Cadet Blue
        addFournisseurButton.setForeground(Color.WHITE);
        addFournisseurButton.addActionListener(e -> addNewFournisseur());
        headerPanel.add(addFournisseurButton, gbc);

        // Référence Bon de Commande
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        headerPanel.add(new JLabel("Référence Bon Commande:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.gridwidth = 2; // Span two columns
        referenceBonCommandeField = new JTextField(20);
        // Générer une référence par défaut au démarrage
        referenceBonCommandeField.setText("BC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
        headerPanel.add(referenceBonCommandeField, gbc);

        add(headerPanel, BorderLayout.NORTH);

        // --- SplitPane pour la sélection des produits et les lignes d'approvisionnement (CENTER) ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        // Panel de Sélection des Produits disponibles
        JPanel productSelectionPanel = new JPanel(new BorderLayout(5, 5));
        productSelectionPanel.setBorder(BorderFactory.createTitledBorder("Produits à Approvisionner"));
        
        // --- Composants de recherche dans le productSelectionPanel ---
        JPanel searchProductPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchProductPanel.add(new JLabel("Rechercher Produit:"));
        searchField = new JTextField(25); // <-- searchField est initialisé ici !
        searchProductPanel.add(searchField);
        searchButton = new JButton("Rechercher");
        searchButton.addActionListener(e -> {
            try {
                searchProducts();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la recherche des produits: " + ex.getMessage(),
                    "Erreur de Base de Données", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        searchProductPanel.add(searchButton);
        productSelectionPanel.add(searchProductPanel, BorderLayout.NORTH); // Ajouté au NORTH du productSelectionPanel

        productSelectionTableModel = new DefaultTableModel(productSelectionColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0 || column == 4) return Integer.class;
                if (column == 3) return Double.class;
                return Object.class;
            }
        };
        productSelectionTable = new JTable(productSelectionTableModel);
        productSelectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productSelectionTable.setAutoCreateRowSorter(true);
        productSelectionPanel.add(new JScrollPane(productSelectionTable), BorderLayout.CENTER); // Table au CENTER

        JPanel addProductPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        addProductPanel.add(new JLabel("Quantité:"));
        quantityToAddField = new JTextField("1", 5);
        addProductPanel.add(quantityToAddField);
        addProductPanel.add(new JLabel("Prix Achat HT:"));
        prixUnitaireAchatField = new JTextField("0.00", 8); // Default to 0.00
        addProductPanel.add(prixUnitaireAchatField);
        addProductToApproButton = new JButton("Ajouter au Bon de Commande");
        addProductToApproButton.setBackground(new Color(135, 206, 250)); // Light Sky Blue
        addProductToApproButton.setForeground(Color.BLACK);
        addProductToApproButton.addActionListener(e -> {
            try {
                addSelectedProductToAppro();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        addProductPanel.add(addProductToApproButton);
        productSelectionPanel.add(addProductPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(productSelectionPanel);

        // Panel des Lignes de l'approvisionnement en cours
        JPanel currentApproLinesPanel = new JPanel(new BorderLayout(5, 5));
        currentApproLinesPanel.setBorder(BorderFactory.createTitledBorder("Bon de Commande en Cours"));
        currentApproLinesTableModel = new DefaultTableModel(currentApproLinesColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0 || column == 3) return Integer.class;
                if (column == 4 || column == 5) return Double.class;
                return Object.class;
            }
        };
        currentApproLinesTable = new JTable(currentApproLinesTableModel);
        currentApproLinesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        currentApproLinesTable.setAutoCreateRowSorter(true);
        currentApproLinesPanel.add(new JScrollPane(currentApproLinesTable), BorderLayout.CENTER);

        JPanel approActionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        removeApproLineButton = new JButton("Retirer Ligne Sélectionnée");
        removeApproLineButton.setBackground(new Color(255, 99, 71)); // Tomato
        removeApproLineButton.setForeground(Color.WHITE);
        removeApproLineButton.addActionListener(e -> removeSelectedApproLine());
        clearApproButton = new JButton("Vider Bon de Commande");
        clearApproButton.setBackground(new Color(255, 215, 0)); // Gold
        clearApproButton.setForeground(Color.BLACK);
        clearApproButton.addActionListener(e -> clearCurrentApprovisionnement());
        approActionsPanel.add(removeApproLineButton);
        approActionsPanel.add(clearApproButton);
        currentApproLinesPanel.add(approActionsPanel, BorderLayout.SOUTH);

        splitPane.setRightComponent(currentApproLinesPanel);
        add(splitPane, BorderLayout.CENTER);

        // --- Panel des Totaux et Finalisation (SOUTH) ---
        JPanel footerPanel = new JPanel(new GridBagLayout());
        footerPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        footerPanel.add(new JLabel("Total HT:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        totalHtLabel = new JLabel("0.00 FCFA");
        totalHtLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalHtLabel.setForeground(Color.BLUE);
        footerPanel.add(totalHtLabel, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        footerPanel.add(new JLabel("TVA (18%):"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        totalTvaLabel = new JLabel("0.00 FCFA");
        totalTvaLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalTvaLabel.setForeground(new Color(34, 139, 34)); // Forest Green
        footerPanel.add(totalTvaLabel, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        footerPanel.add(new JLabel("Total TTC:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        totalTtcLabel = new JLabel("0.00 FCFA");
        totalTtcLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalTtcLabel.setForeground(new Color(255, 69, 0)); // Orange Red
        footerPanel.add(totalTtcLabel, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        finalizeApproButton = new JButton("Finaliser l'Approvisionnement");
        finalizeApproButton.setFont(new Font("Arial", Font.BOLD, 18));
        finalizeApproButton.setBackground(new Color(60, 179, 113)); // Medium Sea Green
        finalizeApproButton.setForeground(Color.WHITE);
        finalizeApproButton.addActionListener(e -> finalizeApprovisionnement());
        footerPanel.add(finalizeApproButton, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setForeground(Color.RED);
        footerPanel.add(messageLabel, gbc);

        add(footerPanel, BorderLayout.SOUTH);
    }

    /**
     * Rafraîchit toutes les données nécessaires au panneau :
     * - Table de sélection des produits.
     * - ComboBox des fournisseurs.
     * - Remise à zéro de l'approvisionnement en cours.
     */
    public void refreshAllData() {
        refreshAvailableProductsTable(); // This is called first
        loadFournisseursIntoComboBox();
        clearCurrentApprovisionnement(); // Commence avec un nouvel approvisionnement vide
    }

    /**
     * Charge tous les fournisseurs dans le JComboBox.
     */
    private void loadFournisseursIntoComboBox() {
        fournisseurComboBox.removeAllItems();
        try {
            List<Fournisseur> fournisseurs = pharmacie.getAllFournisseurs();
            for (Fournisseur f : fournisseurs) {
                fournisseurComboBox.addItem(f);
            }
            if (fournisseurComboBox.getItemCount() > 0) {
                fournisseurComboBox.setSelectedIndex(0);
            }
        } catch (SQLException e) {
            setMessage("Erreur lors du chargement des fournisseurs: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        }
    }

    /**
     * Ajoute un nouveau fournisseur via une boîte de dialogue simple.
     */
    private void addNewFournisseur() {
        JTextField nomField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JTextArea addressArea = new JTextArea(3, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane addressScrollPane = new JScrollPane(addressArea);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Nom Fournisseur:"));
        panel.add(nomField);
        panel.add(new JLabel("Personne Contact:"));
        panel.add(contactField);
        panel.add(new JLabel("Téléphone:"));
        panel.add(phoneField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Adresse:"));
        panel.add(addressScrollPane);

        int result = JOptionPane.showConfirmDialog(this, panel, "Ajouter Nouveau Fournisseur",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String nom = nomField.getText().trim();
            if (nom.isEmpty()) {
                setMessage("Le nom du fournisseur ne peut pas être vide.", Color.RED);
                return;
            }
            Fournisseur nouveauFournisseur = new Fournisseur(
                    nom,
                    contactField.getText().trim(),
                    phoneField.getText().trim(),
                    emailField.getText().trim(),
                    addressArea.getText().trim()
            );
            try {
                if (pharmacie.getFournisseurByNom(nom) != null) {
                    setMessage("Un fournisseur avec ce nom existe déjà.", Color.RED);
                    return;
                }
                boolean success = pharmacie.ajouterFournisseur(nouveauFournisseur);
                if (success) {
                    setMessage("Fournisseur '" + nom + "' ajouté avec succès.", Color.GREEN);
                    loadFournisseursIntoComboBox(); // Rafraîchir la liste des fournisseurs
                    fournisseurComboBox.setSelectedItem(nouveauFournisseur); // Sélectionner le nouveau
                    dataListener.onPharmacieDataChanged(); // Notifier les autres panels si besoin
                } else {
                    setMessage("Erreur lors de l'ajout du fournisseur.", Color.RED);
                }
            } catch (SQLException ex) {
                setMessage("Erreur BD lors de l'ajout du fournisseur: " + ex.getMessage(), Color.RED);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Charge les produits disponibles dans la table de sélection.
     */
    public void refreshAvailableProductsTable() {
        // Defensive check: ensure searchField is not null before using it
        if (searchField != null) { // <-- Added null check here!
            searchField.setText("");
        }
        productSelectionTableModel.setRowCount(0);
        try {
            List<Produit> allProducts = pharmacie.getProduits();
            for (Produit p : allProducts) {
                productSelectionTableModel.addRow(new Object[]{
                    p.getId(),
                    p.getReference(),
                    p.getNom(),
                    p.getPrixHt(), // Prix HT de vente pour info
                    p.getQuantite(),
                    p.getTypeProduit()
                });
            }
        } catch (SQLException e) {
            setMessage("Erreur lors du chargement des produits disponibles: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        }
    }

    /**
     * Recherche les produits selon un critère (nom ou référence) et met à jour la table de sélection.
     */
    private void searchProducts() throws SQLException {
        String critere = searchField.getText().trim();
        productSelectionTableModel.setRowCount(0); 
    
        List<Produit> produits = pharmacie.rechercherProduits(critere);
        for (Produit p : produits) {
            productSelectionTableModel.addRow(new Object[] {
                p.getId(),
                p.getReference(),
                p.getNom(),
                p.getPrixHt(),
                p.getQuantite(),
                p.getTypeProduit()
            });
        }
        setMessage("Recherche terminée. " + produits.size() + " produit(s) trouvé(s).", Color.BLACK);
    }

    /**
     * Ajoute le produit sélectionné à la liste des lignes d'approvisionnement en cours.
     */
    private void addSelectedProductToAppro() throws SQLException {
        int selectedRow = productSelectionTable.getSelectedRow();
        if (selectedRow == -1) {
            setMessage("Veuillez sélectionner un produit à ajouter au bon de commande.", Color.ORANGE);
            return;
        }

        try {
            int productId = (int) productSelectionTableModel.getValueAt(selectedRow, 0);
            int quantity = Integer.parseInt(quantityToAddField.getText().trim());
            double prixAchatHt = Double.parseDouble(prixUnitaireAchatField.getText().trim());

            if (quantity <= 0 || prixAchatHt < 0) {
                setMessage("Quantité doit être positive et Prix Achat HT non négatif.", Color.RED);
                return;
            }

            Produit produit = pharmacie.getProduitById(productId);
            if (produit == null) {
                setMessage("Produit non trouvé.", Color.RED);
                return;
            }

            // Vérifier si la ligne existe déjà pour ce produit et le mettre à jour
            boolean found = false;
            for (LigneApprovisionnement ligne : currentApprovisionnement.getLignesApprovisionnement()) {
                if (ligne.getProduit().getId() == produit.getId()) {
                    ligne.setQuantiteCommandee(ligne.getQuantiteCommandee() + quantity);
                    ligne.setPrixUnitaireAchatHt(prixAchatHt); // Met à jour le prix si différent
                    found = true;
                    break;
                }
            }

            if (!found) {
                LigneApprovisionnement nouvelleLigne = new LigneApprovisionnement(
                    0, // ID Approvisionnement sera défini à la finalisation
                    produit,
                    quantity,
                    prixAchatHt
                );
                currentApprovisionnement.ajouterLigne(nouvelleLigne);
            } else {
                currentApprovisionnement.calculerTotaux(); // Recalculer après modification de ligne existante
            }
            
            updateCurrentApproLinesTable();
            setMessage("Produit ajouté au bon de commande.", Color.BLUE);
        } catch (NumberFormatException ex) {
            setMessage("Veuillez entrer des valeurs numériques valides pour quantité et prix.", Color.RED);
        }
    }

    /**
     * Retire une ligne sélectionnée du bon de commande en cours.
     */
    private void removeSelectedApproLine() {
        int selectedRow = currentApproLinesTable.getSelectedRow();
        if (selectedRow == -1) {
            setMessage("Veuillez sélectionner une ligne à retirer du bon de commande.", Color.ORANGE);
            return;
        }
        int modelRow = currentApproLinesTable.convertRowIndexToModel(selectedRow);
        int productIdToRemove = (int) currentApproLinesTableModel.getValueAt(modelRow, 0);

        currentApprovisionnement.getLignesApprovisionnement().removeIf(
            ligne -> ligne.getProduit().getId() == productIdToRemove
        );
        currentApprovisionnement.calculerTotaux(); // Recalculer après suppression
        updateCurrentApproLinesTable();
        setMessage("Ligne retirée du bon de commande.", Color.BLUE);
    }

    /**
     * Vide complètement le bon de commande en cours.
     */
    private void clearCurrentApprovisionnement() {
        this.currentApprovisionnement = new Approvisionnement(
            (Fournisseur) fournisseurComboBox.getSelectedItem(), // Tente de garder le fournisseur sélectionné
            "BC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
        );
        referenceBonCommandeField.setText(currentApprovisionnement.getReferenceBonCommande());
        updateCurrentApproLinesTable(); // Vide la table d'affichage
        setMessage("Bon de commande vidé.", Color.BLUE);
    }

    /**
     * Met à jour la table affichant les lignes de l'approvisionnement en cours et les totaux.
     */
    private void updateCurrentApproLinesTable() {
        currentApproLinesTableModel.setRowCount(0);
        for (LigneApprovisionnement ligne : currentApprovisionnement.getLignesApprovisionnement()) {
            currentApproLinesTableModel.addRow(new Object[]{
                ligne.getProduit().getId(),
                ligne.getProduit().getReference(),
                ligne.getProduit().getNom(),
                ligne.getQuantiteCommandee(),
                ligne.getPrixUnitaireAchatHt(),
                ligne.getSousTotalHt()
            });
        }
        currentApprovisionnement.calculerTotaux(); // S'assurer que les totaux sont à jour
        totalHtLabel.setText(String.format("%.2f FCFA", currentApprovisionnement.getMontantTotalHt()));
        totalTvaLabel.setText(String.format("%.2f FCFA", currentApprovisionnement.getMontantTva()));
        totalTtcLabel.setText(String.format("%.2f FCFA", currentApprovisionnement.getMontantTotalTtc()));
    }

    /**
     * Finalise l'approvisionnement en l'enregistrant en base de données.
     */
    private void finalizeApprovisionnement() {
        Fournisseur selectedFournisseur = (Fournisseur) fournisseurComboBox.getSelectedItem();
        if (selectedFournisseur == null) {
            setMessage("Veuillez sélectionner un fournisseur.", Color.RED);
            return;
        }
        if (currentApprovisionnement.getLignesApprovisionnement().isEmpty()) {
            setMessage("Le bon de commande est vide. Impossible de finaliser.", Color.ORANGE);
            return;
        }
        String refBC = referenceBonCommandeField.getText().trim();
        if (refBC.isEmpty()) {
            setMessage("La référence du bon de commande est obligatoire.", Color.RED);
            return;
        }

        // Mettre à jour l'objet currentApprovisionnement avec les données finales
        currentApprovisionnement.setFournisseur(selectedFournisseur);
        currentApprovisionnement.setReferenceBonCommande(refBC);
        currentApprovisionnement.setDateApprovisionnement(LocalDateTime.now());
        currentApprovisionnement.calculerTotaux(); // Une dernière fois pour être sûr

        try {
            // Vérifier si la référence du bon de commande existe déjà
            // Note: getApprovisionnementByReference est dans ApprovisionnementDAOImpl et est exposée par Pharmacie
            if (pharmacie.getApprovisionnementByReference(refBC) != null) {
                setMessage("Un approvisionnement avec cette référence de bon de commande existe déjà. Veuillez en utiliser une autre.", Color.RED);
                return;
            }

            boolean success = pharmacie.ajouterApprovisionnement(currentApprovisionnement);
            if (success) {
                setMessage("Approvisionnement pour '" + selectedFournisseur.getNomFournisseur() + "' finalisé (Réf: " + refBC + ").", Color.GREEN);
                clearCurrentApprovisionnement(); // Vider le formulaire après succès
                refreshAvailableProductsTable(); // Rafraîchir les stocks affichés (qui ont été mis à jour via DAO)
                dataListener.onPharmacieDataChanged(); // Notifier les autres panels (Stock, Comptabilité)
            } else {
                setMessage("Erreur lors de la finalisation de l'approvisionnement.", Color.RED);
            }
        } catch (SQLException ex) {
            setMessage("Erreur de base de données lors de la finalisation de l'approvisionnement: " + ex.getMessage(), Color.RED);
            ex.printStackTrace();
        } catch (Exception ex) {
            setMessage("Une erreur inattendue est survenue: " + ex.getMessage(), Color.RED);
            ex.printStackTrace();
        }
    }

    /**
     * Affiche un message à l'utilisateur avec la couleur spécifiée.
     * @param msg Le message à afficher.
     * @param color La couleur du texte du message.
     */
    private void setMessage(String msg, Color color) {
        messageLabel.setText(msg);
        messageLabel.setForeground(color);
    }
}
