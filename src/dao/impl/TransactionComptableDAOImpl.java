package dao.impl;

import dao.TransactionComptableDAO;
import dao.CompteComptableDAO; 
import dao.DatabaseManager;
import model.TransactionComptable;
import model.CompteComptable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types; // Ajouté pour Types.INTEGER
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionComptableDAOImpl implements TransactionComptableDAO {

    private CompteComptableDAO compteComptableDAO;

    public TransactionComptableDAOImpl(CompteComptableDAO compteComptableDAO) {
        this.compteComptableDAO = compteComptableDAO;
    }

    // Méthode pour ajouter une transaction, gérant sa propre connexion
    @Override
    public boolean addTransaction(TransactionComptable transaction) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Début de la transaction pour garantir l'atomicité
            boolean success = addTransaction(conn, transaction); // Délègue à la méthode transactionnelle
            if (success) {
                conn.commit();
            } else {
                conn.rollback();
            }
            return success;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erreur lors du rollback: " + ex.getMessage());
                }
            }
            throw e; // Relaisser l'exception après rollback
        } finally {
            DatabaseManager.close(conn, null); // Ferme la connexion ici
        }
    }

    // Méthode pour ajouter une transaction dans une transaction existante
    @Override
    public boolean addTransaction(Connection conn, TransactionComptable transaction) throws SQLException {
        String sql = "INSERT INTO transactions_comptables (date_transaction, reference_piece, description_transaction, montant, id_compte_debit, id_compte_credit, source_type, source_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setTimestamp(1, Timestamp.valueOf(transaction.getDateTransaction()));
            pstmt.setString(2, transaction.getReferencePiece());
            pstmt.setString(3, transaction.getDescriptionTransaction());
            pstmt.setDouble(4, transaction.getMontant());

            // --- NOUVEAU: Vérifications défensives pour éviter la NullPointerException ici ---
            if (transaction.getCompteDebit() == null) {
                throw new IllegalArgumentException("Le compte de débit de la transaction ne peut pas être null. Vérifiez la création de l'objet TransactionComptable.");
            }
            if (transaction.getCompteCredit() == null) {
                throw new IllegalArgumentException("Le compte de crédit de la transaction ne peut pas être null. Vérifiez la création de l'objet TransactionComptable.");
            }
            // --- FIN NOUVEAU ---

            pstmt.setInt(5, transaction.getCompteDebit().getId_compteComptable());
            pstmt.setInt(6, transaction.getCompteCredit().getId_compteComptable());
            pstmt.setString(7, transaction.getSourceType());
            
            // Gérer le cas où sourceId est null ou 0 (non pertinent)
            Integer sourceId = transaction.getSourceId();
            if (sourceId != null && sourceId > 0) { 
                pstmt.setInt(8, sourceId);
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    transaction.setId_transaction(rs.getInt(1)); // Met à jour l'ID généré
                }
                return true;
            }
            return false;
        } finally {
            DatabaseManager.close(null, pstmt, rs); // Ne ferme pas la connexion 'conn' ici
        }
    }

    @Override
    public TransactionComptable getTransactionById(int id) throws SQLException {
        String sql = "SELECT id_transaction, date_transaction, reference_piece, description_transaction, montant, id_compte_debit, id_compte_credit, source_type, source_id FROM transactions_comptables WHERE id_transaction = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractTransactionFromResultSet(rs);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public List<TransactionComptable> getAllTransactions() throws SQLException {
        List<TransactionComptable> transactions = new ArrayList<>();
        String sql = "SELECT id_transaction, date_transaction, reference_piece, description_transaction, montant, id_compte_debit, id_compte_credit, source_type, source_id FROM transactions_comptables ORDER BY date_transaction DESC, id_transaction DESC";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } finally {
            DatabaseManager.close(conn, stmt, rs);
        }
        return transactions;
    }

    @Override
    public List<TransactionComptable> getTransactionsByCompte(int idCompte) throws SQLException {
        List<TransactionComptable> transactions = new ArrayList<>();
        String sql = "SELECT id_transaction, date_transaction, reference_piece, description_transaction, montant, id_compte_debit, id_compte_credit, source_type, source_id FROM transactions_comptables WHERE id_compte_debit = ? OR id_compte_credit = ? ORDER BY date_transaction DESC, id_transaction DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idCompte);
            pstmt.setInt(2, idCompte);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return transactions;
    }

    @Override
    public List<TransactionComptable> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<TransactionComptable> transactions = new ArrayList<>();
        String sql = "SELECT id_transaction, date_transaction, reference_piece, description_transaction, montant, id_compte_debit, id_compte_credit, source_type, source_id FROM transactions_comptables WHERE date_transaction BETWEEN ? AND ? ORDER BY date_transaction ASC, id_transaction ASC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.valueOf(startDate));
            pstmt.setTimestamp(2, Timestamp.valueOf(endDate));
            rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return transactions;
    }

    @Override
    public List<TransactionComptable> getTransactionsBySourceType(String sourceType) throws SQLException {
        List<TransactionComptable> transactions = new ArrayList<>();
        String sql = "SELECT id_transaction, date_transaction, reference_piece, description_transaction, montant, id_compte_debit, id_compte_credit, source_type, source_id FROM transactions_comptables WHERE source_type = ? ORDER BY date_transaction DESC, id_transaction DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, sourceType);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return transactions;
    }

    @Override
    public boolean deleteTransaction(int id) throws SQLException {
        String sql = "DELETE FROM transactions_comptables WHERE id_transaction = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    /**
     * Méthode utilitaire pour extraire une TransactionComptable d'un ResultSet.
     * Cette méthode récupère les objets CompteComptable associés via leur DAO.
     */
    private TransactionComptable extractTransactionFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_transaction");
        LocalDateTime date = rs.getTimestamp("date_transaction").toLocalDateTime();
        String referencePiece = rs.getString("reference_piece");
        String description = rs.getString("description_transaction");
        double montant = rs.getDouble("montant");
        
        int idCompteDebit = rs.getInt("id_compte_debit");
        int idCompteCredit = rs.getInt("id_compte_credit");
        String sourceType = rs.getString("source_type");
        // Utiliser rs.getObject pour gérer les valeurs NULL pour source_id
        // Si la colonne source_id est INTEGER dans la DB, getInt() retournera 0 pour NULL.
        // Utiliser getObject et cast pour distinguer NULL de 0.
        Integer sourceId = rs.getObject("source_id") != null ? rs.getInt("source_id") : null; 

        // Récupérer les objets CompteComptable via leur DAO
        CompteComptable compteDebit = compteComptableDAO.getCompteById(idCompteDebit);
        CompteComptable compteCredit = compteComptableDAO.getCompteById(idCompteCredit);

        // Il est crucial que compteDebit et compteCredit ne soient PAS null ici pour la construction de TransactionComptable
        // Si la base de données est inconsistante (références manquantes), cette erreur est normale.
        if (compteDebit == null) {
            System.err.println("Erreur: Compte de débit (ID: " + idCompteDebit + ") introuvable pour la transaction ID: " + id + ". Vérifiez l'intégrité de la base de données.");
            // Lancer une exception car une transaction sans compte est une donnée invalide.
            throw new SQLException("Compte de débit (ID: " + idCompteDebit + ") introuvable pour la transaction ID: " + id);
        }
        if (compteCredit == null) {
            System.err.println("Erreur: Compte de crédit (ID: " + idCompteCredit + ") introuvable pour la transaction ID: " + id + ". Vérifiez l'intégrité de la base de données.");
            // Lancer une exception car une transaction sans compte est une donnée invalide.
            throw new SQLException("Compte de crédit (ID: " + idCompteCredit + ") introuvable pour la transaction ID: " + id);
        }

        return new TransactionComptable(id, date, referencePiece, description, montant,
                                        compteDebit, compteCredit, sourceType, sourceId);
    }
}
