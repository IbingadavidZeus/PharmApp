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

    private JLabel totalStockValueLabel;
    
    private final String[] columnNames = {"ID", "Référence", "Nom", "Description", "Type", "Prix HT", "Quantité", "Prix TTC", "Générique", "Ordonnance", "Catégorie Para."};
    public StockPanel(Pharmacie pharmacie, PharmacieDataListener listener) {
        this.pharmacie = pharmacie;
        this.dataListener = listener;
        setLayout(new BorderLayout());

        // --- Configuration du tableau ---
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setFillsViewportHeight(true);
        productTable.setAutoCreateRowSorter(true);
        
        productTable.setRowHeight(24); 

        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Boutons et panel de contrôle ---
        JPanel controlPanel = new JPanel(new BorderLayout());
        JPanel buttonRowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshButton = new JButton("Rafraîchir le stock");
        refreshButton.addActionListener(_ -> remplirTable());
        buttonRowPanel.add(refreshButton);

        JButton deleteButton = new JButton("Supprimer Produit Sélectionné");
        deleteButton.addActionListener(_ -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow != -1) {
                String referenceToDelete = (String) tableModel.getValueAt(selectedRow, 1);
                int confirm = JOptionPane.showConfirmDialog(this, 
                                                            "Voulez-vous vraiment supprimer le produit '" + referenceToDelete + "' ?", 
                                                            "Confirmer Suppression", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        boolean deleted = pharmacie.supprimerProduit(referenceToDelete);
                        if (deleted) {
                            JOptionPane.showMessageDialog(this, "Produit supprimé avec succès !");
                            remplirTable();
                            if (dataListener != null) {
                                dataListener.onPharmacieDataChanged();
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "Erreur lors de la suppression du produit.", "Erreur", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, 
                                                      "Erreur de base de données lors de la suppression: " + ex.getMessage(), 
                                                      "Erreur SQL", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    } catch (Exception ex) {
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
        buttonRowPanel.add(deleteButton);

        controlPanel.add(buttonRowPanel, BorderLayout.CENTER);

        
        totalStockValueLabel = new JLabel("Valeur totale du stock : Calcul en cours...");
        totalStockValueLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalStockValueLabel.setForeground(Color.BLUE);
        totalStockValueLabel.setHorizontalAlignment(SwingConstants.LEFT); 
        controlPanel.add(totalStockValueLabel, BorderLayout.SOUTH); 

        add(controlPanel, BorderLayout.SOUTH);
        remplirTable();
    }
    public void remplirTable() {
        tableModel.setRowCount(0); 
        
        try {
            List<Produit> produits = pharmacie.getProduits();

            for (Produit p : produits) {
                Object[] rowData = new Object[columnNames.length];
                rowData[0] = p.getId();
                rowData[1] = p.getReference();
                rowData[2] = p.getNom();
                rowData[3] = p.getDescription();
                rowData[5] = String.format("%.2f", p.getPrixHt());
                rowData[6] = p.getQuantite();
                rowData[7] = String.format("%.2f", p.calculerPrixTTC());

                if (p instanceof Medicament) {
                    Medicament m = (Medicament) p;
                    rowData[4] = "Médicament";
                    rowData[8] = m.isGenerique() ? "Oui" : "Non";
                    rowData[9] = m.isSurOrdonnance() ? "Oui" : "Non";
                    rowData[10] = "N/A";
                } else if (p instanceof ProduitParaPharmacie) {
                    ProduitParaPharmacie pp = (ProduitParaPharmacie) p;
                    rowData[4] = "Parapharmacie";
                    rowData[8] = "N/A";
                    rowData[9] = "N/A";
                    rowData[10] = pp.getCategorie();
                } else {
                    rowData[4] = "Produit Général";
                    rowData[8] = "N/A";
                    rowData[9] = "N/A";
                    rowData[10] = "N/A";
                }
                tableModel.addRow(rowData);
            }
            updateTotalStockValue();

        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                                          "Erreur lors du chargement des produits depuis la base de données: " + ex.getMessage(), 
                                          "Erreur de Base de Données", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateTotalStockValue() {
        try {
            double totalValue = pharmacie.calculerValeurTotaleStock();
            totalStockValueLabel.setText(String.format("Valeur totale du stock : %.2f FCFA", totalValue));
            totalStockValueLabel.setForeground(Color.BLUE);
        } catch (SQLException e) {
            totalStockValueLabel.setText("Valeur totale du stock : Erreur de calcul.");
            totalStockValueLabel.setForeground(Color.RED);
            e.printStackTrace();
        }
    }
}
