package view;

import model.Facture;
import model.LigneFacture;
import model.Panier;
import model.Pharmacie;
import model.Produit;
import model.Utilisateur;
import model.AssuranceSocial; // Utilisé votre nom de classe AssuranceSocial

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder; // Ajouté pour les titres des bordures
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter; // Ajouté pour écouter les touches clavier
import java.awt.event.KeyEvent; // Ajouté pour écouter les touches clavier
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects; // Ajouté pour Objects.equals
import java.util.UUID;
import java.awt.print.PrinterException; // Ajouté pour la gestion des exceptions d'impression

public class VentePanel extends JPanel {
    private Pharmacie pharmacie;
    private Utilisateur currentUser;
    private PharmacieDataListener dataListener;
    private Panier panier;

    private JTextField searchField;
    private JButton searchButton;
    private JTable productSelectionTable;
    private DefaultTableModel productSelectionTableModel;
    private JTextField quantityToAddField;
    private JButton addToCartButton;

    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private JButton removeFromCartButton;
    private JButton clearCartButton;

    private JLabel totalTTCAvantAssuranceLabel;
    private JComboBox<AssuranceSocial> assuranceComboBox; // Utilise votre nom de classe
    private JLabel montantPrisEnChargeLabel;
    private JLabel montantClientAPayerLabel;

    private double currentClientAmountToPay;

    private JTextField montantDonneField;
    private JLabel monnaieARendreLabel;
    private JButton finaliserVenteButton;

    private JLabel messageLabel;

    private final String[] productColumns = {"Référence", "Nom", "Prix U. TTC", "Stock", "Remboursable"};
    private final String[] cartColumns = {"Référence", "Nom", "Qté", "Prix U. TTC", "Sous-Total", "Remboursable"};

    public VentePanel(Pharmacie pharmacie, Utilisateur currentUser, PharmacieDataListener dataListener) {
        this.pharmacie = pharmacie;
        this.currentUser = currentUser;
        this.dataListener = dataListener;
        this.panier = new Panier(pharmacie);

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- Panel de Recherche et Sélection de Produits (NORTH) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Recherche de Produits"));
        searchPanel.add(new JLabel("Rechercher (Nom/Référence):"));
        searchField = new JTextField(25);
        // Ajout de l'écouteur pour la touche ENTER
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        searchProducts();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(VentePanel.this,
                                "Erreur lors du chargement des produits depuis la base de données: " + ex.getMessage(),
                                "Erreur de Base de Données", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
        });
        searchPanel.add(searchField);
        searchButton = new JButton("Rechercher");
        searchButton.addActionListener(e -> {
            try {
                searchProducts();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors du chargement des produits depuis la base de données: " + ex.getMessage(),
                        "Erreur de Base de Données", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        // --- SplitPane pour la sélection des produits et le panier (CENTER) ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        JPanel productSelectionPanel = new JPanel(new BorderLayout(5, 5));
        productSelectionPanel.setBorder(BorderFactory.createTitledBorder("Produits disponibles"));
        productSelectionTableModel = new DefaultTableModel(productColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 2) return Double.class;
                if (column == 3) return Integer.class;
                if (column == 4) return Boolean.class;
                return Object.class;
            }
        };
        productSelectionTable = new JTable(productSelectionTableModel);
        productSelectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productSelectionTable.setAutoCreateRowSorter(true);
        productSelectionPanel.add(new JScrollPane(productSelectionTable), BorderLayout.CENTER);

        JPanel addToCartPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        addToCartPanel.add(new JLabel("Quantité:"));
        quantityToAddField = new JTextField("1", 5);
        addToCartPanel.add(quantityToAddField);
        addToCartButton = new JButton("Ajouter au Panier");
        addToCartButton.setBackground(new Color(135, 206, 250));
        addToCartButton.setForeground(Color.BLACK);
        addToCartButton.addActionListener(e -> addSelectedProductToCart());
        addToCartPanel.add(addToCartButton);
        productSelectionPanel.add(addToCartPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(productSelectionPanel);

        JPanel cartPanel = new JPanel(new BorderLayout(5, 5));
        cartPanel.setBorder(BorderFactory.createTitledBorder("Votre Panier"));
        cartTableModel = new DefaultTableModel(cartColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 2) return Integer.class;
                if (column == 3 || column == 4) return Double.class;
                if (column == 5) return Boolean.class;
                return Object.class;
            }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.setAutoCreateRowSorter(true);
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel cartActionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        removeFromCartButton = new JButton("Retirer Sélection");
        removeFromCartButton.setBackground(new Color(255, 99, 71));
        removeFromCartButton.setForeground(Color.WHITE);
        removeFromCartButton.addActionListener(e -> removeSelectedProductFromCart());
        clearCartButton = new JButton("Vider Panier");
        clearCartButton.setBackground(new Color(255, 215, 0));
        clearCartButton.setForeground(Color.BLACK);
        clearCartButton.addActionListener(e -> clearCart());
        cartActionsPanel.add(removeFromCartButton);
        cartActionsPanel.add(clearCartButton);
        cartPanel.add(cartActionsPanel, BorderLayout.SOUTH);

        splitPane.setRightComponent(cartPanel);
        add(splitPane, BorderLayout.CENTER);

        // --- Panel des Totaux, Paiement et Finalisation (SOUTH) ---
        JPanel checkoutPanel = new JPanel(new GridBagLayout());
        checkoutPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        checkoutPanel.add(new JLabel("Total TTC (avant assurance):"), gbc);
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        totalTTCAvantAssuranceLabel = new JLabel("0.00 FCFA");
        totalTTCAvantAssuranceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalTTCAvantAssuranceLabel.setForeground(Color.BLUE);
        checkoutPanel.add(totalTTCAvantAssuranceLabel, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        checkoutPanel.add(new JLabel("Assurance Sociale:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        assuranceComboBox = new JComboBox<>();
        assuranceComboBox.setPreferredSize(new Dimension(250, 30));
        assuranceComboBox.addActionListener(e -> calculateTotalsWithAssurance());
        checkoutPanel.add(assuranceComboBox, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        checkoutPanel.add(new JLabel("Montant pris en charge par assurance:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        montantPrisEnChargeLabel = new JLabel("0.00 FCFA");
        montantPrisEnChargeLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        montantPrisEnChargeLabel.setForeground(new Color(34, 139, 34));
        checkoutPanel.add(montantPrisEnChargeLabel, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        checkoutPanel.add(new JLabel("MONTANT CLIENT À PAYER (TTC):"), gbc);
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        montantClientAPayerLabel = new JLabel("0.00 FCFA");
        montantClientAPayerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        montantClientAPayerLabel.setForeground(new Color(255, 69, 0));
        checkoutPanel.add(montantClientAPayerLabel, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        checkoutPanel.add(new JLabel("Montant donné par le client:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        montantDonneField = new JTextField(10);
        // Ajout de l'écouteur pour la touche ENTER et mise à jour dynamique
        montantDonneField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calculateChange(); // Recalcule à chaque frappe
            }
        });
        checkoutPanel.add(montantDonneField, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        checkoutPanel.add(new JLabel("Monnaie à rendre:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        monnaieARendreLabel = new JLabel("0.00 FCFA");
        monnaieARendreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        monnaieARendreLabel.setForeground(Color.DARK_GRAY);
        checkoutPanel.add(monnaieARendreLabel, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        finaliserVenteButton = new JButton("Finaliser la Vente");
        finaliserVenteButton.setFont(new Font("Arial", Font.BOLD, 18));
        finaliserVenteButton.setBackground(new Color(34, 139, 34));
        finaliserVenteButton.setForeground(Color.WHITE);
        finaliserVenteButton.addActionListener(e -> finalizeSale());
        checkoutPanel.add(finaliserVenteButton, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setForeground(Color.RED);
        checkoutPanel.add(messageLabel, gbc);

        add(checkoutPanel, BorderLayout.SOUTH);

        // Initialiser l'affichage
        refreshProductSelectionTable();
        loadAssurancesIntoComboBox();
        updateCartTableAndTotal(); // Cela va aussi déclencher calculateTotalsWithAssurance et calculateChange
    }

    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
        // Si l'utilisateur change, on pourrait vouloir démarrer une nouvelle vente
        // ou simplement s'assurer que l'utilisateur de la facture est à jour si un panier est en cours.
        // Puisque votre classe Panier n'a pas d'utilisateur, cela se gérera à la finalisation de la vente.
    }

    public void refreshProductSelectionTable() {
        productSelectionTableModel.setRowCount(0);
        searchField.setText(""); // Vider le champ de recherche
        try {
            List<Produit> allProducts = pharmacie.getProduits();
            for (Produit p : allProducts) {
                productSelectionTableModel.addRow(new Object[]{
                        p.getReference(),
                        p.getNom(),
                        p.calculerPrixTTC(),
                        p.getQuantite(),
                        p.isEstRemboursable()
                });
            }
        } catch (SQLException e) {
            setMessage("Erreur lors du chargement des produits disponibles: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        }
    }

    private void searchProducts() throws SQLException {
        String critere = searchField.getText().trim();
        productSelectionTableModel.setRowCount(0);

        List<Produit> produits = pharmacie.rechercherProduits(critere);
        for (Produit p : produits) {
            productSelectionTableModel.addRow(new Object[]{
                    p.getReference(),
                    p.getNom(),
                    p.calculerPrixTTC(),
                    p.getQuantite(),
                    p.isEstRemboursable()
            });
        }
        setMessage("Recherche terminée. " + produits.size() + " produit(s) trouvé(s).", Color.BLACK);
    }

    private void addSelectedProductToCart() {
        int selectedRow = productSelectionTable.getSelectedRow();
        if (selectedRow == -1) {
            setMessage("Veuillez sélectionner un produit à ajouter au panier.", Color.ORANGE);
            return;
        }

        try {
            String reference = (String) productSelectionTableModel.getValueAt(selectedRow, 0);
            int quantityToAdd = Integer.parseInt(quantityToAddField.getText().trim());

            if (quantityToAdd <= 0) {
                setMessage("La quantité à ajouter doit être un nombre positif.", Color.RED);
                return;
            }

            String errorMessage = panier.ajouterArticle(reference, quantityToAdd);

            if (errorMessage == null) {
                setMessage("Produit '" + reference + "' ajouté au panier.", Color.BLUE);
                updateCartTableAndTotal();
            } else {
                setMessage("Erreur ajout panier: " + errorMessage, Color.RED);
            }
        } catch (NumberFormatException ex) {
            setMessage("Veuillez entrer une quantité numérique valide.", Color.RED);
        }
    }

    private void removeSelectedProductFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            setMessage("Veuillez sélectionner un article à retirer du panier.", Color.ORANGE);
            return;
        }

        String referenceToRemove = (String) cartTableModel.getValueAt(selectedRow, 0);

        boolean success = panier.supprimerLigne(referenceToRemove);

        if (success) {
            setMessage("Article '" + referenceToRemove + "' retiré du panier.", Color.BLUE);
            updateCartTableAndTotal();
        } else {
            setMessage("Erreur lors du retrait de l'article du panier.", Color.RED);
        }
    }

    private void clearCart() {
        panier.viderPanier();
        updateCartTableAndTotal();
        setMessage("Panier vidé.", Color.BLUE);
        montantDonneField.setText(""); // Réinitialiser le champ montant donné
        monnaieARendreLabel.setText("0.00 FCFA"); // Réinitialiser la monnaie à rendre
    }

    private void updateCartTableAndTotal() {
        cartTableModel.setRowCount(0);
        for (LigneFacture ligne : panier.getLignesPanier()) {
            Produit p = ligne.getProduit();
            cartTableModel.addRow(new Object[]{
                    p.getReference(),
                    p.getNom(),
                    ligne.getQuantite(),
                    ligne.getPrixUnitaire(),
                    ligne.getSousTotal(),
                    p.isEstRemboursable()
            });
        }
        calculateTotalsWithAssurance(); // Ceci met à jour les labels de totaux
        calculateChange();
    }

    private void loadAssurancesIntoComboBox() {
        assuranceComboBox.removeAllItems();
        assuranceComboBox.addItem(null); // Option "Aucune assurance"
        try {
            List<AssuranceSocial> assurances = pharmacie.getAllAssurancesSociales(); // Utilise votre type AssuranceSocial
            for (AssuranceSocial ass : assurances) {
                assuranceComboBox.addItem(ass);
            }
        } catch (SQLException e) {
            setMessage("Erreur lors du chargement des assurances: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        }
    }

    private void calculateTotalsWithAssurance() {
        double totalTTC = panier.calculerTotalPanier();
        totalTTCAvantAssuranceLabel.setText(String.format("%.2f FCFA", totalTTC));

        AssuranceSocial selectedAssurance = (AssuranceSocial) assuranceComboBox.getSelectedItem(); // Utilise votre type
        double montantRemboursable = 0.0;
        double montantPrisEnCharge = 0.0;
        double montantRestantAPayer = totalTTC;

        if (selectedAssurance != null) {
            for (LigneFacture ligne : panier.getLignesPanier()) {
                if (ligne.getProduit().isEstRemboursable()) {
                    montantRemboursable += ligne.getSousTotal();
                }
            }

            montantPrisEnCharge = montantRemboursable * selectedAssurance.getTauxDePriseEnCharge();
            montantRestantAPayer = totalTTC - montantPrisEnCharge;
            if (montantRestantAPayer < 0) {
                montantRestantAPayer = 0; // Le montant à payer par le client ne peut pas être négatif
            }
        }

        this.currentClientAmountToPay = montantRestantAPayer;

        montantPrisEnChargeLabel.setText(String.format("%.2f FCFA", montantPrisEnCharge));
        montantClientAPayerLabel.setText(String.format("%.2f FCFA", montantRestantAPayer));

        calculateChange(); // Recalcule la monnaie à rendre chaque fois que les totaux changent
    }

    private void calculateChange() {
        double totalClientAPayer = this.currentClientAmountToPay;

        try {
            // Remplace la virgule par un point pour la conversion en double
            double montantDonne = Double.parseDouble(montantDonneField.getText().trim().replace(',', '.'));
            if (montantDonne < totalClientAPayer) {
                monnaieARendreLabel.setText("Montant insuffisant !");
                monnaieARendreLabel.setForeground(Color.RED);
            } else {
                double monnaie = montantDonne - totalClientAPayer;
                monnaieARendreLabel.setText(String.format("%.2f FCFA", monnaie));
                monnaieARendreLabel.setForeground(new Color(0, 128, 0)); // Vert foncé
            }
        } catch (NumberFormatException ex) {
            monnaieARendreLabel.setText("0.00 FCFA"); // Si le champ est vide ou invalide
            monnaieARendreLabel.setForeground(Color.DARK_GRAY);
        }
    }

    private void finalizeSale() {
        if (panier.estVide()) {
            setMessage("Le panier est vide. Impossible de finaliser la vente.", Color.ORANGE);
            return;
        }

        double montantFinalClientAPayer = this.currentClientAmountToPay;

        try {
            // Remplace la virgule par un point pour la conversion en double
            double montantDonne = Double.parseDouble(montantDonneField.getText().trim().replace(',', '.'));

            if (montantDonne < montantFinalClientAPayer) {
                setMessage("Le montant donné (" + String.format("%.2f", montantDonne) + " FCFA) est insuffisant. Le client doit payer " + String.format("%.2f", montantFinalClientAPayer) + " FCFA.", Color.RED);
                return;
            }

            // Vérification finale du stock avant de procéder
            for (LigneFacture ligne : panier.getLignesPanier()) {
                Produit produitEnStock = pharmacie.getProduitByReference(ligne.getProduit().getReference());
                if (produitEnStock == null || produitEnStock.getQuantite() < ligne.getQuantite()) {
                    setMessage("Stock insuffisant pour le produit: " + ligne.getProduit().getNom() + ". Stock disponible: " + (produitEnStock != null ? produitEnStock.getQuantite() : "0"), Color.RED);
                    return;
                }
            }

            if (currentUser == null) {
                setMessage("Erreur: Aucun utilisateur connecté pour finaliser la vente. Veuillez vous connecter.", Color.RED);
                return;
            }

            // Création de la facture
            String numeroFacture = "FACT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            LocalDateTime dateFacture = LocalDateTime.now();
            double totalHtFacture = panier.calculerTotalPanier(); // Utiliser la méthode HT de Panier
            double totalTTCFacture = panier.calculerTotalPanier();

            AssuranceSocial selectedAssurance = (AssuranceSocial) assuranceComboBox.getSelectedItem(); // Utilise votre type
            double montantPrisEnChargeParAssurance = 0.0;
            double montantRestantADueClient = totalTTCFacture;

            if (selectedAssurance != null) {
                double montantRemboursableDansPanier = 0.0;
                for (LigneFacture ligne : panier.getLignesPanier()) {
                    if (ligne.getProduit().isEstRemboursable()) {
                        montantRemboursableDansPanier += ligne.getSousTotal(); // Sous-total TTC des produits remboursables
                    }
                }
                montantPrisEnChargeParAssurance = montantRemboursableDansPanier * selectedAssurance.getTauxDePriseEnCharge();
                montantRestantADueClient = totalTTCFacture - montantPrisEnChargeParAssurance;
                if (montantRestantADueClient < 0) {
                    montantRestantADueClient = 0;
                }
            }

            Facture nouvelleFacture = new Facture(
                    numeroFacture,
                    dateFacture,
                    currentUser,
                    panier.getLignesPanier(),
                    totalHtFacture,
                    totalTTCFacture,
                    selectedAssurance, // Passer l'objet AssuranceSocial tel quel
                    montantPrisEnChargeParAssurance,
                    montantRestantADueClient
            );

            boolean success = pharmacie.finaliserVente(nouvelleFacture);

            if (success) {
                setMessage("Vente finalisée avec succès ! Facture #" + nouvelleFacture.getNumeroFacture() + " enregistrée.", Color.GREEN);

                String factureText = generateFactureText(nouvelleFacture, montantDonne);
                // Afficher la facture dans un JTextArea pour permettre la copie
                JTextArea factureTextArea = new JTextArea(factureText);
                factureTextArea.setEditable(false);
                factureTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Police pour alignement
                factureTextArea.setTabSize(4); // Taille des tabulations

                JScrollPane scrollPane = new JScrollPane(factureTextArea);
                scrollPane.setPreferredSize(new Dimension(500, 600)); // Taille de la fenêtre

                JButton copyFactureButton = new JButton("Copier la facture");
                copyFactureButton.addActionListener(e -> copyToClipboard(factureText));

                // NOUVEAU: Bouton Imprimer
                JButton printFactureButton = new JButton("Imprimer la facture");
                printFactureButton.addActionListener(e -> {
                    try {
                        // Utilise la méthode print() de JTextArea
                        boolean complete = factureTextArea.print();
                        if (complete) {
                            JOptionPane.showMessageDialog(this, "Impression de la facture terminée.", "Impression", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, "Impression de la facture annulée ou incomplète.", "Impression", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (PrinterException pe) {
                        JOptionPane.showMessageDialog(this, "Erreur lors de l'impression: " + pe.getMessage(), "Erreur d'impression", JOptionPane.ERROR_MESSAGE);
                        pe.printStackTrace();
                    }
                });

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                buttonPanel.add(copyFactureButton);
                buttonPanel.add(printFactureButton); // Ajout du bouton Imprimer

                JPanel dialogContent = new JPanel(new BorderLayout());
                dialogContent.add(scrollPane, BorderLayout.CENTER);
                dialogContent.add(buttonPanel, BorderLayout.SOUTH);

                JOptionPane.showMessageDialog(this, dialogContent, "Facture Générée", JOptionPane.INFORMATION_MESSAGE);

                clearCart(); // Vider le panier après succès
                refreshProductSelectionTable(); // Rafraîchir la table des produits (stock mis à jour)

                if (dataListener != null) {
                    dataListener.onPharmacieDataChanged(); // Notifier MainFrame pour rafraîchir les autres panels
                }
            } else {
                setMessage("Erreur: Impossible de finaliser la vente. Problème de base de données ou de stock.", Color.RED);
            }

        } catch (NumberFormatException ex) {
            setMessage("Veuillez entrer un montant valide pour le paiement.", Color.RED);
        } catch (SQLException ex) {
            setMessage("Erreur de base de données lors de la finalisation: " + ex.getMessage(), Color.RED);
            ex.printStackTrace();
        } catch (Exception ex) {
            setMessage("Une erreur inattendue est survenue: " + ex.getMessage(), Color.RED);
            ex.printStackTrace();
        }
    }

    /**
     * Génère le texte formaté de la facture pour l'impression.
     * @param facture La facture à formater.
     * @param montantDonne Le montant donné par le client.
     * @return Une chaîne de caractères représentant la facture.
     */
    private String generateFactureText(Facture facture, double montantDonne) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Largeur totale de la facture pour le formatage
        int factureWidth = 60; // Gardons 60 caractères pour le design type reçu de caisse

        String separator = "=".repeat(factureWidth);
        String thinSeparator = "-".repeat(factureWidth);

        // Header de la pharmacie
        sb.append(separator).append("\n");
        sb.append(centerText("PHARMACIE " + pharmacie.getNom().toUpperCase(), factureWidth)).append("\n");
        sb.append(centerText("Adresse: " + pharmacie.getAdresse(), factureWidth)).append("\n");
        sb.append(separator).append("\n");
        sb.append(centerText("FACTURE DE VENTE", factureWidth)).append("\n");
        sb.append(thinSeparator).append("\n");

        // Détails de la facture
        sb.append(String.format("Facture N°: %s\n", facture.getNumeroFacture()));
        sb.append(String.format("Date:       %s\n", facture.getDateFacture().format(formatter)));
        sb.append(String.format("Vendu par:  %s (ID: %d)\n", facture.getUtilisateur().getNomUtilisateur(), facture.getUtilisateur().getId()));
        sb.append(thinSeparator).append("\n");

        // Lignes de produits
        // "Produit" (30), "Qté" (5), "Prix U." (10), "Sous-Total" (12) = 57 + 3 espaces = 60
        sb.append(String.format("%-30s %5s %10s %12s\n", "Produit", "Qté", "Prix U.", "Sous-Total"));
        sb.append(thinSeparator).append("\n");

        for (LigneFacture ligne : facture.getLignesFacture()) {
            String productName = ligne.getProduit().getNom();
            if (productName.length() > 30) {
                productName = productName.substring(0, 27) + "..."; // Tronque si trop long
            }
            sb.append(String.format("%-30s %5d %10.2f %12.2f\n",
                    productName,
                    ligne.getQuantite(),
                    ligne.getPrixUnitaire(), // Prix unitaire TTC de la ligne
                    ligne.getSousTotal())); // Sous-total TTC de la ligne
        }
        sb.append(thinSeparator).append("\n");

        // Totaux
        // Les labels sont alignés à gauche, les montants à droite sur 12 caractères
        sb.append(String.format("%-45s %12.2f FCFA\n", "TOTAL HORS TAXE (HT):", facture.getTotalHt()));
        // Calcule la TVA directement si elle n'est pas un attribut de la facture
        double tvaAmount = facture.getMontantTotal() - facture.getTotalHt();
        sb.append(String.format("%-45s %12.2f FCFA\n", "TVA (18%):", tvaAmount));
        sb.append(String.format("%-45s %12.2f FCFA\n", "TOTAL GLOBAL (TTC):", facture.getMontantTotal()));

        // Section Assurance (utilise les getters de votre AssuranceSocial)
        if (facture.getAssuranceSocial() != null) {
            sb.append(thinSeparator).append("\n");
            sb.append(String.format("Assurance Sociale: %s\n", facture.getAssuranceSocial().getNom_assurance())); // Utilise getNom_assurance()
            sb.append(String.format("Taux Prise en Charge: %.0f%%\n", facture.getAssuranceSocial().getTauxDePriseEnCharge() * 100));
            sb.append(String.format("%-45s %12.2f FCFA\n", "Montant Pris en Charge:", facture.getMontantPrisEnChargeAssurance()));
        }

        // Paiement et Monnaie
        sb.append(thinSeparator).append("\n");
        sb.append(String.format("%-45s %12.2f FCFA\n", "MONTANT CLIENT À PAYER:", facture.getMontantRestantAPayerClient()));
        sb.append(String.format("%-45s %12.2f FCFA\n", "Montant Payé:", montantDonne));

        double monnaieARendre = montantDonne - facture.getMontantRestantAPayerClient();
        sb.append(String.format("%-45s %12.2f FCFA\n", "Monnaie à Rendre:", monnaieARendre));

        // Footer
        sb.append(separator).append("\n");
        sb.append(centerText("MERCI DE VOTRE VISITE !", factureWidth)).append("\n");
        sb.append(separator).append("\n");

        return sb.toString();
    }

    /**
     * Centre un texte donné dans une largeur spécifiée.
     * @param text Le texte à centrer.
     * @param width La largeur totale disponible.
     * @return Le texte centré.
     */
    private String centerText(String text, int width) {
        if (text == null || text.isEmpty()) {
            return " ".repeat(width);
        }
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int padding = width - text.length();
        int leftPadding = padding / 2;
        int rightPadding = padding - leftPadding;
        return " ".repeat(leftPadding) + text + " ".repeat(rightPadding);
    }

    /**
     * Définit le texte du message de statut avec une couleur.
     * @param msg Le message à afficher.
     * @param color La couleur du texte.
     */
    private void setMessage(String msg, Color color) {
        messageLabel.setText(msg);
        messageLabel.setForeground(color);
    }

    /**
     * Copie le texte donné dans le presse-papiers du système.
     * @param text Le texte à copier.
     */
    private void copyToClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
}
