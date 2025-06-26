package dao.impl;

import dao.DatabaseManager;
import dao.LigneFactureDAO;
import dao.ProduitDAO;
import model.LigneFacture;
import model.Produit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LigneFactureDAOImpl implements LigneFactureDAO {

    private ProduitDAO produitDAO;

    public LigneFactureDAOImpl(ProduitDAO produitDAO) {
        this.produitDAO = produitDAO;
    }

    @Override
    public boolean ajouterLigneFacture(LigneFacture ligneFacture) throws SQLException {
        String sql = "INSERT INTO lignesfacture (id_facture, id_produit, quantite_vendue, prix_unitaire_ht, sous_total) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setInt(1, ligneFacture.getIdFacture());
            pstmt.setInt(2, ligneFacture.getProduit().getId());
            pstmt.setInt(3, ligneFacture.getQuantite());
            pstmt.setDouble(4, ligneFacture.getPrixUnitaire());
            pstmt.setDouble(5, ligneFacture.getSousTotal());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    ligneFacture.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
    }

    @Override
    public List<LigneFacture> getLignesFactureByFactureId(int idFacture) throws SQLException {
        List<LigneFacture> lignes = new ArrayList<>();
        String sql = "SELECT id_ligne_facture, id_produit, quantite_vendue, prix_unitaire_ht, sous_total FROM lignesfacture WHERE id_facture = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idFacture);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int idLigne = rs.getInt("id_ligne_facture");
                int idProduit = rs.getInt("id_produit");
                int quantite = rs.getInt("quantite_vendue");
                double prixUnitaire = rs.getDouble("prix_unitaire_ht");
                double sousTotal = rs.getDouble("sous_total");

                // Charger le produit associé
                Produit produit = produitDAO.findProduitById(idProduit);

                LigneFacture ligne = new LigneFacture(idLigne, idFacture, produit, quantite, prixUnitaire, sousTotal);
                lignes.add(ligne);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return lignes;
    }

    @Override
    public LigneFacture getLigneFactureById(int id) throws SQLException {
        String sql = "SELECT id_ligne_facture, id_facture, id_produit, quantite_vendue, prix_unitaire_ht, sous_total FROM lignesfacture WHERE id_ligne_facture = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        LigneFacture ligneFacture = null;

        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int idLigne = rs.getInt("id_ligne_facture");
                int idFacture = rs.getInt("id_facture");
                int idProduit = rs.getInt("id_produit");
                int quantite = rs.getInt("quantite_vendue");
                double prixUnitaire = rs.getDouble("prix_unitaire_ht");
                double sousTotal = rs.getDouble("sous_total");

                Produit produit = produitDAO.findProduitById(idProduit);

                ligneFacture = new LigneFacture(idLigne, idFacture, produit, quantite, prixUnitaire, sousTotal);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return ligneFacture;
    }

    @Override
    public boolean mettreAJourLigneFacture(LigneFacture ligneFacture) throws SQLException {
        String sql = "UPDATE lignesfacture SET id_facture = ?, id_produit = ?, quantite_vendue = ?, prix_unitaire_ht = ?, sous_total = ? WHERE id_ligne_facture = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, ligneFacture.getIdFacture());
            pstmt.setInt(2, ligneFacture.getProduit().getId());
            pstmt.setInt(3, ligneFacture.getQuantite());
            pstmt.setDouble(4, ligneFacture.getPrixUnitaire());
            pstmt.setDouble(5, ligneFacture.getSousTotal());
            pstmt.setInt(6, ligneFacture.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public boolean supprimerLigneFacture(int id) throws SQLException {
        String sql = "DELETE FROM lignesfacture WHERE id_ligne_facture = ?";
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

    @Override
    public boolean supprimerLignesFactureByFactureId(int idFacture) throws SQLException {
        String sql = "DELETE FROM lignesfacture WHERE id_facture = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idFacture);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public boolean ajouterLigneFacture(Connection conn, LigneFacture ligneFacture) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'ajouterLigneFacture'");
    }
}