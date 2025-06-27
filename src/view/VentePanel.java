package view;

import model.Facture;
import model.LigneFacture;
import model.Panier;
import model.Pharmacie;
import model.Produit;
import model.Utilisateur;
import model.AssuranceSocial;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.UUID;

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
    private JComboBox<AssuranceSocial> assuranceComboBox;
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

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Recherche de Produits"));
        searchPanel.add(new JLabel("Rechercher (Nom/Référence):"));
        searchField = new JTextField(25);
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
        montantDonneField.addActionListener(e -> calculateChange());
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

        refreshProductSelectionTable();
        loadAssurancesIntoComboBox();
        updateCartTableAndTotal();
    }

    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
    }

    public void refreshProductSelectionTable() {
        productSelectionTableModel.setRowCount(0);
        searchField.setText("");
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
        montantDonneField.setText("");
        monnaieARendreLabel.setText("0.00 FCFA");
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
        calculateTotalsWithAssurance();
        calculateChange();
    }

    private void loadAssurancesIntoComboBox() {
        assuranceComboBox.removeAllItems();
        assuranceComboBox.addItem(null);
        try {
            List<AssuranceSocial> assurances = pharmacie.getAllAssurancesSociales();
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

        AssuranceSocial selectedAssurance = (AssuranceSocial) assuranceComboBox.getSelectedItem();
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
                montantRestantAPayer = 0;
            }
        }

        this.currentClientAmountToPay = montantRestantAPayer;

        montantPrisEnChargeLabel.setText(String.format("%.2f FCFA", montantPrisEnCharge));
        montantClientAPayerLabel.setText(String.format("%.2f FCFA", montantRestantAPayer));

        calculateChange();
    }

    private void calculateChange() {
        double totalClientAPayer = this.currentClientAmountToPay;

        try {
            double montantDonne = Double.parseDouble(montantDonneField.getText().trim());
            if (montantDonne < totalClientAPayer) {
                monnaieARendreLabel.setText("Montant insuffisant !");
                monnaieARendreLabel.setForeground(Color.RED);
            } else {
                double monnaie = montantDonne - totalClientAPayer;
                monnaieARendreLabel.setText(String.format("%.2f FCFA", monnaie));
                monnaieARendreLabel.setForeground(Color.BLUE);
            }
        } catch (NumberFormatException ex) {
            monnaieARendreLabel.setText("0.00 FCFA");
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
            double montantDonne = Double.parseDouble(montantDonneField.getText().trim());

            if (montantDonne < montantFinalClientAPayer) {
                setMessage("Le montant donné (" + String.format("%.2f", montantDonne) + " FCFA) est insuffisant. Le client doit payer " + String.format("%.2f", montantFinalClientAPayer) + " FCFA.", Color.RED);
                return;
            }

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

            String numeroFacture = "FACT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            LocalDateTime dateFacture = LocalDateTime.now();
            double totalHtFacture = panier.calculerTotalPanier();
            double totalTTCFacture = panier.calculerTotalPanier();

            AssuranceSocial selectedAssurance = (AssuranceSocial) assuranceComboBox.getSelectedItem();
            double montantPrisEnChargeParAssurance = 0.0;
            double montantRestantADueClient = totalTTCFacture;

            if (selectedAssurance != null) {
                double montantRemboursableDansPanier = 0.0;
                for (LigneFacture ligne : panier.getLignesPanier()) {
                    if (ligne.getProduit().isEstRemboursable()) {
                        montantRemboursableDansPanier += ligne.getSousTotal();
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
                    selectedAssurance,
                    montantPrisEnChargeParAssurance,
                    montantRestantADueClient
            );

            boolean success = pharmacie.finaliserVente(nouvelleFacture);

            if (success) {
                setMessage("Vente finalisée avec succès ! Facture #" + nouvelleFacture.getNumeroFacture() + " enregistrée.", Color.GREEN);

                String factureText = generateFactureText(nouvelleFacture, montantDonne);
                int dialogResult = JOptionPane.showConfirmDialog(this,
                        factureText + "\n\nVoulez-vous copier la facture dans le presse-papiers ?",
                        "Facture Générée", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (dialogResult == JOptionPane.YES_OPTION) {
                    copyToClipboard(factureText);
                    JOptionPane.showMessageDialog(this, "Facture copiée dans le presse-papiers !", "Copie Réussie", JOptionPane.INFORMATION_MESSAGE);
                }

                clearCart();
                refreshProductSelectionTable();

                if (dataListener != null) {
                    dataListener.onPharmacieDataChanged();
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

    private String generateFactureText(Facture facture, double montantDonne) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        sb.append("=====================================================\n");
        sb.append("         PHARMACIE ").append(pharmacie.getNom().toUpperCase()).append("       \n");
        sb.append("         Adresse: ").append(pharmacie.getAdresse()).append("       \n");
        sb.append("=====================================================\n");
        sb.append("                 FACTURE DE VENTE                  \n");
        sb.append("-----------------------------------------------------\n");
        sb.append(String.format("Facture N°: %s\n", facture.getNumeroFacture()));
        sb.append(String.format("Date:       %s\n", facture.getDateFacture().format(formatter)));
        sb.append(String.format("Vendu par:  %s (ID: %d)\n", facture.getUtilisateur().getNomUtilisateur(), facture.getUtilisateur().getId()));
        sb.append("-----------------------------------------------------\n");
        sb.append(String.format("%-25s %5s %10s %10s\n", "Produit", "Qté", "Prix U.", "Sous-Total"));
        sb.append("-----------------------------------------------------\n");

        for (LigneFacture ligne : facture.getLignesFacture()) {
            sb.append(String.format("%-25.25s %5d %10.2f %10.2f\n",
                    ligne.getProduit().getNom(),
                    ligne.getQuantite(),
                    ligne.getProduit().calculerPrixTTC(),
                    ligne.getSousTotal()));
        }
        sb.append("-----------------------------------------------------\n");
        sb.append(String.format("%-40s %10.2f FCFA\n", "TOTAL HORS TAXE (HT):", facture.getTotalHt()));
        sb.append(String.format("%-40s %10.2f FCFA\n", "TOTAL GLOBAL (TTC):", facture.getMontantTotal()));

        if (facture.getAssuranceSocial() != null) {
            sb.append("-----------------------------------------------------\n");
            sb.append(String.format("Assurance Sociale: %s\n", facture.getAssuranceSocial().getNom_assurance()));
            sb.append(String.format("Taux Prise en Charge: %.0f%%\n", facture.getAssuranceSocial().getTauxDePriseEnCharge() * 100));
            sb.append(String.format("Montant Pris en Charge: %10.2f FCFA\n", facture.getMontantPrisEnChargeAssurance()));
        }

        sb.append("-----------------------------------------------------\n");
        sb.append(String.format("%-40s %10.2f FCFA\n", "MONTANT CLIENT À PAYER:", facture.getMontantRestantAPayerClient()));
        sb.append(String.format("%-40s %10.2f FCFA\n", "Montant Payé:", montantDonne));

        double monnaieARendre = montantDonne - facture.getMontantRestantAPayerClient();
        sb.append(String.format("%-40s %10.2f FCFA\n", "Monnaie à Rendre:", monnaieARendre));

        sb.append("=====================================================\n");
        sb.append("           MERCI DE VOTRE VISITE!                  \n");
        sb.append("=====================================================\n");

        return sb.toString();
    }

    private void setMessage(String msg, Color color) {
        messageLabel.setText(msg);
        messageLabel.setForeground(color);
    }

    private void copyToClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
}