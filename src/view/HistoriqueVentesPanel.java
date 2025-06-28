package view;

import model.Facture;
import model.LigneFacture;
import model.Pharmacie;
import model.Utilisateur;
import model.AssuranceSocial; // Ajouté pour les détails de l'assurance

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects; // Ajouté pour Objects.equals
import java.util.stream.Collectors;

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
        filterButton.addActionListener(e -> applyFilter());
        filterPanel.add(filterButton);

        resetButton = new JButton("Réinitialiser Filtres");
        resetButton.addActionListener(e -> resetFilters());
        filterPanel.add(resetButton);

        add(filterPanel, BorderLayout.NORTH);

        // --- Tableau des Factures (Centre) ---
        facturesTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class; // ID Facture
                if (columnIndex == 4) return String.class; // Total TTC (formatted string)
                return Object.class;
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
        viewDetailsButton.addActionListener(e -> viewFactureDetails());
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
        utilisateurFilterComboBox.addItem(null); // Option pour "Tous les vendeurs"
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
            
            // Logique de filtrage combinée
            if (startDate != null && endDate != null) {
                factures = pharmacie.getFacturesByDateRange(startDate, endDate);
                if (selectedUser != null) {
                    factures = factures.stream()
                                       .filter(f -> Objects.equals(f.getUtilisateur().getId(), selectedUser.getId()))
                                       .collect(Collectors.toList());
                }
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
                // Début de la refonte du design de la facture pour l'affichage des détails
                StringBuilder details = new StringBuilder();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

                int factureDetailsWidth = 75; // Largeur pour les détails de la facture

                String separator = "=".repeat(factureDetailsWidth);
                String thinSeparator = "-".repeat(factureDetailsWidth);

                details.append(separator).append("\n");
                details.append(centerText("DÉTAILS DE LA FACTURE", factureDetailsWidth)).append("\n");
                details.append(separator).append("\n");

                details.append(String.format("%-25s %s\n", "ID Facture:", facture.getId()));
                details.append(String.format("%-25s %s\n", "Numéro Facture:", facture.getNumeroFacture()));
                details.append(String.format("%-25s %s\n", "Date & Heure:", facture.getDateFacture().format(formatter)));
                details.append(String.format("%-25s %s\n", "Vendu par:", facture.getUtilisateur() != null ? facture.getUtilisateur().getNomUtilisateur() : "N/A")).append("\n");

                details.append(thinSeparator).append("\n");
                details.append(centerText("PRODUITS VENDUS", factureDetailsWidth)).append("\n");
                details.append(thinSeparator).append("\n");

                // En-tête des produits: Produit (35), Qté (5), Prix U. (12), Sous-Total (15) = Total 67 + 3 espaces
                details.append(String.format("%-35s %5s %12s %15s\n", "Produit", "Qté", "Prix U. TTC", "Sous-Total TTC"));
                details.append(thinSeparator).append("\n");

                for (LigneFacture ligne : facture.getLignesFacture()) {
                    String productName = ligne.getProduit().getNom();
                    if (productName.length() > 35) {
                        productName = productName.substring(0, 32) + "...";
                    }
                    details.append(String.format("%-35s %5d %12.2f %15.2f\n",
                            productName,
                            ligne.getQuantite(),
                            ligne.getPrixUnitaire(), // Ceci est déjà le prix TTC unitaire
                            ligne.getSousTotal()));
                }
                details.append(thinSeparator).append("\n\n");

                // Résumé des totaux
                details.append(String.format("%-45s %15.2f FCFA\n", "Total HT:", facture.getTotalHt()));
                // Calcule la TVA si elle n'est pas un attribut direct de la facture
                double tvaAmount = facture.getMontantTotal() - facture.getTotalHt();
                details.append(String.format("%-45s %15.2f FCFA\n", "TVA (18%):", tvaAmount));
                details.append(String.format("%-45s %15.2f FCFA\n", "TOTAL GLOBAL (TTC):", facture.getMontantTotal())).append("\n");

                // Section Assurance (si présente)
                if (facture.getAssuranceSocial() != null) { // Utilisez getAssuranceSocial() pour votre modèle
                    details.append(thinSeparator).append("\n");
                    details.append(String.format("Assurance Sociale: %s\n", facture.getAssuranceSocial().getNom_assurance())); // Utilisez getNom_assurance()
                    details.append(String.format("Taux de Prise en Charge: %.0f%%\n", facture.getAssuranceSocial().getTauxDePriseEnCharge() * 100));
                    details.append(String.format("%-45s %15.2f FCFA\n", "Montant pris en charge par assurance:", facture.getMontantPrisEnChargeAssurance())).append("\n");
                }

                details.append(thinSeparator).append("\n");
                details.append(String.format("%-45s %15.2f FCFA\n", "MONTANT CLIENT À PAYER:", facture.getMontantRestantAPayerClient())).append("\n");
                details.append(separator).append("\n");
                details.append(centerText("FIN DES DÉTAILS", factureDetailsWidth)).append("\n");
                details.append(separator).append("\n");


                JTextArea textArea = new JTextArea(details.toString());
                textArea.setEditable(false);
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Police pour l'alignement
                textArea.setTabSize(4); // Assurez-vous que les tabulations sont gérées si utilisées

                JScrollPane scrollPaneDialog = new JScrollPane(textArea);
                scrollPaneDialog.setPreferredSize(new Dimension(700, 500)); // Taille de la boîte de dialogue

                JOptionPane.showMessageDialog(this, scrollPaneDialog, "Détails de la Facture #" + facture.getNumeroFacture(),
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
            messageLabel.setText("Une erreur inattendue est survenue lors de l'affichage des détails: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace();
        }
    }

    /**
     * Centre un texte donné dans une largeur spécifiée.
     * C'est une méthode utilitaire pour le formatage du rapport.
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
}
