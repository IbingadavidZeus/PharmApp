package view;

import model.CompteComptable;
import model.Pharmacie;
import model.TransactionComptable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator; // Pour le tri
import java.util.List;
import java.util.stream.Collectors; // Pour Java 8+ stream API

public class ComptabilitePanel extends JPanel {
    private Pharmacie pharmacie;
    private PharmacieDataListener dataListener;

    // Composants pour le Grand Livre
    private JTable grandLivreTable;
    private DefaultTableModel grandLivreTableModel;
    private JLabel grandLivreMessageLabel;

    // Composants pour le Journal des Transactions
    private JTable journalTable;
    private DefaultTableModel journalTableModel;
    private JLabel journalMessageLabel;
    
    private JButton printButton; // Bouton d'impression

    // Colonnes
    private final String[] grandLivreColumnNames = {"Numéro Compte", "Nom Compte", "Type", "Solde"};
    private final String[] journalColumnNames = {"ID Trans.", "Date", "Référence", "Description", "Montant", "Compte Débit", "Compte Crédit", "Source"};

    public ComptabilitePanel(Pharmacie pharmacie, PharmacieDataListener dataListener) {
        this.pharmacie = pharmacie;
        this.dataListener = dataListener;
        initUI();
        refreshAccountingData(); // Charger les données au démarrage
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- Grand Livre Section (NORTH) ---
        JPanel grandLivrePanel = new JPanel(new BorderLayout(5, 5));
        grandLivrePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(65, 105, 225)), // Bleu royal
            "Grand Livre des Comptes (Soldes)", // Titre
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14), new Color(65, 105, 225)
        ));

        grandLivreTableModel = new DefaultTableModel(grandLivreColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return Double.class; // Solde
                return Object.class;
            }
        };
        grandLivreTable = new JTable(grandLivreTableModel);
        grandLivreTable.setAutoCreateRowSorter(true);
        grandLivreTable.getTableHeader().setReorderingAllowed(false); // Empêche le déplacement des colonnes
        grandLivrePanel.add(new JScrollPane(grandLivreTable), BorderLayout.CENTER);

        grandLivreMessageLabel = new JLabel(" ", SwingConstants.CENTER);
        grandLivreMessageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        grandLivreMessageLabel.setForeground(Color.BLACK);
        grandLivrePanel.add(grandLivreMessageLabel, BorderLayout.SOUTH);

        add(grandLivrePanel, BorderLayout.NORTH);

        // --- Journal des Transactions Section (CENTER) ---
        JPanel journalPanel = new JPanel(new BorderLayout(5, 5));
        journalPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(34, 139, 34)), // Vert forêt
            "Journal des Transactions (Écritures Détaillées)", // Titre
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14), new Color(34, 139, 34)
        ));

        journalTableModel = new DefaultTableModel(journalColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return Double.class; // Montant
                return Object.class;
            }
        };
        journalTable = new JTable(journalTableModel);
        journalTable.setAutoCreateRowSorter(true);
        journalTable.getTableHeader().setReorderingAllowed(false);
        journalPanel.add(new JScrollPane(journalTable), BorderLayout.CENTER);

        journalMessageLabel = new JLabel(" ", SwingConstants.CENTER);
        journalMessageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        journalMessageLabel.setForeground(Color.BLACK);
        journalPanel.add(journalMessageLabel, BorderLayout.SOUTH);

        add(journalPanel, BorderLayout.CENTER);

        // --- Panneau des actions (inclut le bouton imprimer) (SOUTH) ---
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10)); // Alignement à droite
        printButton = new JButton("Imprimer les Rapports Comptables");
        printButton.setBackground(new Color(70, 130, 180)); // SteelBlue
        printButton.setForeground(Color.WHITE);
        printButton.setFont(new Font("Arial", Font.BOLD, 14));
        printButton.addActionListener(e -> generateAndDisplayAccountingReport());
        actionsPanel.add(printButton);

        add(actionsPanel, BorderLayout.SOUTH);
    }

    /**
     * Rafraîchit toutes les données comptables affichées dans le panel.
     * Appelé au démarrage et lorsque des données de la pharmacie changent.
     */
    public void refreshAccountingData() {
        populateGrandLivreTable();
        populateJournalTable();
    }

    /**
     * Remplit la table du Grand Livre avec les soldes des comptes.
     */
    private void populateGrandLivreTable() {
        grandLivreTableModel.setRowCount(0); // Vider la table
        try {
            List<CompteComptable> comptes = pharmacie.getAllComptesComptables();
            if (comptes.isEmpty()) {
                grandLivreMessageLabel.setText("Aucun compte comptable trouvé.");
                grandLivreMessageLabel.setForeground(Color.ORANGE);
                return;
            }

            for (CompteComptable compte : comptes) {
                // Calculer le solde pour chaque compte
                double solde = calculateCompteBalance(compte);
                grandLivreTableModel.addRow(new Object[]{
                    compte.getNumeroCompte(),
                    compte.getNomCompte(),
                    compte.getTypeCompte(),
                    solde 
                });
            }
            grandLivreMessageLabel.setText("Grand Livre mis à jour. " + comptes.size() + " comptes affichés.");
            grandLivreMessageLabel.setForeground(Color.BLACK);

        } catch (SQLException e) {
            grandLivreMessageLabel.setText("Erreur lors du chargement du Grand Livre: " + e.getMessage());
            grandLivreMessageLabel.setForeground(Color.RED);
            e.printStackTrace();
        }
    }

    /**
     * Calcule le solde d'un compte donné.
     * Pour un compte d'ACTIF ou CHARGE, Solde = Débits - Crédits.
     * Pour un compte de PASSIF ou PRODUIT, Solde = Crédits - Débits.
     * @param compte Le CompteComptable pour lequel calculer le solde.
     * @return Le solde du compte.
     */
    private double calculateCompteBalance(CompteComptable compte) throws SQLException {
        double totalDebits = 0;
        double totalCredits = 0;
        List<TransactionComptable> transactions = pharmacie.getTransactionsComptablesByCompte(compte.getId_compteComptable());

        for (TransactionComptable trans : transactions) {
            if (trans.getCompteDebit() != null && trans.getCompteDebit().getId_compteComptable() == compte.getId_compteComptable()) {
                totalDebits += trans.getMontant();
            }
            if (trans.getCompteCredit() != null && trans.getCompteCredit().getId_compteComptable() == compte.getId_compteComptable()) {
                totalCredits += trans.getMontant();
            }
        }

        switch (compte.getTypeCompte()) { // Utilise getTypeCompte()
            case "ACTIF":
            case "CHARGE":
                return totalDebits - totalCredits;
            case "PASSIF":
            case "PRODUIT":
            case "CAPITAL":
                return totalCredits - totalDebits;
            default:
                return totalDebits - totalCredits; 
        }
    }

    /**
     * Remplit la table du Journal des Transactions.
     */
    private void populateJournalTable() {
        journalTableModel.setRowCount(0); // Vider la table
        try {
            List<TransactionComptable> transactions = pharmacie.getAllTransactionsComptables();
            if (transactions.isEmpty()) {
                journalMessageLabel.setText("Aucune transaction comptable trouvée.");
                journalMessageLabel.setForeground(Color.ORANGE);
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (TransactionComptable trans : transactions) {
                journalTableModel.addRow(new Object[]{
                    trans.getId_transaction(),
                    trans.getDateTransaction().format(formatter),
                    trans.getReferencePiece() != null ? trans.getReferencePiece() : "N/A",
                    trans.getDescriptionTransaction(),
                    trans.getMontant(),
                    trans.getCompteDebit() != null ? trans.getCompteDebit().getNumeroCompte() : "N/A",
                    trans.getCompteCredit() != null ? trans.getCompteCredit().getNumeroCompte() : "N/A",
                    trans.getSourceType() != null ? trans.getSourceType() : "N/A"
                });
            }
            journalMessageLabel.setText("Journal des transactions mis à jour. " + transactions.size() + " transactions affichées.");
            journalMessageLabel.setForeground(Color.BLACK);

        } catch (SQLException e) {
            journalMessageLabel.setText("Erreur lors du chargement du Journal des Transactions: " + e.getMessage());
            journalMessageLabel.setForeground(Color.RED);
            e.printStackTrace();
        }
    }

    /**
     * Génère un rapport textuel du Grand Livre et du Journal des Transactions,
     * puis l'affiche dans une boîte de dialogue avec des options d'impression.
     */
    private void generateAndDisplayAccountingReport() {
        StringBuilder report = new StringBuilder();
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Définir la largeur totale pour le formatage du rapport
        int reportWidth = 120; // Réduit la largeur pour un meilleur affichage global sur des terminaux standards
        int journalWidth = 160; // Garde le journal plus large pour les descriptions

        String separator = "=".repeat(reportWidth);
        String subSeparator = "-".repeat(reportWidth);
        String journalSeparator = "-".repeat(journalWidth);


        report.append(separator).append("\n");
        report.append(String.format("%" + (reportWidth / 2 + "RAPPORT COMPTABLE DE LA PHARMACIE".length() / 2) + "s\n", "RAPPORT COMPTABLE DE LA PHARMACIE"));
        report.append(String.format("%" + (reportWidth / 2 + ("Date de Génération: " + LocalDateTime.now().format(fullFormatter)).length() / 2) + "s\n", "Date de Génération: " + LocalDateTime.now().format(fullFormatter)));
        report.append(separator).append("\n\n");

        try {
            // --- SECTION GRAND LIVRE ---
            report.append(subSeparator).append("\n");
            report.append(String.format("%" + (reportWidth / 2 + "GRAND LIVRE DES COMPTES".length() / 2) + "s\n", "GRAND LIVRE DES COMPTES"));
            report.append(subSeparator).append("\n");
            
            // Largeurs de colonnes ajustées pour le Grand Livre (total 95 chars, reste identique car ok)
            report.append(String.format("%-15s %-45s %-15s %18s\n", "N° Compte", "Nom Compte", "Type", "Solde (FCFA)"));
            report.append(subSeparator).append("\n");

            List<CompteComptable> comptes = pharmacie.getAllComptesComptables();
            comptes.sort(Comparator.comparing(CompteComptable::getNumeroCompte));

            for (CompteComptable compte : comptes) {
                double solde = calculateCompteBalance(compte);
                report.append(String.format("%-15s %-45s %-15s %18.2f\n",
                    compte.getNumeroCompte(),
                    compte.getNomCompte(),
                    compte.getTypeCompte(),
                    solde
                ));
            }
            report.append(subSeparator).append("\n\n");

            // --- SECTION JOURNAL DES TRANSACTIONS ---
            report.append(journalSeparator).append("\n");
            report.append(String.format("%" + (journalWidth / 2 + "JOURNAL DES TRANSACTIONS".length() / 2) + "s\n", "JOURNAL DES TRANSACTIONS"));
            report.append(journalSeparator).append("\n");
            
            // Largeurs de colonnes ajustées pour le Journal (total ~158 chars)
            // ID (8), Date (20), Référence (20), Description (45), Montant (15), Débit (12), Crédit (12), Source (15)
            report.append(String.format("%-8s %-20s %-20s %-45s %15s %-12s %-12s %-15s\n",
                "ID", "Date", "Référence", "Description", "Montant", "Débit", "Crédit", "Source"
            ));
            report.append(journalSeparator).append("\n");

            List<TransactionComptable> transactions = pharmacie.getAllTransactionsComptables();
            transactions.sort(Comparator.comparing(TransactionComptable::getDateTransaction));

            for (TransactionComptable trans : transactions) {
                // Tronquer la description pour maintenir l'alignement
                String description = trans.getDescriptionTransaction();
                if (description.length() > 45) { // Tronque à 45 caractères max
                    description = description.substring(0, 42) + "...";
                }
                String referencePiece = trans.getReferencePiece() != null ? trans.getReferencePiece() : "N/A";
                if (referencePiece.length() > 20) {
                    referencePiece = referencePiece.substring(0, 17) + "...";
                }

                report.append(String.format("%-8d %-20s %-20s %-45s %15.2f %-12s %-12s %-15s\n",
                    trans.getId_transaction(),
                    trans.getDateTransaction().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")),
                    referencePiece,
                    description,
                    trans.getMontant(),
                    trans.getCompteDebit() != null ? trans.getCompteDebit().getNumeroCompte() : "N/A",
                    trans.getCompteCredit() != null ? trans.getCompteCredit().getNumeroCompte() : "N/A",
                    trans.getSourceType() != null ? trans.getSourceType() : "N/A"
                ));
            }
            report.append(journalSeparator).append("\n");

            displayReportDialog(report.toString());

        } catch (SQLException e) {
            setMessage("Erreur lors de la génération du rapport comptable: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        }
    }

    /**
     * Affiche le rapport généré dans une boîte de dialogue avec options d'action.
     * @param reportContent Le contenu textuel du rapport.
     */
    private void displayReportDialog(String reportContent) {
        JTextArea textArea = new JTextArea(reportContent);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Police monospace pour l'alignement
        textArea.setTabSize(4); // Définir la taille des tabulations si des \t sont utilisés

        JScrollPane scrollPane = new JScrollPane(textArea);
        // Ajuste la taille de la fenêtre du rapport pour être un peu plus large afin de mieux voir le journal
        scrollPane.setPreferredSize(new Dimension(880, 600)); 

        // Boutons pour les actions
        JButton copyButton = new JButton("Copier dans le Presse-papiers");
        copyButton.addActionListener(e -> copyToClipboard(reportContent));
        
        JButton saveButton = new JButton("Enregistrer sous...");
        saveButton.addActionListener(e -> saveReportToFile(reportContent));

        // Panneau des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(copyButton);
        buttonPanel.add(saveButton);

        // Panneau principal de la boîte de dialogue
        JPanel dialogContent = new JPanel(new BorderLayout());
        dialogContent.add(scrollPane, BorderLayout.CENTER);
        dialogContent.add(buttonPanel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, dialogContent, "Rapport Comptable", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Copie le texte donné dans le presse-papiers du système.
     * @param text Le texte à copier.
     */
    private void copyToClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        JOptionPane.showMessageDialog(this, "Rapport copié dans le presse-papiers !");
    }

    /**
     * Permet à l'utilisateur d'enregistrer le rapport dans un fichier texte.
     * @param reportContent Le contenu textuel du rapport.
     */
    private void saveReportToFile(String reportContent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer le Rapport Comptable");
        // Suggérer un nom de fichier par défaut
        String defaultFileName = "RapportComptable_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
        fileChooser.setSelectedFile(new java.io.File(defaultFileName));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write(reportContent);
                JOptionPane.showMessageDialog(this, "Rapport enregistré avec succès dans: " + fileToSave.getAbsolutePath(), "Enregistrement Réussi", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement du fichier: " + ex.getMessage(), "Erreur d'Enregistrement", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Affiche un message à l'utilisateur avec la couleur spécifiée.
     * @param msg Le message à afficher.
     * @param color La couleur du texte du message.
     */
    private void setMessage(String msg, Color color) {
        if (journalMessageLabel != null) {
            journalMessageLabel.setText(msg);
            journalMessageLabel.setForeground(color);
        } else if (grandLivreMessageLabel != null) {
            grandLivreMessageLabel.setText(msg);
            grandLivreMessageLabel.setForeground(color);
        } else {
            System.out.println("ComptabilitePanel Message: " + msg);
        }
    }
}
