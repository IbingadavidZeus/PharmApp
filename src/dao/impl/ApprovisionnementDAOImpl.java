package dao.impl;

import dao.ApprovisionnementDAO;
import dao.FournisseurDAO;
import dao.LigneApprovisionnementDAO;
import dao.ProduitDAO;
import dao.CompteComptableDAO;
import dao.TransactionComptableDAO;
import dao.DatabaseManager;
import model.Approvisionnement;
import model.Fournisseur;
import model.LigneApprovisionnement;
import model.Produit;
import model.CompteComptable;
import model.TransactionComptable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApprovisionnementDAOImpl implements ApprovisionnementDAO {

    private FournisseurDAO fournisseurDAO;
    private LigneApprovisionnementDAO ligneApprovisionnementDAO;
    private ProduitDAO produitDAO;
    private CompteComptableDAO compteComptableDAO;
    private TransactionComptableDAO transactionComptableDAO;

    // Comptes comptables clés (initialisés une seule fois si possible)
    private CompteComptable compteAchatsMarchandises;
    private CompteComptable compteTVADeductible;
    private CompteComptable compteCaisse; 
    private CompteComptable compteFournisseurs; 
    private CompteComptable compteBanque; 

    public ApprovisionnementDAOImpl(FournisseurDAO fournisseurDAO, LigneApprovisionnementDAO ligneApprovisionnementDAO,
                                    ProduitDAO produitDAO, CompteComptableDAO compteComptableDAO,
                                    TransactionComptableDAO transactionComptableDAO) {
        this.fournisseurDAO = fournisseurDAO;
        this.ligneApprovisionnementDAO = ligneApprovisionnementDAO;
        this.produitDAO = produitDAO;
        this.compteComptableDAO = compteComptableDAO;
        this.transactionComptableDAO = transactionComptableDAO;

        try {
            initComptesComptables();
        } catch (SQLException e) {
            System.err.println("Erreur fatale lors de l'initialisation des comptes comptables dans ApprovisionnementDAOImpl: " + e.getMessage());
            e.printStackTrace();
            // Propager l'erreur comme une RuntimeException car l'application ne peut pas fonctionner sans ces comptes.
            throw new RuntimeException("Impossible d'initialiser les comptes comptables requis pour l'approvisionnement.", e);
        }
    }

    private void initComptesComptables() throws SQLException {
        // Assurez-vous que les comptes sont chargés et non null
        compteAchatsMarchandises = compteComptableDAO.getCompteByNumero("607");
        if (compteAchatsMarchandises == null) {
            throw new SQLException("Compte 607 (Achats de marchandises) introuvable. Vérifiez votre plan comptable.");
        }
        
        compteTVADeductible = compteComptableDAO.getCompteByNumero("4456");
        if (compteTVADeductible == null) {
            throw new SQLException("Compte 4456 (TVA Déductible) introuvable. Vérifiez votre plan comptable.");
        }

        compteCaisse = compteComptableDAO.getCompteByNumero("530");
        if (compteCaisse == null) {
            System.err.println("Avertissement: Compte 530 (Caisse) introuvable. Certaines fonctionnalités pourraient être affectées.");
            // Ne pas jeter d'exception critique ici si la caisse n'est pas essentielle pour l'appro.
        }

        compteFournisseurs = compteComptableDAO.getCompteByNumero("401"); 
        if (compteFournisseurs == null) {
            System.err.println("Avertissement: Compte 401 (Fournisseurs) introuvable. Certaines fonctionnalités pourraient être affectées.");
            // Ne pas jeter d'exception critique ici si 401 n'est pas directement utilisé pour le paiement instantané d'appro.
        }

        compteBanque = compteComptableDAO.getCompteByNumero("512");
        if (compteBanque == null) {
            throw new SQLException("Compte 512 (Banque) introuvable. Nécessaire pour les paiements d'approvisionnement. Vérifiez votre plan comptable.");
        }
        System.out.println("Comptes comptables pour approvisionnement initialisés : Achats=" + compteAchatsMarchandises.getNumeroCompte() + 
                           ", TVA=" + compteTVADeductible.getNumeroCompte() + ", Banque=" + compteBanque.getNumeroCompte());
    }

    @Override
    public boolean addApprovisionnement(Approvisionnement approvisionnement) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Début de la transaction
            boolean success = addApprovisionnement(conn, approvisionnement);
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
            if (conn != null) {
                conn.setAutoCommit(true); // Restaurer l'auto-commit par défaut
                conn.close();
            }
        }
    }

    @Override
    public boolean addApprovisionnement(Connection conn, Approvisionnement approvisionnement) throws SQLException {
        String sqlAppro = "INSERT INTO approvisionnements (date_approvisionnement, id_fournisseur, montant_total_ht, montant_total_ttc, montant_tva, reference_bon_commande) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmtAppro = null;
        ResultSet rsAppro = null;

        try {
            // 1. Insérer l'approvisionnement principal
            pstmtAppro = conn.prepareStatement(sqlAppro, Statement.RETURN_GENERATED_KEYS);
            pstmtAppro.setTimestamp(1, Timestamp.valueOf(approvisionnement.getDateApprovisionnement()));
            pstmtAppro.setInt(2, approvisionnement.getFournisseur().getId());
            pstmtAppro.setDouble(3, approvisionnement.getMontantTotalHt());
            pstmtAppro.setDouble(4, approvisionnement.getMontantTotalTtc());
            pstmtAppro.setDouble(5, approvisionnement.getMontantTva());
            pstmtAppro.setString(6, approvisionnement.getReferenceBonCommande());

            int rowsAffected = pstmtAppro.executeUpdate();
            if (rowsAffected == 0) {
                return false;
            }

            rsAppro = pstmtAppro.getGeneratedKeys();
            if (rsAppro.next()) {
                approvisionnement.setId(rsAppro.getInt(1));
            } else {
                throw new SQLException("Échec de la récupération de l'ID de l'approvisionnement.");
            }

            // 2. Insérer les lignes d'approvisionnement et mettre à jour le stock
            for (LigneApprovisionnement ligne : approvisionnement.getLignesApprovisionnement()) {
                ligne.setIdApprovisionnement(approvisionnement.getId());
                if (!ligneApprovisionnementDAO.addLigneApprovisionnement(conn, ligne)) {
                    return false;
                }

                // Mise à jour du stock du produit
                Produit produitActuel = produitDAO.findProduitById(ligne.getProduit().getId());
                if (produitActuel == null) {
                    throw new SQLException("Produit introuvable lors de la mise à jour du stock: ID " + ligne.getProduit().getId());
                }
                int nouveauStock = produitActuel.getQuantite() + ligne.getQuantiteCommandee();
                if (!produitDAO.mettreAJourQuantite(conn, produitActuel.getReference(), nouveauStock)) {
                    return false;
                }
            }

            // --- NOUVEAU: 3. Générer les écritures comptables pour l'approvisionnement (paiement DIRECT via BANQUE) ---
            LocalDateTime transactionDate = approvisionnement.getDateApprovisionnement();
            String reference = approvisionnement.getReferenceBonCommande();
            int sourceId = approvisionnement.getId();
            String descriptionBase = "Approvisionnement Fact. Fournisseur " + approvisionnement.getFournisseur().getNomFournisseur() + " (Réf: " + reference + ")";

            // Vérifications cruciales avant de créer les transactions
            if (compteAchatsMarchandises == null) {
                throw new SQLException("Le compte d'achats (607) n'est pas initialisé. Impossible de créer la transaction de débit.");
            }
            if (compteBanque == null) {
                throw new SQLException("Le compte de banque (512) n'est pas initialisé. Impossible de créer la transaction de crédit.");
            }

            // Écriture 1: Débit des Achats de marchandises (HT)
            TransactionComptable transAchatsHT = new TransactionComptable(
                transactionDate,
                reference,
                descriptionBase + " - Achats HT",
                approvisionnement.getMontantTotalHt(),
                compteAchatsMarchandises, // Compte de débit
                compteBanque,             // Compte de crédit
                "APPROVISIONNEMENT",
                sourceId
            );
            if (!transactionComptableDAO.addTransaction(conn, transAchatsHT)) {
                return false;
            }

            // Écriture 2: Débit de la TVA déductible (si applicable)
            if (approvisionnement.getMontantTva() > 0) {
                if (compteTVADeductible == null) { // Vérification si le compte TVA est null
                    throw new SQLException("Le compte de TVA déductible (4456) n'est pas initialisé. Impossible de créer la transaction de TVA.");
                }
                TransactionComptable transTVADeductible = new TransactionComptable(
                    transactionDate,
                    reference,
                    descriptionBase + " - TVA Déductible",
                    approvisionnement.getMontantTva(),
                    compteTVADeductible,      // Compte de débit
                    compteBanque,             // Compte de crédit
                    "APPROVISIONNEMENT_TVA",
                    sourceId
                );
                if (!transactionComptableDAO.addTransaction(conn, transTVADeductible)) {
                    return false;
                }
            }
            return true;
        } finally {
            DatabaseManager.close(null, pstmtAppro, rsAppro); // Ne ferme pas la connexion 'conn' ici
        }
    }

    @Override
    public Approvisionnement getApprovisionnementById(int id) throws SQLException {
        String sql = "SELECT id_approvisionnement, date_approvisionnement, id_fournisseur, montant_total_ht, montant_total_ttc, montant_tva, reference_bon_commande FROM approvisionnements WHERE id_approvisionnement = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractApprovisionnementFromResultSet(rs);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public Approvisionnement getApprovisionnementByReference(String referenceBonCommande) throws SQLException {
        String sql = "SELECT id_approvisionnement, date_approvisionnement, id_fournisseur, montant_total_ht, montant_total_ttc, montant_tva, reference_bon_commande FROM approvisionnements WHERE reference_bon_commande = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, referenceBonCommande);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractApprovisionnementFromResultSet(rs);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public List<Approvisionnement> getAllApprovisionnements() throws SQLException {
        List<Approvisionnement> approvisionnements = new ArrayList<>();
        // Jointure avec 'fournisseurs' pour extraire les informations du fournisseur directement
        String sql = "SELECT a.id_approvisionnement, a.date_approvisionnement, a.montant_total_ht, a.montant_total_ttc, a.montant_tva, a.reference_bon_commande, " +
                     "f.id_fournisseur, f.nom_fournisseur, f.contact_fournisseur, f.telephone_fournisseur, f.email_fournisseur, f.adresse_fournisseur " +
                     "FROM approvisionnements a JOIN fournisseurs f ON a.id_fournisseur = f.id_fournisseur ORDER BY a.date_approvisionnement DESC";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                approvisionnements.add(extractApprovisionnementFromResultSet(rs));
            }
        } finally {
            DatabaseManager.close(conn, stmt, rs);
        }
        return approvisionnements;
    }

    @Override
    public List<Approvisionnement> getApprovisionnementsByFournisseur(int idFournisseur) throws SQLException {
        List<Approvisionnement> approvisionnements = new ArrayList<>();
        String sql = "SELECT a.id_approvisionnement, a.date_approvisionnement, a.montant_total_ht, a.montant_total_ttc, a.montant_tva, a.reference_bon_commande, " +
                     "f.id_fournisseur, f.nom_fournisseur, f.contact_fournisseur, f.telephone_fournisseur, f.email_fournisseur, f.adresse_fournisseur " +
                     "FROM approvisionnements a JOIN fournisseurs f ON a.id_fournisseur = f.id_fournisseur WHERE a.id_fournisseur = ? ORDER BY a.date_approvisionnement DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idFournisseur);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                approvisionnements.add(extractApprovisionnementFromResultSet(rs));
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return approvisionnements;
    }

    @Override
    public List<Approvisionnement> getApprovisionnementsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<Approvisionnement> approvisionnements = new ArrayList<>();
        String sql = "SELECT a.id_approvisionnement, a.date_approvisionnement, a.montant_total_ht, a.montant_total_ttc, a.montant_tva, a.reference_bon_commande, " +
                     "f.id_fournisseur, f.nom_fournisseur, f.contact_fournisseur, f.telephone_fournisseur, f.email_fournisseur, f.adresse_fournisseur " +
                     "FROM approvisionnements a JOIN fournisseurs f ON a.id_fournisseur = f.id_fournisseur WHERE a.date_approvisionnement BETWEEN ? AND ? ORDER BY a.date_approvisionnement DESC";
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
                approvisionnements.add(extractApprovisionnementFromResultSet(rs));
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return approvisionnements;
    }

    @Override
    public boolean updateApprovisionnement(Approvisionnement approvisionnement) throws SQLException {
        String sql = "UPDATE approvisionnements SET date_approvisionnement = ?, id_fournisseur = ?, montant_total_ht = ?, montant_total_ttc = ?, montant_tva = ?, reference_bon_commande = ? WHERE id_approvisionnement = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.valueOf(approvisionnement.getDateApprovisionnement()));
            pstmt.setInt(2, approvisionnement.getFournisseur().getId());
            pstmt.setDouble(3, approvisionnement.getMontantTotalHt());
            pstmt.setDouble(4, approvisionnement.getMontantTotalTtc());
            pstmt.setDouble(5, approvisionnement.getMontantTva());
            pstmt.setString(6, approvisionnement.getReferenceBonCommande());
            pstmt.setInt(7, approvisionnement.getId());
            return pstmt.executeUpdate() > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public boolean deleteApprovisionnement(int id) throws SQLException {
        String sql = "DELETE FROM approvisionnements WHERE id_approvisionnement = ?";
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

    private Approvisionnement extractApprovisionnementFromResultSet(ResultSet rs) throws SQLException {
        // Tente de récupérer le fournisseur depuis la jointure pour éviter un appel DAO séparé si possible
        Fournisseur fournisseur = new Fournisseur(
            rs.getInt("id_fournisseur"),
            rs.getString("nom_fournisseur"),
            rs.getString("contact_fournisseur"),
            rs.getString("telephone_fournisseur"),
            rs.getString("email_fournisseur"),
            rs.getString("adresse_fournisseur")
        );
        // Si pour une raison quelconque la jointure ne ramène pas le fournisseur complet,
        // on pourrait toujours faire un appel à fournisseurDAO.getFournisseurById(rs.getInt("id_fournisseur"));
        // mais c'est moins performant.

        Approvisionnement approvisionnement = new Approvisionnement(
            rs.getInt("id_approvisionnement"),
            rs.getTimestamp("date_approvisionnement").toLocalDateTime(),
            fournisseur,
            rs.getDouble("montant_total_ht"),
            rs.getDouble("montant_total_ttc"),
            rs.getDouble("montant_tva"),
            rs.getString("reference_bon_commande")
        );
        // Charger les lignes d'approvisionnement pour cet approvisionnement (nécessite un appel DAO séparé)
        approvisionnement.setLignesApprovisionnement(ligneApprovisionnementDAO.getLignesApprovisionnementByApproId(approvisionnement.getId()));
        return approvisionnement;
    }
}
