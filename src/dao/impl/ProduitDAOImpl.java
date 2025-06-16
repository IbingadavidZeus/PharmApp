package dao.impl;

import java.sql.Types;
import dao.DatabaseManager;
import dao.ProduitDAO;
import model.Medicament;
import model.Produit;
import model.ProduitParaPharmacie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProduitDAOImpl implements ProduitDAO {

    @Override
    public boolean ajouterProduit(Produit produit) throws SQLException {
        String sql = "INSERT INTO Produits (reference, nom, description, prix_ht, quantite, type_produit, est_generique, est_sur_ordonnance, categorie_parapharmacie) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, produit.getReference());
            pstmt.setString(2, produit.getNom());
            pstmt.setString(3, produit.getDescription());
            pstmt.setDouble(4, produit.getPrixHt());
            pstmt.setInt(5, produit.getQuantite());

            if (produit instanceof Medicament) {
                Medicament medicament = (Medicament) produit;
                pstmt.setString(6, "Medicament");
                pstmt.setBoolean(7, medicament.isGenerique());
                pstmt.setBoolean(8, medicament.isSurOrdonnance());
                pstmt.setNull(9, Types.VARCHAR);
            } else if (produit instanceof ProduitParaPharmacie) {
                ProduitParaPharmacie parapharmacie = (ProduitParaPharmacie) produit;
                pstmt.setString(6, "Parapharmacie");
                pstmt.setNull(7, Types.BOOLEAN);
                pstmt.setNull(8, Types.BOOLEAN);
                pstmt.setString(9, parapharmacie.getCategorie());
            } else {
                throw new IllegalArgumentException("Type de produit inconnu ou non supporté pour l'insertion: " + produit.getClass().getName());
            }

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        produit.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public Produit trouverParReference(String reference) throws SQLException {
        String sql = "SELECT id_produit, reference, nom, description, prix_ht, quantite, type_produit, est_generique, est_sur_ordonnance, categorie_parapharmacie FROM Produits WHERE reference = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Produit produit = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, reference);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                produit = createProduitFromResultSet(rs);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return produit;
    }

    @Override
    public Produit trouverParId(int id) throws SQLException {
        return findProduitById(id);
    }

    @Override
    public Produit findProduitById(int id) throws SQLException {
        String sql = "SELECT id_produit, reference, nom, description, prix_ht, quantite, type_produit, est_generique, est_sur_ordonnance, categorie_parapharmacie FROM Produits WHERE id_produit = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Produit produit = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                produit = createProduitFromResultSet(rs);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return produit;
    }

    @Override
    public List<Produit> getAllProduits() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT id_produit, reference, nom, description, prix_ht, quantite, type_produit, est_generique, est_sur_ordonnance, categorie_parapharmacie FROM Produits";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                produits.add(createProduitFromResultSet(rs));
            }
        } finally {
            DatabaseManager.close(conn, stmt, rs);
        }
        return produits;
    }

    @Override
    public boolean mettreAJourProduit(Produit produit) throws SQLException {
        String sql = "UPDATE Produits SET nom = ?, description = ?, prix_ht = ?, quantite = ?, type_produit = ?, est_generique = ?, est_sur_ordonnance = ?, categorie_parapharmacie = ? WHERE reference = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, produit.getNom());
            pstmt.setString(2, produit.getDescription());
            pstmt.setDouble(3, produit.getPrixHt());
            pstmt.setInt(4, produit.getQuantite());

            if (produit instanceof Medicament) {
                Medicament medicament = (Medicament) produit;
                pstmt.setString(5, "Medicament");
                pstmt.setBoolean(6, medicament.isGenerique());
                pstmt.setBoolean(7, medicament.isSurOrdonnance());
                pstmt.setNull(8, Types.VARCHAR);
            } else if (produit instanceof ProduitParaPharmacie) {
                ProduitParaPharmacie parapharmacie = (ProduitParaPharmacie) produit;
                pstmt.setString(5, "Parapharmacie");
                pstmt.setNull(6, Types.BOOLEAN);
                pstmt.setNull(7, Types.BOOLEAN);
                pstmt.setString(8, parapharmacie.getCategorie());
            } else {
                throw new IllegalArgumentException("Type de produit inconnu ou non supporté pour la mise à jour: " + produit.getClass().getName());
            }
            pstmt.setString(9, produit.getReference());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public boolean supprimerProduit(String reference) throws SQLException {
        String sql = "DELETE FROM Produits WHERE reference = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, reference);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public boolean mettreAJourQuantite(String reference, int nouvelleQuantite) throws SQLException {
        String sql = "UPDATE Produits SET quantite = ? WHERE reference = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, nouvelleQuantite);
            pstmt.setString(2, reference);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public List<Produit> rechercherProduits(String critere) throws SQLException {
        List<Produit> produits = new ArrayList<>();
        // Recherche par nom ou référence (case-insensitive)
        String sql = "SELECT id_produit, reference, nom, description, prix_ht, quantite, type_produit, est_generique, est_sur_ordonnance, categorie_parapharmacie FROM Produits WHERE nom LIKE ? OR reference LIKE ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            // Ajoute des jokers '%' pour la recherche partielle et passe le critère en minuscules (si la BDD n'est pas configurée pour une recherche insensible à la casse par défaut,
            // il faudrait utiliser LOWER(nom) LIKE LOWER(?) ou le faire côté Java si les données sont petites)
            // Pour MySQL, LIKE est souvent insensible à la casse par défaut, mais ce n'est pas garanti pour toutes les configurations/DBs.
            pstmt.setString(1, "%" + critere + "%");
            pstmt.setString(2, "%" + critere + "%");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                produits.add(createProduitFromResultSet(rs)); // Utilise la méthode utilitaire
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return produits;
    }

    private Produit createProduitFromResultSet(ResultSet rs) throws SQLException {
        int idProduit = rs.getInt("id_produit");
        String reference = rs.getString("reference");
        String nom = rs.getString("nom");
        String description = rs.getString("description");
        double prixHT = rs.getDouble("prix_ht");
        int quantite = rs.getInt("quantite");
        String typeProduit = rs.getString("type_produit");

        if ("Medicament".equals(typeProduit)) {
            boolean estGenerique = rs.getBoolean("est_generique");
            boolean estSurOrdonnance = rs.getBoolean("est_sur_ordonnance");
            return new Medicament(idProduit, nom, reference, description, prixHT, quantite, estGenerique, estSurOrdonnance);
        } else if ("Parapharmacie".equals(typeProduit)) {
            String categorie = rs.getString("categorie_parapharmacie");
            return new ProduitParaPharmacie(idProduit, nom, reference, description, prixHT, quantite, categorie);
        } else {
            throw new SQLException("Type de produit inconnu dans la base de données pour la référence " + reference + ": " + typeProduit);
        }
    }
}
