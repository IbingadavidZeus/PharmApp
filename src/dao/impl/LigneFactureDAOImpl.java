package dao.impl;

import dao.LigneFactureDAO;
import dao.ProduitDAO;
import dao.DatabaseManager; 
import model.LigneFacture;
import model.Produit;
import model.Medicament; 
import model.ProduitParaPharmacie; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; 
import java.util.ArrayList;
import java.util.List;

public class LigneFactureDAOImpl implements LigneFactureDAO {
    private ProduitDAO produitDAO;

    public LigneFactureDAOImpl(ProduitDAO produitDAO) {
        this.produitDAO = produitDAO;
    }

    // Version pour transaction: utilise la connexion fournie
    @Override
    public boolean ajouterLigneFacture(Connection conn, LigneFacture ligneFacture) throws SQLException {
        String sql = "INSERT INTO lignesfacture (id_facture, reference_produit, quantite_vendue, prix_unitaire_ht, prix_unitaire_ttc, sous_total) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) { 
            stmt.setInt(1, ligneFacture.getIdFacture());
            stmt.setString(2, ligneFacture.getProduit().getReference());
            stmt.setInt(3, ligneFacture.getQuantite());
            stmt.setDouble(4, ligneFacture.getPrixUnitaire());
            stmt.setDouble(5, ligneFacture.getPrixUnitaire()); // Prix unitaire TTC   
            stmt.setDouble(6, ligneFacture.getSousTotal());
            return stmt.executeUpdate() > 0;
        }
        // Pas de close() ou setAutoCommit(true/false) ici, la gestion de la transaction est externe
    }

    // Version autonome: ouvre et ferme sa propre connexion
    @Override
    public boolean ajouterLigneFacture(LigneFacture ligneFacture) throws SQLException {
        String sql = "INSERT INTO lignesfacture (id_facture, reference_produit, quantite_vendue, prix_unitaire_ht, prix_unitaire_ttc, sous_total) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setInt(1, ligneFacture.getIdFacture());
            pstmt.setString(2, ligneFacture.getProduit().getReference());
            pstmt.setInt(3, ligneFacture.getQuantite());
            pstmt.setDouble(4, ligneFacture.getPrixUnitaire());
            pstmt.setDouble(5, ligneFacture.getPrixUnitaire());
            pstmt.setDouble(6, ligneFacture.getSousTotal());

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
            DatabaseManager.close(conn, pstmt, rs); // Ferme les ressources
        }
    }

    @Override
    public List<LigneFacture> getLignesFactureByFactureId(int factureId) throws SQLException {
        List<LigneFacture> lignesFacture = new ArrayList<>();
        String sql = "SELECT lf.id_ligne_facture, lf.id_facture, lf.reference_produit, lf.quantite_vendue, lf.prix_unitaire_ht, lf.prix_unitaire_ttc, lf.sous_total, " +
                     "p.id_produit, p.nom, p.description, p.prix_ht as produit_prix_ht, p.quantite as produit_quantite, p.type_produit, p.est_generique, p.est_sur_ordonnance, p.categorie_parapharmacie " +
                     "FROM lignesfacture lf JOIN produits p ON lf.reference_produit = p.reference WHERE lf.id_facture = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection(); 
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, factureId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Produit produit = null;
                int idProduit = rs.getInt("id_produit");
                String produitNom = rs.getString("nom");
                String produitReference = rs.getString("reference_produit");
                String produitDescription = rs.getString("description");
                double produitPrixHt = rs.getDouble("produit_prix_ht");
                int produitQuantite = rs.getInt("produit_quantite");
                String produitType = rs.getString("type_produit");

                if ("Medicament".equalsIgnoreCase(produitType)) { 
                    boolean estGenerique = rs.getBoolean("est_generique");
                    boolean estSurOrdonnance = rs.getBoolean("est_sur_ordonnance");
                    produit = new Medicament(idProduit, produitNom, produitReference, produitDescription, produitPrixHt, produitQuantite, estGenerique, estSurOrdonnance);
                } else if ("Parapharmacie".equalsIgnoreCase(produitType)) { 
                    String categoriePara = rs.getString("categorie_parapharmacie");
                    produit = new ProduitParaPharmacie(idProduit, produitNom, produitReference, produitDescription, produitPrixHt, produitQuantite, categoriePara);
                } else {
                    produit = new Produit(idProduit, produitNom, produitReference, produitDescription, produitPrixHt, produitQuantite, produitType);
                }

                LigneFacture ligneFacture = new LigneFacture(
                    rs.getInt("id_ligne_facture"),
                    rs.getInt("id_facture"),
                    produit, 
                    rs.getInt("quantite_vendue"),
                    rs.getDouble("prix_unitaire_ht"),
                    rs.getDouble("prix_unitaire_ttc"), 
                    rs.getDouble("sous_total")
                );
                lignesFacture.add(ligneFacture);
            }
        } finally {
            DatabaseManager.close(conn, stmt, rs); 
        }
        return lignesFacture;
    }

    @Override
    public LigneFacture getLigneFactureById(int id) throws SQLException {
        String sql = "SELECT lf.id_ligne_facture, lf.id_facture, lf.reference_produit, lf.quantite_vendue, lf.prix_unitaire_ht, lf.prix_unitaire_ttc, lf.sous_total, " +
                     "p.id_produit, p.nom, p.description, p.prix_ht as produit_prix_ht, p.quantite as produit_quantite, p.type_produit, p.est_generique, p.est_sur_ordonnance, p.categorie_parapharmacie " +
                     "FROM lignesfacture lf JOIN produits p ON lf.reference_produit = p.reference WHERE lf.id_ligne_facture = ?";
        
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
                Produit produit = null;
                int idProduit = rs.getInt("id_produit");
                String produitNom = rs.getString("nom");
                String produitReference = rs.getString("reference_produit");
                String produitDescription = rs.getString("description");
                double produitPrixHt = rs.getDouble("produit_prix_ht");
                int produitQuantite = rs.getInt("produit_quantite");
                String produitType = rs.getString("type_produit");

                if ("Medicament".equalsIgnoreCase(produitType)) {
                    boolean estGenerique = rs.getBoolean("est_generique");
                    boolean estSurOrdonnance = rs.getBoolean("est_sur_ordonnance");
                    produit = new Medicament(idProduit, produitNom, produitReference, produitDescription, produitPrixHt, produitQuantite, estGenerique, estSurOrdonnance);
                } else if ("Parapharmacie".equalsIgnoreCase(produitType)) {
                    String categoriePara = rs.getString("categorie_parapharmacie");
                    produit = new ProduitParaPharmacie(idProduit, produitNom, produitReference, produitDescription, produitPrixHt, produitQuantite, categoriePara);
                } else {
                    produit = new Produit(idProduit, produitNom, produitReference, produitDescription, produitPrixHt, produitQuantite, produitType);
                }

                ligneFacture = new LigneFacture(
                    rs.getInt("id_ligne_facture"),
                    rs.getInt("id_facture"),
                    produit,
                    rs.getInt("quantite_vendue"),
                    rs.getDouble("prix_unitaire_ht"),
                    rs.getDouble("prix_unitaire_ttc"), 
                    rs.getDouble("sous_total")
                );
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return ligneFacture;
    }

    @Override
    public boolean mettreAJourLigneFacture(LigneFacture ligneFacture) throws SQLException {
        String sql = "UPDATE lignesfacture SET id_facture = ?, reference_produit = ?, quantite_vendue = ?, prix_unitaire_ht = ?, prix_unitaire_ttc = ?, sous_total = ? WHERE id_ligne_facture = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, ligneFacture.getIdFacture());
            pstmt.setString(2, ligneFacture.getProduit().getReference()); 
            pstmt.setInt(3, ligneFacture.getQuantite());
            pstmt.setDouble(4, ligneFacture.getPrixUnitaire());
            // pstmt.setDouble(5, ligneFacture.getPrixUnitaire()); 
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
}
