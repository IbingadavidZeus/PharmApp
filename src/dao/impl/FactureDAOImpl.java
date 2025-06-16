package dao.impl;

import dao.DatabaseManager;
import dao.FactureDAO;
import dao.UtilisateurDAO;
import dao.LigneFactureDAO;
import model.Facture;
import model.Utilisateur;
import model.LigneFacture;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FactureDAOImpl implements FactureDAO {

    private UtilisateurDAO utilisateurDAO;
    private LigneFactureDAO ligneFactureDAO;

    public FactureDAOImpl(UtilisateurDAO utilisateurDAO, LigneFactureDAO ligneFactureDAO) {
        this.utilisateurDAO = utilisateurDAO;
        this.ligneFactureDAO = ligneFactureDAO;
    }

    @Override
    public boolean ajouterFacture(Facture facture) throws SQLException {
        // SQL initialement sans numero_facture car il est généré APRES l'ID
        String sqlInsertFacture = "INSERT INTO Factures (date_facture, total_ttc, id_utilisateur) VALUES (?, ?, ?)";
        String sqlUpdateNumeroFacture = "UPDATE Factures SET numero_facture = ? WHERE id_facture = ?"; 
        
        Connection conn = null;
        PreparedStatement pstmtInsertFacture = null;
        PreparedStatement pstmtUpdateNumeroFacture = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert the main invoice without the custom number
            pstmtInsertFacture = conn.prepareStatement(sqlInsertFacture, Statement.RETURN_GENERATED_KEYS);
            pstmtInsertFacture.setTimestamp(1, Timestamp.valueOf(facture.getDateFacture()));
            pstmtInsertFacture.setDouble(2, facture.getMontantTotal());
            pstmtInsertFacture.setInt(3, facture.getUtilisateur().getId());

            int rowsAffectedFacture = pstmtInsertFacture.executeUpdate();

            if (rowsAffectedFacture > 0) {
                rs = pstmtInsertFacture.getGeneratedKeys();
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    facture.setId(generatedId); // Set the generated ID on the Java object

                    // 2. Generate the custom invoice number using the generated ID
                    String numeroFacture = facture.getUtilisateur().getNomUtilisateur().toUpperCase() + "-" + String.format("%06d", generatedId);
                    facture.setNumeroFacture(numeroFacture); // Set the custom number on the Java object

                    // 3. Update the newly inserted invoice with the generated custom number
                    pstmtUpdateNumeroFacture = conn.prepareStatement(sqlUpdateNumeroFacture);
                    pstmtUpdateNumeroFacture.setString(1, numeroFacture);
                    pstmtUpdateNumeroFacture.setInt(2, generatedId);
                    pstmtUpdateNumeroFacture.executeUpdate();

                    // 4. Insert invoice lines
                    for (LigneFacture ligne : facture.getLignesFacture()) {
                        ligne.setIdFacture(facture.getId()); 
                        ligneFactureDAO.ajouterLigneFacture(ligne); 
                    }

                    // 5. Update product quantities in stock
                    String sqlUpdateProductQuantite = "UPDATE Produits SET quantite = quantite - ? WHERE id_produit = ?";
                    PreparedStatement pstmtUpdateProduct = conn.prepareStatement(sqlUpdateProductQuantite);
                    for (LigneFacture ligne : facture.getLignesFacture()) {
                        pstmtUpdateProduct.setInt(1, ligne.getQuantite());
                        pstmtUpdateProduct.setInt(2, ligne.getProduit().getId());
                        pstmtUpdateProduct.addBatch();
                    }
                    pstmtUpdateProduct.executeBatch();

                    conn.commit(); // Commit the transaction
                    return true;
                }
            }
            conn.rollback(); // Rollback if nothing inserted
            return false;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Rollback in case of error
            }
            throw e; // Propagate the exception
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // Reset auto-commit
            }
            DatabaseManager.close(null, pstmtInsertFacture, rs);
            DatabaseManager.close(null, pstmtUpdateNumeroFacture); 
        }
    }


    @Override
    public Facture getFactureById(int id) throws SQLException {
        // Select numero_facture
        String sql = "SELECT id_facture, numero_facture, date_facture, total_ttc, id_utilisateur FROM Factures WHERE id_facture = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Facture facture = null;

        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String numeroFacture = rs.getString("numero_facture"); 
                LocalDateTime dateFacture = rs.getTimestamp("date_facture").toLocalDateTime();
                double montantTotal = rs.getDouble("total_ttc");
                int idUtilisateur = rs.getInt("id_utilisateur");

                Utilisateur utilisateur = utilisateurDAO.getUtilisateurById(idUtilisateur);

                facture = new Facture(id, numeroFacture, dateFacture, montantTotal, utilisateur);

                List<LigneFacture> lignes = ligneFactureDAO.getLignesFactureByFactureId(id);
                facture.setLignesFacture(lignes);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return facture;
    }

    @Override
    public List<Facture> getAllFactures() throws SQLException {
        List<Facture> factures = new ArrayList<>();
        // Select numero_facture
        String sql = "SELECT id_facture, numero_facture, date_facture, total_ttc, id_utilisateur FROM Factures ORDER BY date_facture DESC";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int idFacture = rs.getInt("id_facture");
                String numeroFacture = rs.getString("numero_facture"); 
                LocalDateTime dateFacture = rs.getTimestamp("date_facture").toLocalDateTime();
                double montantTotal = rs.getDouble("total_ttc");
                int idUtilisateur = rs.getInt("id_utilisateur");

                Utilisateur utilisateur = utilisateurDAO.getUtilisateurById(idUtilisateur);

                Facture facture = new Facture(idFacture, numeroFacture, dateFacture, montantTotal, utilisateur);
                factures.add(facture);
            }
        } finally {
            DatabaseManager.close(conn, stmt, rs);
        }
        return factures;
    }
    
    @Override
    public List<Facture> getFacturesByUtilisateur(Utilisateur utilisateur) throws SQLException {
        List<Facture> factures = new ArrayList<>();
        // Select numero_facture
        String sql = "SELECT id_facture, numero_facture, date_facture, total_ttc, id_utilisateur FROM Factures WHERE id_utilisateur = ? ORDER BY date_facture DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, utilisateur.getId());
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int idFacture = rs.getInt("id_facture");
                String numeroFacture = rs.getString("numero_facture"); 
                LocalDateTime dateFacture = rs.getTimestamp("date_facture").toLocalDateTime();
                double montantTotal = rs.getDouble("total_ttc");
                
                Facture facture = new Facture(idFacture, numeroFacture, dateFacture, montantTotal, utilisateur);
                factures.add(facture);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return factures;
    }

    @Override
    public List<Facture> getFacturesByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<Facture> factures = new ArrayList<>();
        // Select numero_facture
        String sql = "SELECT id_facture, numero_facture, date_facture, total_ttc, id_utilisateur FROM Factures WHERE date_facture BETWEEN ? AND ? ORDER BY date_facture DESC";
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
                int idFacture = rs.getInt("id_facture");
                String numeroFacture = rs.getString("numero_facture"); 
                LocalDateTime dateFacture = rs.getTimestamp("date_facture").toLocalDateTime();
                double montantTotal = rs.getDouble("total_ttc");
                int idUtilisateur = rs.getInt("id_utilisateur");

                Utilisateur utilisateur = utilisateurDAO.getUtilisateurById(idUtilisateur);
                
                Facture facture = new Facture(idFacture, numeroFacture, dateFacture, montantTotal, utilisateur);
                factures.add(facture);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return factures;
    }

    @Override
    public boolean mettreAJourFacture(Facture facture) throws SQLException {
        // Include numero_facture in the update
        String sql = "UPDATE Factures SET numero_facture = ?, date_facture = ?, total_ttc = ?, id_utilisateur = ? WHERE id_facture = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, facture.getNumeroFacture());
            pstmt.setTimestamp(2, Timestamp.valueOf(facture.getDateFacture()));
            pstmt.setDouble(3, facture.getMontantTotal());
            pstmt.setInt(4, facture.getUtilisateur().getId());
            pstmt.setInt(5, facture.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public boolean supprimerFacture(int id) throws SQLException {
        String sql = "DELETE FROM Factures WHERE id_facture = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }
}
