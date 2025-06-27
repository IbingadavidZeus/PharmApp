package view;

import model.CompteComptable;
import model.Pharmacie;
import model.TransactionComptable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
                    solde // Le solde sera affiché avec deux décimales par le TableCellRenderer par défaut si Double
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

        switch (compte.getTypeCompte()) { // Assurez-vous que CompteComptable a getTypeComptable()
            case "ACTIF":
            case "CHARGE":
                return totalDebits - totalCredits;
            case "PASSIF":
            case "PRODUIT":
            case "CAPITAL": // Ou tout autre type qui augmente au crédit
                return totalCredits - totalDebits;
            default:
                return totalDebits - totalCredits; // Comportement par défaut
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
}
