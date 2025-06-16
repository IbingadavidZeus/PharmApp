package dao.impl;

import dao.DatabaseManager;
import dao.LigneFactureDAO;
import dao.ProduitDAO; // Pour charger l'objet Produit
import model.LigneFacture;
import model.Produit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LigneFactureDAOImpl implements LigneFactureDAO {

    private ProduitDAO produitDAO; // Nécessaire pour construire l'objet LigneFacture complet

    public LigneFactureDAOImpl(ProduitDAO produitDAO) {
        this.produitDAO = produitDAO;
    }

    @Override
    public boolean ajouterLigneFacture(LigneFacture ligneFacture) throws SQLException {
        String sql = "INSERT INTO Lignes_Facture (id_facture, id_produit, quantite, prix_unitaire, sous_total) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection();
            // Important: Ne pas utiliser Statement.RETURN_GENERATED_KEYS ici si la transaction est gérée par FactureDAO
            // La connexion est passée de FactureDAOImpl si elle fait partie de la même transaction.
            // Cependant, si appelée individuellement, elle aura besoin de son propre ID auto-généré.
            // Pour l'ajout par le FactureDAO, on part du principe que la connexion est déjà dans une transaction.
            // Pour un usage autonome (moins courant), il faudrait gérer l'ID.
            // Pour le moment, on ne gère pas l'ID généré ici, car il est principalement géré par FactureDAOImpl.
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); // Ajout pour le cas où elle est appelée seule

            pstmt.setInt(1, ligneFacture.getIdFacture());
            pstmt.setInt(2, ligneFacture.getProduit().getId());
            pstmt.setInt(3, ligneFacture.getQuantite());
            pstmt.setDouble(4, ligneFacture.getPrixUnitaire());
            pstmt.setDouble(5, ligneFacture.getSousTotal());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    ligneFacture.setId(rs.getInt(1)); // Définit l'ID de la ligne si généré
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
        String sql = "SELECT id_ligne, id_produit, quantite, prix_unitaire, sous_total FROM Lignes_Facture WHERE id_facture = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idFacture);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int idLigne = rs.getInt("id_ligne");
                int idProduit = rs.getInt("id_produit");
                int quantite = rs.getInt("quantite");
                double prixUnitaire = rs.getDouble("prix_unitaire");
                double sousTotal = rs.getDouble("sous_total");

                // Charger le produit associé
                Produit produit = produitDAO.findProduitById(idProduit); // Utilise le DAO produit

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
        String sql = "SELECT id_ligne, id_facture, id_produit, quantite, prix_unitaire, sous_total FROM Lignes_Facture WHERE id_ligne = ?";
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
                int idLigne = rs.getInt("id_ligne");
                int idFacture = rs.getInt("id_facture");
                int idProduit = rs.getInt("id_produit");
                int quantite = rs.getInt("quantite");
                double prixUnitaire = rs.getDouble("prix_unitaire");
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
        String sql = "UPDATE Lignes_Facture SET id_facture = ?, id_produit = ?, quantite = ?, prix_unitaire = ?, sous_total = ? WHERE id_ligne = ?";
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
        String sql = "DELETE FROM Lignes_Facture WHERE id_ligne = ?";
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
        String sql = "DELETE FROM Lignes_Facture WHERE id_facture = ?";
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
}