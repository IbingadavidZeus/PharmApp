package dao.impl;

import dao.DatabaseManager;
import dao.FactureDAO;
import dao.UtilisateurDAO; // Pour récupérer l'objet Utilisateur
import dao.LigneFactureDAO; // Pour charger les lignes de facture
import model.Facture;
import model.Utilisateur;
import model.LigneFacture; // Assurez-vous d'importer LigneFacture

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FactureDAOImpl implements FactureDAO {

    private UtilisateurDAO utilisateurDAO;
    private LigneFactureDAO ligneFactureDAO;

    // Le DAO de l'utilisateur et des lignes de facture est nécessaire pour construire l'objet Facture complet
    public FactureDAOImpl(UtilisateurDAO utilisateurDAO, LigneFactureDAO ligneFactureDAO) {
        this.utilisateurDAO = utilisateurDAO;
        this.ligneFactureDAO = ligneFactureDAO;
    }

    @Override
    public boolean ajouterFacture(Facture facture) throws SQLException {
        String sqlFacture = "INSERT INTO Factures (date_facture, montant_total, id_utilisateur) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmtFacture = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Début de la transaction

            // 1. Insérer la facture principale
            pstmtFacture = conn.prepareStatement(sqlFacture, Statement.RETURN_GENERATED_KEYS);
            pstmtFacture.setTimestamp(1, Timestamp.valueOf(facture.getDateFacture()));
            pstmtFacture.setDouble(2, facture.getMontantTotal()); // Le montant final sera calculé avant l'appel à cette méthode
            pstmtFacture.setInt(3, facture.getUtilisateur().getId());

            int rowsAffectedFacture = pstmtFacture.executeUpdate();

            if (rowsAffectedFacture > 0) {
                rs = pstmtFacture.getGeneratedKeys();
                if (rs.next()) {
                    facture.setId(rs.getInt(1)); // Récupère et définit l'ID généré pour la facture

                    // 2. Insérer les lignes de facture
                    for (LigneFacture ligne : facture.getLignesFacture()) {
                        ligne.setIdFacture(facture.getId()); // Associe la ligne à l'ID de la facture générée
                        ligneFactureDAO.ajouterLigneFacture(ligne); // Utilise le DAO de LigneFacture
                    }

                    // 3. Mettre à jour la quantité des produits dans le stock
                    // C'est critique pour une transaction atomique
                    String sqlUpdateProductQuantite = "UPDATE Produits SET quantite = quantite - ? WHERE id_produit = ?";
                    PreparedStatement pstmtUpdateProduct = conn.prepareStatement(sqlUpdateProductQuantite);
                    for (LigneFacture ligne : facture.getLignesFacture()) {
                        pstmtUpdateProduct.setInt(1, ligne.getQuantite());
                        pstmtUpdateProduct.setInt(2, ligne.getProduit().getId());
                        pstmtUpdateProduct.addBatch(); // Ajoute la commande au lot
                    }
                    pstmtUpdateProduct.executeBatch(); // Exécute toutes les mises à jour en lot

                    conn.commit(); // Valide la transaction
                    return true;
                }
            }
            conn.rollback(); // Annule la transaction si rien n'est inséré
            return false;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Annule la transaction en cas d'erreur
            }
            throw e; // Propage l'exception
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // Rétablit l'auto-commit
            }
            DatabaseManager.close(null, pstmtFacture, rs); // Connexion fermée par DatabaseManager
        }
    }


    @Override
    public Facture getFactureById(int id) throws SQLException {
        String sql = "SELECT id_facture, date_facture, montant_total, id_utilisateur FROM Factures WHERE id_facture = ?";
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
                LocalDateTime dateFacture = rs.getTimestamp("date_facture").toLocalDateTime();
                double montantTotal = rs.getDouble("montant_total");
                int idUtilisateur = rs.getInt("id_utilisateur");

                // Charger l'utilisateur
                Utilisateur utilisateur = utilisateurDAO.getUtilisateurById(idUtilisateur); // Utilise le DAO utilisateur

                facture = new Facture(id, dateFacture, montantTotal, utilisateur);

                // Charger les lignes de facture associées
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
        String sql = "SELECT id_facture, date_facture, montant_total, id_utilisateur FROM Factures ORDER BY date_facture DESC";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int idFacture = rs.getInt("id_facture");
                LocalDateTime dateFacture = rs.getTimestamp("date_facture").toLocalDateTime();
                double montantTotal = rs.getDouble("montant_total");
                int idUtilisateur = rs.getInt("id_utilisateur");

                Utilisateur utilisateur = utilisateurDAO.getUtilisateurById(idUtilisateur);

                Facture facture = new Facture(idFacture, dateFacture, montantTotal, utilisateur);
                // Charger les lignes de facture si nécessaire pour chaque facture (peut être coûteux pour beaucoup de factures)
                // Pour une liste complète, on peut choisir de ne charger les lignes qu'avec getFactureById()
                // Ici, on ne les charge pas pour éviter les requêtes N+1
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
        String sql = "SELECT id_facture, date_facture, montant_total, id_utilisateur FROM Factures WHERE id_utilisateur = ? ORDER BY date_facture DESC";
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
                LocalDateTime dateFacture = rs.getTimestamp("date_facture").toLocalDateTime();
                double montantTotal = rs.getDouble("montant_total");
                
                // L'utilisateur est déjà connu, pas besoin de le recharger
                Facture facture = new Facture(idFacture, dateFacture, montantTotal, utilisateur);
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
        String sql = "SELECT id_facture, date_facture, montant_total, id_utilisateur FROM Factures WHERE date_facture BETWEEN ? AND ? ORDER BY date_facture DESC";
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
                LocalDateTime dateFacture = rs.getTimestamp("date_facture").toLocalDateTime();
                double montantTotal = rs.getDouble("montant_total");
                int idUtilisateur = rs.getInt("id_utilisateur");

                Utilisateur utilisateur = utilisateurDAO.getUtilisateurById(idUtilisateur);
                
                Facture facture = new Facture(idFacture, dateFacture, montantTotal, utilisateur);
                factures.add(facture);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return factures;
    }

    @Override
    public boolean mettreAJourFacture(Facture facture) throws SQLException {
        // Cette méthode met à jour seulement la facture principale (montant, date, utilisateur)
        // Les lignes de facture sont gérées séparément par LigneFactureDAO
        String sql = "UPDATE Factures SET date_facture = ?, montant_total = ?, id_utilisateur = ? WHERE id_facture = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.valueOf(facture.getDateFacture()));
            pstmt.setDouble(2, facture.getMontantTotal());
            pstmt.setInt(3, facture.getUtilisateur().getId());
            pstmt.setInt(4, facture.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public boolean supprimerFacture(int id) throws SQLException {
        // Lors de la suppression d'une facture, ses lignes de facture doivent être supprimées en premier
        // grâce à la contrainte ON DELETE CASCADE ou manuellement ici.
        // On Delete Cascade est fortement recommandé dans la BDD pour Lignes_Facture.
        // Si vous n'avez pas ON DELETE CASCADE, vous devrez appeler ligneFactureDAO.supprimerLignesFactureByFactureId(id); ici.
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