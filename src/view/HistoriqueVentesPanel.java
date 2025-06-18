package view;

import model.Facture;
import model.LigneFacture;
import model.Pharmacie;
import model.Utilisateur;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class HistoriqueVentesPanel extends JPanel {
    private Pharmacie pharmacie;

    // Composants de filtrage

    private JTextField startDateField;
    private JTextField endDateField;
    private JComboBox<Utilisateur> utilisateurFilterComboBox;
    private JButton filterButton;
    private JButton resetButton;

    // Tableau des factures

    private JTable facturesTable;
    private DefaultTableModel facturesTableModel;
    private final String[] columnNames = { "ID Facture", "Numéro Facture", "Date et Heure", "Vendu par", "Total TTC" };

    // Bouton pour voir les détails

    private JButton viewDetailsButton;

    // Message d'information/erreur

    private JLabel messageLabel;

    public HistoriqueVentesPanel(Pharmacie pharmacie) {
        this.pharmacie = pharmacie;
        initUI();
        loadUtilisateursForFilter();
        refreshFacturesTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // --- Panel de Filtrage (Nord) ---

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        filterPanel.add(new JLabel("Date de début (JJ/MM/AAAA HH:MM):"));
        startDateField = new JTextField(15);
        filterPanel.add(startDateField);

        filterPanel.add(new JLabel("Date de fin (JJ/MM/AAAA HH:MM):"));
        endDateField = new JTextField(15);
        filterPanel.add(endDateField);

        filterPanel.add(new JLabel("Vendeur:"));
        utilisateurFilterComboBox = new JComboBox<>();
        filterPanel.add(utilisateurFilterComboBox);

        filterButton = new JButton("Filtrer");
        filterButton.addActionListener(_ -> applyFilter());
        filterPanel.add(filterButton);

        resetButton = new JButton("Réinitialiser Filtres");
        resetButton.addActionListener(_ -> resetFilters());
        filterPanel.add(resetButton);

        add(filterPanel, BorderLayout.NORTH);

        // --- Tableau des Factures (Centre) ---

        facturesTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        facturesTable = new JTable(facturesTableModel);
        facturesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        facturesTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(facturesTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Panel Boutons et Message (Sud) ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        viewDetailsButton = new JButton("Voir Détails Facture");
        viewDetailsButton.addActionListener(_ -> viewFactureDetails());
        buttonPanel.add(viewDetailsButton);

        bottomPanel.add(buttonPanel, BorderLayout.NORTH);

        messageLabel = new JLabel(" ");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setForeground(Color.BLUE);
        bottomPanel.add(messageLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Charge tous les utilisateurs dans la ComboBox de filtrage.
     */
    private void loadUtilisateursForFilter() {
        utilisateurFilterComboBox.removeAllItems();
        utilisateurFilterComboBox.addItem(null);
        try {
            List<Utilisateur> users = pharmacie.getAllUtilisateurs();
            for (Utilisateur user : users) {
                utilisateurFilterComboBox.addItem(user);
            }
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors du chargement des vendeurs: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace();
        }
    }

    /**
     * Rafraîchit le tableau des factures en appliquant les filtres si renseignés,
     * sinon affiche toutes les factures.
     */
    public void refreshFacturesTable() {
        facturesTableModel.setRowCount(0);
        messageLabel.setText("Chargement des factures...");
        messageLabel.setForeground(Color.BLUE);

        try {
            List<Facture> factures;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime startDate = null;
            LocalDateTime endDate = null;
            try {
                if (!startDateField.getText().trim().isEmpty()) {
                    startDate = LocalDateTime.parse(startDateField.getText().trim(), formatter);
                }
                if (!endDateField.getText().trim().isEmpty()) {
                    endDate = LocalDateTime.parse(endDateField.getText().trim(), formatter);
                }
            } catch (DateTimeParseException e) {
                messageLabel.setText("Format de date/heure invalide. Utilisez JJ/MM/AAAA HH:MM.");
                messageLabel.setForeground(Color.RED);
                return;
            }

            Utilisateur selectedUser = (Utilisateur) utilisateurFilterComboBox.getSelectedItem();
            if (startDate != null && endDate != null) {
                factures = pharmacie.getFacturesByDateRange(startDate, endDate);
            } else if (selectedUser != null) {
                factures = pharmacie.getFacturesByUtilisateur(selectedUser);
            } else {
                factures = pharmacie.getAllFactures();
            }

            for (Facture f : factures) {
                Object[] rowData = new Object[] {
                        f.getId(),
                        f.getNumeroFacture(),
                        f.getDateFacture().format(formatter),
                        f.getUtilisateur() != null ? f.getUtilisateur().getNomUtilisateur() : "N/A",
                        String.format("%.2f FCFA", f.getMontantTotal())
                };
                facturesTableModel.addRow(rowData);
            }
            messageLabel.setText(factures.size() + " facture(s) trouvée(s).");
            messageLabel.setForeground(Color.BLACK);

        } catch (SQLException e) {
            messageLabel.setText("Erreur SQL lors du chargement des factures: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace();
        } catch (Exception e) {
            messageLabel.setText("Une erreur inattendue est survenue: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace();
        }
    }

    /**
     * Applique les filtres sélectionnés et rafraîchit le tableau.
     */
    private void applyFilter() {
        refreshFacturesTable();
    }

    /**
     * Réinitialise les champs de filtre et rafraîchit le tableau pour afficher
     * toutes les factures.
     */
    private void resetFilters() {
        startDateField.setText("");
        endDateField.setText("");
        utilisateurFilterComboBox.setSelectedItem(null);
        refreshFacturesTable();
    }

    /**
     * Affiche les détails d'une facture sélectionnée dans une boîte de dialogue.
     */
    private void viewFactureDetails() {
        int selectedRow = facturesTable.getSelectedRow();
        if (selectedRow == -1) {
            messageLabel.setText("Veuillez sélectionner une facture à visualiser.");
            messageLabel.setForeground(Color.ORANGE);
            return;
        }

        try {
            int factureId = (int) facturesTableModel.getValueAt(selectedRow, 0);
            Facture facture = pharmacie.getFactureById(factureId);

            if (facture != null) {
                StringBuilder details = new StringBuilder();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

                details.append("FACTURE DÉTAILS\n");
                details.append("--------------------------------------------------\n");
                details.append("ID Facture:       ").append(facture.getId()).append("\n");
                details.append("Numéro Facture:   ").append(facture.getNumeroFacture()).append("\n");
                details.append("Date:             ").append(facture.getDateFacture().format(formatter)).append("\n");
                details.append("Vendu par:        ").append(facture.getUtilisateur().getNomUtilisateur()).append("\n");
                details.append("Total HT:         ").append(String.format("%.2f FCFA", facture.getTotalHt()))
                        .append("\n");
                details.append("Total TTC:        ").append(String.format("%.2f FCFA", facture.getMontantTotal()))
                        .append("\n");
                details.append("--------------------------------------------------\n");
                details.append("Produits :\n");
                details.append(String.format("%-25s %8s %12s %15s\n", "Nom Produit", "Qté", "Prix U.", "Sous-Total"));
                details.append("--------------------------------------------------\n");

                for (LigneFacture ligne : facture.getLignesFacture()) {
                    details.append(String.format("%-25.25s %8d %12.2f %15.2f\n",
                            ligne.getProduit().getNom(),
                            ligne.getQuantite(),
                            ligne.getPrixUnitaire(),
                            ligne.getSousTotal()));
                }
                details.append("--------------------------------------------------\n");
                details.append("Total TTC: ").append(String.format("%.2f FCFA", facture.getMontantTotal()));

                JTextArea textArea = new JTextArea(details.toString());
                textArea.setEditable(false);
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(600, 400));

                JOptionPane.showMessageDialog(this, scrollPane, "Détails de la Facture #" + facture.getNumeroFacture(),
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                messageLabel.setText("Facture introuvable.");
                messageLabel.setForeground(Color.RED);
            }
        } catch (SQLException e) {
            messageLabel.setText("Erreur SQL lors de la récupération des détails de la facture: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace();
        } catch (Exception e) {
            messageLabel.setText("Une erreur inattendue est survenue: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace();
        }
    }
}
