package view;

import model.Facture;
import model.LigneFacture;
import model.Panier;
import model.Pharmacie;
import model.Produit;
import model.Utilisateur;

import javax.swing.*;
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
import java.util.Optional; // Pour Optional.empty() ou Optional.of()

public class VentePanel extends JPanel {
    private Pharmacie pharmacie;
    private Utilisateur currentUser; // L'utilisateur actuellement connecté qui effectue la vente
    private PharmacieDataListener dataListener; // Pour notifier MainFrame des changements de stock
    private Panier panier;

    // Composants de l'interface utilisateur
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

    private JLabel totalLabel;
    private JTextField montantDonneField;
    private JLabel monnaieARendreLabel;
    private JButton finaliserVenteButton;

    private JLabel messageLabel; // Pour afficher les messages à l'utilisateur

    // Colonnes pour les tableaux
    private final String[] productColumns = {"Référence", "Nom", "Prix U. TTC", "Stock"};
    private final String[] cartColumns = {"Référence", "Nom", "Quantité", "Prix U. TTC", "Sous-Total"};

    public VentePanel(Pharmacie pharmacie, Utilisateur currentUser, PharmacieDataListener dataListener) {
        this.pharmacie = pharmacie;
        this.currentUser = currentUser;
        this.dataListener = dataListener;
        this.panier = new Panier(pharmacie); // Initialise le panier

        setLayout(new BorderLayout(10, 10)); // Layout principal

        // --- Panel Nord: Recherche de produits ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(25);
        searchButton = new JButton("Rechercher Produit");
        searchButton.addActionListener(_ -> searchProducts());
        searchPanel.add(new JLabel("Rechercher (Nom/Référence):"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        // --- Panel Central: Deux tableaux (Produits disponibles et Panier) ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // --- Panel Gauche: Sélection de produits ---
        JPanel productSelectionPanel = new JPanel(new BorderLayout());
        productSelectionPanel.setBorder(BorderFactory.createTitledBorder("Produits disponibles"));
        productSelectionTableModel = new DefaultTableModel(productColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        productSelectionTable = new JTable(productSelectionTableModel);
        productSelectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Une seule sélection
        productSelectionTable.setAutoCreateRowSorter(true); // Permet le tri
        productSelectionPanel.add(new JScrollPane(productSelectionTable), BorderLayout.CENTER);

        // Panel pour ajouter au panier
        JPanel addToCartPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addToCartPanel.add(new JLabel("Qté à ajouter:"));
        quantityToAddField = new JTextField("1", 5);
        addToCartPanel.add(quantityToAddField);
        addToCartButton = new JButton("Ajouter au Panier");
        addToCartButton.addActionListener(_ -> addSelectedProductToCart());
        addToCartPanel.add(addToCartButton);
        productSelectionPanel.add(addToCartPanel, BorderLayout.SOUTH);
        
        splitPane.setLeftComponent(productSelectionPanel);

        // --- Panel Droit: Contenu du panier ---
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBorder(BorderFactory.createTitledBorder("Votre Panier"));
        cartTableModel = new DefaultTableModel(cartColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.setAutoCreateRowSorter(true);
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        // Panel pour les actions du panier
        JPanel cartActionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        removeFromCartButton = new JButton("Retirer Sélection");
        removeFromCartButton.addActionListener(_ -> removeSelectedProductFromCart());
        clearCartButton = new JButton("Vider Panier");
        clearCartButton.addActionListener(_ -> clearCart());
        cartActionsPanel.add(removeFromCartButton);
        cartActionsPanel.add(clearCartButton);
        cartPanel.add(cartActionsPanel, BorderLayout.SOUTH);

        splitPane.setRightComponent(cartPanel);
        add(splitPane, BorderLayout.CENTER);
        splitPane.setResizeWeight(0.5); // Répartit l'espace 50/50 initialement

        // --- Panel Sud: Récapitulatif et Finalisation ---
        JPanel checkoutPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        checkoutPanel.add(new JLabel("Total Panier (TTC):"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        totalLabel = new JLabel("0.00 FCFA");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(Color.BLUE);
        checkoutPanel.add(totalLabel, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        checkoutPanel.add(new JLabel("Montant donné par le client:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        montantDonneField = new JTextField(10);
        montantDonneField.addActionListener(_ -> calculateChange()); // Calculer la monnaie en appuyant sur Entrée
        checkoutPanel.add(montantDonneField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        checkoutPanel.add(new JLabel("Monnaie à rendre:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        monnaieARendreLabel = new JLabel("0.00 FCFA");
        monnaieARendreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        monnaieARendreLabel.setForeground(Color.DARK_GRAY);
        checkoutPanel.add(monnaieARendreLabel, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        finaliserVenteButton = new JButton("Finaliser la Vente");
        finaliserVenteButton.setFont(new Font("Arial", Font.BOLD, 18));
        finaliserVenteButton.setBackground(new Color(0, 150, 0)); // Vert
        finaliserVenteButton.setForeground(Color.WHITE);
        finaliserVenteButton.addActionListener(_ -> finalizeSale());
        checkoutPanel.add(finaliserVenteButton, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        messageLabel = new JLabel(" ");
        messageLabel.setForeground(Color.RED);
        checkoutPanel.add(messageLabel, gbc);
        row++;

        add(checkoutPanel, BorderLayout.SOUTH);

        // Remplir le tableau des produits disponibles au démarrage
        refreshProductSelectionTable();
        updateCartTableAndTotal(); // Initialise le tableau du panier et le total
    }

    // --- Méthodes de l'interface utilisateur et logique ---

    /**
     * Définit l'utilisateur courant pour le VentePanel.
     * Utile si le VentePanel est créé une seule fois et l'utilisateur change.
     */
    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
    }

    /**
     * Recherche les produits dans la base de données et met à jour le tableau de sélection.
     */
    private void searchProducts() {
        String critere = searchField.getText().trim();
        productSelectionTableModel.setRowCount(0); // Vider le tableau actuel

        List<Produit> produits = pharmacie.rechercherProduits(critere); // Utilise la méthode DAO
        for (Produit p : produits) {
            // Utiliser p.calculerPrixTTC() pour afficher le prix de vente TTC
            productSelectionTableModel.addRow(new Object[]{
                p.getReference(),
                p.getNom(),
                String.format("%.2f", p.calculerPrixTTC()), // Prix TTC
                p.getQuantite() // Stock actuel
            });
        }
        messageLabel.setText("Recherche terminée. " + produits.size() + " produit(s) trouvé(s).");
        messageLabel.setForeground(Color.BLACK);
    }
    
    /**
     * Rafraîchit le tableau de sélection des produits avec tous les produits du stock.
     * Appelé au démarrage ou après une vente pour montrer les quantités mises à jour.
     */
    public void refreshProductSelectionTable() {
        productSelectionTableModel.setRowCount(0);
        searchField.setText(""); // Efface le champ de recherche
        try {
            List<Produit> allProducts = pharmacie.getProduits(); // Récupère TOUS les produits
            for (Produit p : allProducts) {
                // Utiliser p.calculerPrixTTC() pour afficher le prix de vente TTC
                productSelectionTableModel.addRow(new Object[]{
                    p.getReference(),
                    p.getNom(),
                    String.format("%.2f", p.calculerPrixTTC()), // Prix TTC
                    p.getQuantite() // Stock actuel
                });
            }
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors du chargement des produits disponibles: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace();
        }
    }


    /**
     * Ajoute le produit sélectionné au panier.
     */
    private void addSelectedProductToCart() {
        int selectedRow = productSelectionTable.getSelectedRow();
        if (selectedRow == -1) {
            messageLabel.setText("Veuillez sélectionner un produit à ajouter au panier.");
            messageLabel.setForeground(Color.ORANGE);
            return;
        }

        try {
            // Obtenir la référence du produit depuis la ligne sélectionnée (colonne 0)
            String reference = (String) productSelectionTableModel.getValueAt(selectedRow, 0);
            int quantityToAdd = Integer.parseInt(quantityToAddField.getText().trim());

            // La méthode ajouterArticle de Panier gère déjà les SQLException et retourne un message d'erreur
            String errorMessage = panier.ajouterArticle(reference, quantityToAdd); 

            if (errorMessage == null) {
                messageLabel.setText("Produit '" + reference + "' ajouté au panier.");
                messageLabel.setForeground(Color.BLUE);
                updateCartTableAndTotal(); // Rafraîchir le tableau du panier et le total
            } else {
                messageLabel.setText("Erreur ajout panier: " + errorMessage);
                messageLabel.setForeground(Color.RED);
            }
        } catch (NumberFormatException ex) {
            messageLabel.setText("Veuillez entrer une quantité numérique valide.");
            messageLabel.setForeground(Color.RED);
        }
        // Le bloc 'catch (SQLException e)' est retiré d'ici car Panier.ajouterArticle() le gère déjà.
    }

    /**
     * Retire la ligne sélectionnée du panier.
     */
    private void removeSelectedProductFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            messageLabel.setText("Veuillez sélectionner un article à retirer du panier.");
            messageLabel.setForeground(Color.ORANGE);
            return;
        }

        String referenceToRemove = (String) cartTableModel.getValueAt(selectedRow, 0); // Référence du produit dans le panier

        boolean success = panier.supprimerLigne(referenceToRemove); // Supprime toute la ligne pour ce produit

        if (success) {
            messageLabel.setText("Article '" + referenceToRemove + "' retiré du panier.");
            messageLabel.setForeground(Color.BLUE);
            updateCartTableAndTotal(); // Rafraîchir le tableau du panier et le total
        } else {
            messageLabel.setText("Erreur lors du retrait de l'article du panier.");
            messageLabel.setForeground(Color.RED);
        }
    }

    /**
     * Vide complètement le panier.
     */
    private void clearCart() {
        panier.viderPanier();
        updateCartTableAndTotal();
        messageLabel.setText("Panier vidé.");
        messageLabel.setForeground(Color.BLUE);
        montantDonneField.setText(""); // Réinitialiser le champ montant donné
        monnaieARendreLabel.setText("0.00 FCFA"); // Réinitialiser la monnaie
    }

    /**
     * Met à jour le tableau du panier et le label du total.
     */
    private void updateCartTableAndTotal() {
        cartTableModel.setRowCount(0); // Vider le tableau
        for (LigneFacture ligne : panier.getLignesPanier()) {
            Produit p = ligne.getProduit();
            cartTableModel.addRow(new Object[]{
                p.getReference(),
                p.getNom(),
                ligne.getQuantite(),
                String.format("%.2f", ligne.getPrixUnitaire()),
                String.format("%.2f", ligne.getSousTotal())
            });
        }
        totalLabel.setText(String.format("%.2f FCFA", panier.calculerTotalPanier()));
        calculateChange(); // Recalcule la monnaie si un montant est déjà entré
    }

    /**
     * Calcule et affiche la monnaie à rendre.
     */
    private void calculateChange() {
        double total = panier.calculerTotalPanier();
        try {
            double montantDonne = Double.parseDouble(montantDonneField.getText().trim());
            if (montantDonne < total) {
                monnaieARendreLabel.setText("Montant insuffisant !");
                monnaieARendreLabel.setForeground(Color.RED);
            } else {
                double monnaie = montantDonne - total;
                monnaieARendreLabel.setText(String.format("%.2f FCFA", monnaie));
                monnaieARendreLabel.setForeground(Color.BLUE);
            }
        } catch (NumberFormatException ex) {
            monnaieARendreLabel.setText("0.00 FCFA"); // Si la saisie n'est pas valide, réinitialiser
            monnaieARendreLabel.setForeground(Color.DARK_GRAY);
        }
    }

    /**
     * Finalise la vente: crée la facture, l'enregistre en DB, met à jour le stock, vide le panier.
     */
    private void finalizeSale() {
        if (panier.estVide()) {
            messageLabel.setText("Le panier est vide. Impossible de finaliser la vente.");
            messageLabel.setForeground(Color.ORANGE);
            return;
        }

        try {
            double totalAPayer = panier.calculerTotalPanier();
            double montantDonne = Double.parseDouble(montantDonneField.getText().trim());

            if (montantDonne < totalAPayer) {
                messageLabel.setText("Le montant donné est insuffisant. Veuillez ajuster le paiement.");
                messageLabel.setForeground(Color.RED);
                return;
            }

            // Vérifier le stock une dernière fois avant de finaliser (précaution supplémentaire)
            for (LigneFacture ligne : panier.getLignesPanier()) {
                Produit produitEnStock = pharmacie.getProduitByReference(ligne.getProduit().getReference());
                if (produitEnStock == null || produitEnStock.getQuantite() < ligne.getQuantite()) {
                    messageLabel.setText("Stock insuffisant pour le produit: " + ligne.getProduit().getNom());
                    messageLabel.setForeground(Color.RED);
                    return; // Annuler la vente
                }
            }

            // Créer l'objet Facture
            // NOUVEAU: Vérifier que currentUser n'est pas null
            if (currentUser == null) {
                messageLabel.setText("Erreur: Aucun utilisateur connecté pour finaliser la vente.");
                messageLabel.setForeground(Color.RED);
                return;
            }
            Facture nouvelleFacture = new Facture(currentUser); // L'utilisateur courant
            nouvelleFacture.setLignesFacture(panier.getLignesPanier()); // Ajoute les lignes du panier
            nouvelleFacture.calculerMontantTotal(); // S'assure que le total est bien calculé

            // Tenter de finaliser la vente via la Pharmacie (qui utilise FactureDAO et LigneFactureDAO)
            boolean success = pharmacie.finaliserVente(nouvelleFacture);

            if (success) {
                messageLabel.setText("Vente finalisée avec succès ! Facture #" + nouvelleFacture.getId() + " enregistrée.");
                messageLabel.setForeground(Color.GREEN);
                
                // --- Imprimer/Exporter la facture (Version Texte) ---
                String factureText = generateFactureText(nouvelleFacture);
                int dialogResult = JOptionPane.showConfirmDialog(this, 
                                                                 factureText + "\n\nVoulez-vous copier la facture dans le presse-papiers ?", 
                                                                 "Facture Générée", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (dialogResult == JOptionPane.YES_OPTION) {
                    copyToClipboard(factureText);
                    JOptionPane.showMessageDialog(this, "Facture copiée dans le presse-papiers !");
                }
                
                clearCart(); // Vider le panier après la vente
                refreshProductSelectionTable(); // Rafraîchir les quantités de stock dans le tableau de sélection
                
                // Notifier les autres panels (notamment StockPanel) qu'une vente a eu lieu et que les données ont changé
                if (dataListener != null) {
                    dataListener.onPharmacieDataChanged();
                }
            } else {
                messageLabel.setText("Erreur: Impossible de finaliser la vente.");
                messageLabel.setForeground(Color.RED);
            }

        } catch (NumberFormatException ex) {
            messageLabel.setText("Veuillez entrer un montant valide pour le paiement.");
            messageLabel.setForeground(Color.RED);
        } catch (SQLException ex) {
            messageLabel.setText("Erreur de base de données lors de la finalisation: " + ex.getMessage());
            messageLabel.setForeground(Color.RED);
            ex.printStackTrace();
        } catch (Exception ex) {
            messageLabel.setText("Une erreur inattendue est survenue: " + ex.getMessage());
            messageLabel.setForeground(Color.RED);
            ex.printStackTrace();
        }
    }

    /**
     * Génère une représentation textuelle de la facture pour l'affichage/l'exportation.
     * @param facture La facture à générer.
     * @return La facture sous forme de chaîne de caractères.
     */
    private String generateFactureText(Facture facture) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        sb.append("========================================================\n");
        sb.append("                 FACTURE DE VENTE                     \n");
        sb.append("========================================================\n");
        sb.append("Pharmacie: ").append(pharmacie.getNom()).append("\n");
        sb.append("Adresse:   ").append(pharmacie.getAdresse()).append("\n");
        sb.append("--------------------------------------------------------\n");
        sb.append("Facture ID: ").append(facture.getId()).append("\n");
        sb.append("Date:       ").append(facture.getDateFacture().format(formatter)).append("\n");
        sb.append("Vendu par:  ").append(facture.getUtilisateur().getNomUtilisateur()).append(" (ID: ").append(facture.getUtilisateur().getId()).append(")\n");
        sb.append("--------------------------------------------------------\n");
        sb.append(String.format("%-25s %8s %12s %15s\n", "Produit", "Qté", "Prix U.", "Sous-Total"));
        sb.append("--------------------------------------------------------\n");

        for (LigneFacture ligne : facture.getLignesFacture()) {
            sb.append(String.format("%-25.25s %8d %12.2f %15.2f\n",
                                    ligne.getProduit().getNom(),
                                    ligne.getQuantite(),
                                    ligne.getPrixUnitaire(),
                                    ligne.getSousTotal()));
        }
        sb.append("--------------------------------------------------------\n");
        sb.append(String.format("%-46s %15.2f FCFA\n", "TOTAL À PAYER (TTC):", facture.getMontantTotal()));
        
        try {
            double montantDonne = Double.parseDouble(montantDonneField.getText().trim());
            double monnaie = montantDonne - facture.getMontantTotal();
            sb.append(String.format("%-46s %15.2f FCFA\n", "Montant payé:", montantDonne));
            sb.append(String.format("%-46s %15.2f FCFA\n", "Monnaie à rendre:", monnaie));
        } catch (NumberFormatException e) {
            sb.append(String.format("%-46s %15s\n", "Montant payé:", "N/A"));
            sb.append(String.format("%-46s %15s\n", "Monnaie à rendre:", "N/A (Erreur de saisie)"));
        }
        
        sb.append("========================================================\n");
        sb.append("              MERCI DE VOTRE VISITE!                  \n");
        sb.append("========================================================\n");

        return sb.toString();
    }

    /**
     * Copie le texte donné dans le presse-papiers du système.
     */
    private void copyToClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
}
