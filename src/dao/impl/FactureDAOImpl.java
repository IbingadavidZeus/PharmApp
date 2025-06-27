package dao.impl;

import dao.FactureDAO;
import dao.LigneFactureDAO;
import dao.ProduitDAO;
import dao.UtilisateurDAO;
import dao.AssuranceSocialDAO; 
import dao.DatabaseManager;
import model.Facture;
import model.LigneFacture;
import model.Produit;
import model.AssuranceSocial; 
import model.Utilisateur;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FactureDAOImpl implements FactureDAO {
    protected UtilisateurDAO utilisateurDAO;
    protected LigneFactureDAO ligneFactureDAO;
    protected ProduitDAO produitDAO;
    protected AssuranceSocialDAO assuranceSocialDAO;

    public FactureDAOImpl(UtilisateurDAO utilisateurDAO, LigneFactureDAO ligneFactureDAO, ProduitDAO produitDAO, AssuranceSocialDAO assuranceSocialDAO) {
        this.utilisateurDAO = utilisateurDAO;
        this.ligneFactureDAO = ligneFactureDAO;
        this.produitDAO = produitDAO;
        this.assuranceSocialDAO = assuranceSocialDAO;
    }

    @Override
    public boolean ajouterFacture(Facture facture) throws SQLException {
        Connection conn = null;
        PreparedStatement stmtFacture = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Début de la transaction

            // 1. Insérer la facture
            // IMPORTANT: Harmonisation des noms de colonnes SQL avec le schéma que vous avez fourni
            String sqlFacture = "INSERT INTO factures (numero_facture, date_facture, id_utilisateur, total_ht, total_ttc, id_assurance, montant_prise_en_charge, montant_restant_a_payer_client) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            stmtFacture = conn.prepareStatement(sqlFacture, Statement.RETURN_GENERATED_KEYS);
            stmtFacture.setString(1, facture.getNumeroFacture());
            stmtFacture.setTimestamp(2, Timestamp.valueOf(facture.getDateFacture()));
            stmtFacture.setInt(3, facture.getUtilisateur().getId());
            stmtFacture.setDouble(4, facture.getTotalHt());
            stmtFacture.setDouble(5, facture.getMontantTotal());
            
            // Gestion de l'ID de l'assurance (peut être NULL si pas d'assurance)
            if (facture.getAssuranceSocial() != null) { // Correction: getAssuranceSociale()
                stmtFacture.setInt(6, facture.getAssuranceSocial().getId_assurance()); // Correction: getId()
            } else {
                stmtFacture.setNull(6, Types.INTEGER);
            }
            stmtFacture.setDouble(7, facture.getMontantPrisEnChargeAssurance()); // Mappe à montant_prise_en_charge
            stmtFacture.setDouble(8, facture.getMontantRestantAPayerClient());

            int rowsAffected = stmtFacture.executeUpdate();
            if (rowsAffected == 0) {
                conn.rollback();
                return false;
            }

            try (ResultSet generatedKeys = stmtFacture.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    facture.setId(generatedKeys.getInt(1));
                } else {
                    conn.rollback();
                    throw new SQLException("Échec de la récupération de l'ID de la facture, aucune clé générée.");
                }
            }

            // 2. Insérer les lignes de facture et décrémenter le stock
            for (LigneFacture ligne : facture.getLignesFacture()) {
                
                ligne.setIdFacture(facture.getId());
                
                if (!ligneFactureDAO.ajouterLigneFacture(conn, ligne)) { 
                    conn.rollback();
                    return false;
                }

                Produit produitActuelEnBase = produitDAO.trouverParReference(ligne.getProduit().getReference());
                if (produitActuelEnBase == null) {
                    conn.rollback();
                    throw new SQLException("Produit non trouvé lors de la décrémentation du stock pour la référence: " + ligne.getProduit().getReference());
                }

                int quantiteVendue = ligne.getQuantite();
                int nouveauStock = produitActuelEnBase.getQuantite() - quantiteVendue;

                if (nouveauStock < 0) {
                    conn.rollback();
                    throw new SQLException("Stock insuffisant pour le produit " + produitActuelEnBase.getNom() + ". Stock disponible: " + produitActuelEnBase.getQuantite() + ", Quantité vendue: " + quantiteVendue);
                }
                // boolean stockUpdated = produitDAO.mettreAJourQuantite(conn, produitActuelEnBase.getReference(), nouveauStock);
                // if (!stockUpdated) {
                //     conn.rollback(); 
                //     return false;
                // }
            }

            conn.commit(); 
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); 
                } catch (SQLException ex) {
                    System.err.println("Erreur lors du rollback: " + ex.getMessage());
                }
            }
            throw e; 
        } finally {
            if (stmtFacture != null) {
                stmtFacture.close();
            }
            if (conn != null) {
                conn.setAutoCommit(true); 
                conn.close(); 
            }
        }
    }

    @Override
    public List<Facture> getAllFactures() throws SQLException {
        List<Facture> factures = new ArrayList<>();
        String sql = "SELECT f.id_facture, f.numero_facture, f.date_facture, f.total_ht, f.total_ttc, " +
                     "f.montant_prise_en_charge, f.montant_restant_a_payer_client, " +
                     "u.id_utilisateur, u.nom_utilisateur, u.mot_de_passe_hash, u.role, " +
                     "a.id_assurance, a.nom_assurance, a.taux_de_prise_en_charge " +
                     "FROM factures f " +
                     "JOIN utilisateurs u ON f.id_utilisateur = u.id_utilisateur " +
                     "LEFT JOIN assurances_social a ON f.id_assurance = a.id_assurance " + 
                     "ORDER BY f.date_facture DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Utilisateur utilisateur = new Utilisateur(
                    rs.getInt("id_utilisateur"),
                    rs.getString("nom_utilisateur"),
                    rs.getString("mot_de_passe_hash"),
                    rs.getString("role")
                );

                AssuranceSocial assurance = null; 
                if (rs.getObject("id_assurance") != null) {
                    assurance = new AssuranceSocial( 
                        rs.getInt("id_assurance"),
                        rs.getString("nom_assurance"),
                        rs.getDouble("taux_de_prise_en_charge")
                    );
                }

                Facture facture = new Facture(
                    rs.getInt("id_facture"),
                    rs.getString("numero_facture"),
                    rs.getTimestamp("date_facture").toLocalDateTime(),
                    utilisateur,
                    rs.getDouble("total_ht"),
                    rs.getDouble("total_ttc"),
                    assurance, 
                    rs.getDouble("montant_prise_en_charge"), 
                    rs.getDouble("montant_restant_a_payer_client")
                );
                facture.setLignesFacture(ligneFactureDAO.getLignesFactureByFactureId(facture.getId()));
                factures.add(facture);
            }
        }
        return factures;
    }

    @Override
    public List<Facture> getFacturesByUtilisateur(Utilisateur utilisateur) throws SQLException {
        List<Facture> factures = new ArrayList<>();
        
        String sql = "SELECT f.id_facture, f.numero_facture, f.date_facture, f.total_ht, f.total_ttc, " +
                     "f.montant_prise_en_charge, f.montant_restant_a_payer_client, " +
                     "u.id_utilisateur, u.nom_utilisateur, u.mot_de_passe_hash, u.role, " +
                     "a.id_assurance, a.nom_assurance, a.taux_de_prise_en_charge " +
                     "FROM factures f " +
                     "JOIN utilisateurs u ON f.id_utilisateur = u.id_utilisateur " +
                     "LEFT JOIN assurances_social a ON f.id_assurance = a.id_assurance " + 
                     "WHERE f.id_utilisateur = ? ORDER BY f.date_facture DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, utilisateur.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Utilisateur factureUser = new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("nom_utilisateur"),
                        rs.getString("mot_de_passe_hash"),
                        rs.getString("role")
                    );
                    AssuranceSocial assurance = null; 
                    if (rs.getObject("id_assurance") != null) {
                        assurance = new AssuranceSocial( 
                            rs.getInt("id_assurance"),
                            rs.getString("nom_assurance"),
                            rs.getDouble("taux_de_prise_en_charge")
                        );
                    }
                    Facture facture = new Facture(
                        rs.getInt("id_facture"),
                        rs.getString("numero_facture"),
                        rs.getTimestamp("date_facture").toLocalDateTime(),
                        factureUser,
                        rs.getDouble("total_ht"),
                        rs.getDouble("total_ttc"),
                        assurance,
                        rs.getDouble("montant_prise_en_charge"), 
                        rs.getDouble("montant_restant_a_payer_client")
                    );
                    facture.setLignesFacture(ligneFactureDAO.getLignesFactureByFactureId(facture.getId()));
                    factures.add(facture);
                }
            }
        }
        return factures;
    }

    @Override
    public List<Facture> getFacturesByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<Facture> factures = new ArrayList<>();
        String sql = "SELECT f.id_facture, f.numero_facture, f.date_facture, f.total_ht, f.total_ttc, " +
                     "f.montant_prise_en_charge, f.montant_restant_a_payer_client, " +
                     "u.id_utilisateur, u.nom_utilisateur, u.mot_de_passe_hash, u.role, " +
                     "a.id_assurance, a.nom_assurance, a.taux_de_prise_en_charge " +
                     "FROM factures f " +
                     "JOIN utilisateurs u ON f.id_utilisateur = u.id_utilisateur " +
                     "LEFT JOIN assurances_social a ON f.id_assurance = a.id_assurance " + 
                     "WHERE f.date_facture BETWEEN ? AND ? ORDER BY f.date_facture DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Utilisateur utilisateur = new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("nom_utilisateur"),
                        rs.getString("mot_de_passe_hash"),
                        rs.getString("role")
                    );
                    AssuranceSocial assurance = null; 
                    if (rs.getObject("id_assurance") != null) {
                        assurance = new AssuranceSocial( 
                            rs.getInt("id_assurance"),
                            rs.getString("nom_assurance"),
                            rs.getDouble("taux_de_prise_en_charge")
                        );
                    }
                    Facture facture = new Facture(
                        rs.getInt("id_facture"),
                        rs.getString("numero_facture"),
                        rs.getTimestamp("date_facture").toLocalDateTime(),
                        utilisateur,
                        rs.getDouble("total_ht"),
                        rs.getDouble("total_ttc"),
                        assurance,
                        rs.getDouble("montant_prise_en_charge"), 
                        rs.getDouble("montant_restant_a_payer_client")
                    );
                    facture.setLignesFacture(ligneFactureDAO.getLignesFactureByFactureId(facture.getId()));
                    factures.add(facture);
                }
            }
        }
        return factures;
    }

    @Override
    public Facture getFactureById(int id) throws SQLException {
        
        String sql = "SELECT f.id_facture, f.numero_facture, f.date_facture, f.total_ht, f.total_ttc, " +
                     "f.montant_prise_en_charge, f.montant_restant_a_payer_client, " +
                     "u.id_utilisateur, u.nom_utilisateur, u.mot_de_passe_hash, u.role, " +
                     "a.id_assurance, a.nom_assurance, a.taux_de_prise_en_charge " +
                     "FROM factures f " +
                     "JOIN utilisateurs u ON f.id_utilisateur = u.id_utilisateur " +
                     "LEFT JOIN assurances_social a ON f.id_assurance = a.id_assurance " + 
                     "WHERE f.id_facture = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Utilisateur utilisateur = new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("nom_utilisateur"),
                        rs.getString("mot_de_passe_hash"),
                        rs.getString("role")
                    );
                    AssuranceSocial assurance = null; 
                    if (rs.getObject("id_assurance") != null) {
                        assurance = new AssuranceSocial( 
                            rs.getInt("id_assurance"),
                            rs.getString("nom_assurance"),
                            rs.getDouble("taux_de_prise_en_charge")
                        );
                    }
                    Facture facture = new Facture(
                        rs.getInt("id_facture"),
                        rs.getString("numero_facture"),
                        rs.getTimestamp("date_facture").toLocalDateTime(),
                        utilisateur,
                        rs.getDouble("total_ht"),
                        rs.getDouble("total_ttc"),
                        assurance,
                        rs.getDouble("montant_prise_en_charge"), 
                        rs.getDouble("montant_restant_a_payer_client")
                    );
                    facture.setLignesFacture(ligneFactureDAO.getLignesFactureByFactureId(facture.getId()));
                    return facture;
                }
            }
        }
        return null;
    }

    @Override
    public boolean mettreAJourFacture(Facture facture) throws SQLException {
       
        throw new UnsupportedOperationException("Unimplemented method 'mettreAJourFacture'");
    }

    @Override
    public boolean supprimerFacture(int id) throws SQLException {
        
        throw new UnsupportedOperationException("Unimplemented method 'supprimerFacture'");
    }
}
