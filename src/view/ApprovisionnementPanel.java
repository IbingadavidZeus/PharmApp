package view;

import model.Pharmacie;
import model.Produit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ApprovisionnementPanel extends JPanel {
    private Pharmacie pharmacie;
    private PharmacieDataListener dataListener; // Pour notifier MainFrame des changements de stock

    // Composants de l'interface utilisateur
    private JTextField searchField;
    private JButton searchButton;
    private JTable productSelectionTable;
    private DefaultTableModel productSelectionTableModel;
    private JTextField quantityToAddField;
    private JButton addSupplyButton;

    private JLabel messageLabel; // Pour afficher les messages à l'utilisateur

    // Colonnes pour le tableau de sélection de produits
    private final String[] productColumns = {"Référence", "Nom", "Prix U. TTC", "Stock Actuel"};

    public ApprovisionnementPanel(Pharmacie pharmacie, PharmacieDataListener dataListener) {
        this.pharmacie = pharmacie;
        this.dataListener = dataListener;

        setLayout(new BorderLayout(10, 10));

        // --- Panel Nord: Recherche de produits ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(25);
        searchButton = new JButton("Rechercher Produit");
        searchButton.addActionListener(_ -> searchProducts());
        searchPanel.add(new JLabel("Rechercher (Nom/Référence):"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        // --- Panel Centre: Tableau de sélection de produits ---
        JPanel productTablePanel = new JPanel(new BorderLayout());
        productTablePanel.setBorder(BorderFactory.createTitledBorder("Produits à Approvisionner"));
        productSelectionTableModel = new DefaultTableModel(productColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        productSelectionTable = new JTable(productSelectionTableModel);
        productSelectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productSelectionTable.setAutoCreateRowSorter(true);
        productTablePanel.add(new JScrollPane(productSelectionTable), BorderLayout.CENTER);
        add(productTablePanel, BorderLayout.CENTER);

        // --- Panel Sud: Quantité à ajouter et Bouton d'approvisionnement ---
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        bottomPanel.add(new JLabel("Quantité à ajouter:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        quantityToAddField = new JTextField("1", 10);
        bottomPanel.add(quantityToAddField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        addSupplyButton = new JButton("Ajouter au Stock");
        addSupplyButton.setFont(new Font("Arial", Font.BOLD, 16));
        addSupplyButton.setBackground(new Color(50, 150, 200)); // Bleu
        addSupplyButton.setForeground(Color.WHITE);
        addSupplyButton.addActionListener(_ -> addSupply());
        bottomPanel.add(addSupplyButton, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        messageLabel = new JLabel(" ");
        messageLabel.setForeground(Color.BLACK);
        bottomPanel.add(messageLabel, gbc);
        row++;

        add(bottomPanel, BorderLayout.SOUTH);

        // Remplir le tableau des produits au démarrage
        refreshProductTable();
    }

    /**
     * Recherche les produits et met à jour le tableau de sélection.
     */
    private void searchProducts() {
        String critere = searchField.getText().trim();
        productSelectionTableModel.setRowCount(0); // Vider le tableau actuel

        try {
            List<Produit> produits = pharmacie.rechercherProduits(critere);
            for (Produit p : produits) {
                productSelectionTableModel.addRow(new Object[]{
                    p.getReference(),
                    p.getNom(),
                    String.format("%.2f", p.calculerPrixTTC()),
                    p.getQuantite()
                });
            }
            messageLabel.setText("Recherche terminée. " + produits.size() + " produit(s) trouvé(s).");
            messageLabel.setForeground(Color.BLACK);
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de la recherche des produits: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace();
        }
    }

    /**
     * Rafraîchit le tableau de sélection des produits avec tous les produits du stock.
     */
    public void refreshProductTable() {
        productSelectionTableModel.setRowCount(0);
        searchField.setText(""); // Efface le champ de recherche
        try {
            List<Produit> allProducts = pharmacie.getProduits();
            for (Produit p : allProducts) {
                productSelectionTableModel.addRow(new Object[]{
                    p.getReference(),
                    p.getNom(),
                    String.format("%.2f", p.calculerPrixTTC()),
                    p.getQuantite()
                });
            }
            messageLabel.setText("Tableau des produits rafraîchi.");
            messageLabel.setForeground(Color.BLACK);
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors du chargement des produits disponibles: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace();
        }
    }

    /**
     * Ajoute la quantité spécifiée au stock du produit sélectionné.
     */
    private void addSupply() {
        int selectedRow = productSelectionTable.getSelectedRow();
        if (selectedRow == -1) {
            messageLabel.setText("Veuillez sélectionner un produit à approvisionner.");
            messageLabel.setForeground(Color.ORANGE);
            return;
        }

        try {
            String reference = (String) productSelectionTableModel.getValueAt(selectedRow, 0);
            int quantityToAdd = Integer.parseInt(quantityToAddField.getText().trim());

            if (quantityToAdd <= 0) {
                messageLabel.setText("La quantité à ajouter doit être un nombre positif.");
                messageLabel.setForeground(Color.RED);
                return;
            }

            // Appel de la nouvelle méthode d'approvisionnement dans Pharmacie
            boolean success = pharmacie.approvisionnerProduit(reference, quantityToAdd);

            if (success) {
                messageLabel.setText("Stock du produit '" + reference + "' mis à jour avec succès. Quantité ajoutée: " + quantityToAdd);
                messageLabel.setForeground(Color.GREEN);
                refreshProductTable(); // Rafraîchir le tableau pour montrer le nouveau stock
                quantityToAddField.setText("1"); // Réinitialiser le champ de quantité
                if (dataListener != null) {
                    dataListener.onPharmacieDataChanged(); // Notifier MainFrame (et donc StockPanel)
                }
            } else {
                messageLabel.setText("Échec de l'approvisionnement du produit.");
                messageLabel.setForeground(Color.RED);
            }
        } catch (NumberFormatException ex) {
            messageLabel.setText("Veuillez entrer une quantité numérique valide.");
            messageLabel.setForeground(Color.RED);
        } catch (IllegalArgumentException ex) {
            messageLabel.setText("Erreur: " + ex.getMessage());
            messageLabel.setForeground(Color.RED);
        } catch (SQLException ex) {
            messageLabel.setText("Erreur SQL lors de l'approvisionnement: " + ex.getMessage());
            messageLabel.setForeground(Color.RED);
            ex.printStackTrace();
        } catch (Exception ex) {
            messageLabel.setText("Une erreur inattendue est survenue: " + ex.getMessage());
            messageLabel.setForeground(Color.RED);
            ex.printStackTrace();
        }
    }
}
