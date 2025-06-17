package view;

import model.Pharmacie;
import model.Produit;
import model.Medicament;
import model.ProduitParaPharmacie;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException; 
import java.util.List;

public class StockPanel extends JPanel {
    private Pharmacie pharmacie;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private PharmacieDataListener dataListener;

    // Colonnes pour le tableau, incluant la description
    private final String[] columnNames = {"ID", "Référence", "Nom", "Description", "Type", "Prix HT", "Quantité", "Prix TTC", "Générique", "Ordonnance", "Catégorie Para."};

    // Constructeur mis à jour pour accepter le PharmacieDataListener
    public StockPanel(Pharmacie pharmacie, PharmacieDataListener listener) {
        this.pharmacie = pharmacie;
        this.dataListener = listener; // Initialisation du listener
        setLayout(new BorderLayout());

        // --- Configuration du tableau ---
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Rendre toutes les cellules non éditables par défaut
            }
        };
        productTable = new JTable(tableModel);
        productTable.setFillsViewportHeight(true); // Pour que le tableau remplisse la zone d'affichage
        productTable.setAutoCreateRowSorter(true); // Permet le tri par colonne
        
        // Ajuster la hauteur des lignes si la description est longue
        productTable.setRowHeight(24); 
        // Vous pouvez aussi tenter de configurer un RowRenderer si les descriptions sont très longues
        // pour qu'elles s'affichent sur plusieurs lignes, mais c'est plus complexe.

        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Boutons et panel de contrôle ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshButton = new JButton("Rafraîchir le stock");
        refreshButton.addActionListener(_ -> remplirTable());
        controlPanel.add(refreshButton);

        JButton deleteButton = new JButton("Supprimer Produit Sélectionné");
        deleteButton.addActionListener(_ -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow != -1) {
                String referenceToDelete = (String) tableModel.getValueAt(selectedRow, 1); // La référence est à l'index 1
                int confirm = JOptionPane.showConfirmDialog(this, 
                                                            "Voulez-vous vraiment supprimer le produit '" + referenceToDelete + "' ?", 
                                                            "Confirmer Suppression", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try { // NOUVEAU: Ajout d'un try-catch pour SQLException ici
                        boolean deleted = pharmacie.supprimerProduit(referenceToDelete);
                        if (deleted) {
                            JOptionPane.showMessageDialog(this, "Produit supprimé avec succès !");
                            remplirTable(); // Rafraîchir le tableau après suppression
                            if (dataListener != null) {
                                dataListener.onPharmacieDataChanged(); // Notifier les autres vues
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "Erreur lors de la suppression du produit.", "Erreur", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) { // Capture spécifique de l'erreur SQL
                        JOptionPane.showMessageDialog(this, 
                                                      "Erreur de base de données lors de la suppression: " + ex.getMessage(), 
                                                      "Erreur SQL", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    } catch (Exception ex) { // Capture des autres exceptions inattendues
                        JOptionPane.showMessageDialog(this, 
                                                      "Une erreur inattendue est survenue lors de la suppression: " + ex.getMessage(), 
                                                      "Erreur", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un produit à supprimer.", "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            }
        });
        controlPanel.add(deleteButton);


        add(controlPanel, BorderLayout.SOUTH);

        // Remplir le tableau initialement
        remplirTable();
    }

    // Méthode pour remplir ou rafraîchir le tableau avec les données de la BDD
    public void remplirTable() {
        tableModel.setRowCount(0); // Vider le tableau existant
        
        try {
            List<Produit> produits = pharmacie.getProduits(); // Récupère tous les produits depuis la BDD via Pharmacie

            for (Produit p : produits) {
                Object[] rowData = new Object[columnNames.length];
                rowData[0] = p.getId();             // ID
                rowData[1] = p.getReference();      // Référence
                rowData[2] = p.getNom();            // Nom
                rowData[3] = p.getDescription();    // NOUVEAU: Description
                rowData[5] = String.format("%.2f", p.getPrixHt()); // Prix HT
                rowData[6] = p.getQuantite();       // Quantité
                rowData[7] = String.format("%.2f", p.calculerPrixTTC()); // Prix TTC

                if (p instanceof Medicament) {
                    Medicament m = (Medicament) p;
                    rowData[4] = "Médicament"; // Type (index 4 car description est à l'index 3)
                    rowData[8] = m.isGenerique() ? "Oui" : "Non";       // Générique
                    rowData[9] = m.isSurOrdonnance() ? "Oui" : "Non";   // Ordonnance
                    rowData[10] = "N/A"; // Pas de catégorie pour les médicaments
                } else if (p instanceof ProduitParaPharmacie) {
                    ProduitParaPharmacie pp = (ProduitParaPharmacie) p;
                    rowData[4] = "Parapharmacie"; // Type
                    rowData[8] = "N/A"; // Générique (N/A)
                    rowData[9] = "N/A"; // Ordonnance (N/A)
                    rowData[10] = pp.getCategorie(); // Catégorie spécifique (Utilise getCategorie())
                } else {
                    rowData[4] = "Produit Général"; // Pour les cas non-spécifiques
                    rowData[8] = "N/A";
                    rowData[9] = "N/A";
                    rowData[10] = "N/A";
                }
                tableModel.addRow(rowData);
            }
        }
        catch (SQLException ex) {
            // Afficher un message d'erreur à l'utilisateur si la récupération échoue
            JOptionPane.showMessageDialog(this, 
                                          "Erreur lors du chargement des produits depuis la base de données: " + ex.getMessage(), 
                                          "Erreur de Base de Données", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Imprimer la trace pour le débogage
        }
    }
}
