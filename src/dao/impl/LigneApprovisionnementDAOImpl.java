package dao.impl;

import dao.DatabaseManager;
import dao.LigneApprovisionnementDAO;
import dao.ProduitDAO; // Pour récupérer l'objet Produit
import model.LigneApprovisionnement;
import model.Produit;
import model.Medicament; // Imports spécifiques pour les sous-classes de Produit
import model.ProduitParaPharmacie; // Imports spécifiques pour les sous-classes de Produit

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LigneApprovisionnementDAOImpl implements LigneApprovisionnementDAO {

    private ProduitDAO produitDAO; // Nécessaire pour charger l'objet Produit

    public LigneApprovisionnementDAOImpl(ProduitDAO produitDAO) {
        this.produitDAO = produitDAO;
    }

    @Override
    public boolean addLigneApprovisionnement(LigneApprovisionnement ligneApprovisionnement) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            return addLigneApprovisionnement(conn, ligneApprovisionnement); // Délègue à la méthode transactionnelle
        } finally {
            DatabaseManager.close(conn, null); // Ferme la connexion ici
        }
    }

    @Override
    public boolean addLigneApprovisionnement(Connection conn, LigneApprovisionnement ligneApprovisionnement) throws SQLException {
        String sql = "INSERT INTO lignes_approvisionnement (id_approvisionnement, id_produit, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, ligneApprovisionnement.getIdApprovisionnement());
            pstmt.setInt(2, ligneApprovisionnement.getProduit().getId()); // Utilise l'ID du produit
            pstmt.setInt(3, ligneApprovisionnement.getQuantiteCommandee());
            pstmt.setDouble(4, ligneApprovisionnement.getPrixUnitaireAchatHt());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    ligneApprovisionnement.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } finally {
            DatabaseManager.close(null, pstmt, rs); // Ne ferme pas la connexion 'conn' ici
        }
    }

    @Override
    public LigneApprovisionnement getLigneApprovisionnementById(int id) throws SQLException {
        String sql = "SELECT id_ligne_approvisionnement, id_approvisionnement, id_produit, quantite, prix_unitaire FROM lignes_approvisionnement WHERE id_ligne_approvisionnement = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractLigneApprovisionnementFromResultSet(rs);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public List<LigneApprovisionnement> getLignesApprovisionnementByApproId(int approvisionnementId) throws SQLException {
        List<LigneApprovisionnement> lignes = new ArrayList<>();
        String sql = "SELECT id_ligne_approvisionnement, id_approvisionnement, id_produit, quantite, prix_unitaire FROM lignes_approvisionnement WHERE id_approvisionnement = ? ORDER BY id_ligne_approvisionnement";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, approvisionnementId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                lignes.add(extractLigneApprovisionnementFromResultSet(rs));
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return lignes;
    }

    @Override
    public boolean updateLigneApprovisionnement(LigneApprovisionnement ligneApprovisionnement) throws SQLException {
        String sql = "UPDATE lignes_approvisionnement SET id_approvisionnement = ?, id_produit = ?, quantite = ?, prix_unitaire = ? WHERE id_ligne_approvisionnement = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, ligneApprovisionnement.getIdApprovisionnement());
            pstmt.setInt(2, ligneApprovisionnement.getProduit().getId());
            pstmt.setInt(3, ligneApprovisionnement.getQuantiteCommandee());
            pstmt.setDouble(4, ligneApprovisionnement.getPrixUnitaireAchatHt());
            pstmt.setInt(5, ligneApprovisionnement.getId());
            return pstmt.executeUpdate() > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public boolean deleteLigneApprovisionnement(int id) throws SQLException {
        String sql = "DELETE FROM lignes_approvisionnement WHERE id_ligne_approvisionnement = ?";
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

    @Override
    public boolean deleteLignesApprovisionnementByApproId(int approvisionnementId) throws SQLException {
        String sql = "DELETE FROM lignes_approvisionnement WHERE id_approvisionnement = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, approvisionnementId);
            return pstmt.executeUpdate() > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    private LigneApprovisionnement extractLigneApprovisionnementFromResultSet(ResultSet rs) throws SQLException {
        // Le ProduitDAO est utilisé ici pour récupérer l'objet Produit complet
        Produit produit = produitDAO.findProduitById(rs.getInt("id_produit"));
        if (produit == null) {
            throw new SQLException("Produit avec ID " + rs.getInt("id_produit") + " non trouvé pour la ligne d'approvisionnement ID " + rs.getInt("id_ligne_approvisionnement"));
        }

        return new LigneApprovisionnement(
            rs.getInt("id_ligne_approvisionnement"),
            rs.getInt("id_approvisionnement"),
            produit,
            rs.getInt("quantite"),
            rs.getDouble("prix_unitaire")
        );
    }
}
