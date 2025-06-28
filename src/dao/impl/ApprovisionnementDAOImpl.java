package dao.impl;

import dao.ApprovisionnementDAO;
import dao.FournisseurDAO;
import dao.LigneApprovisionnementDAO;
import dao.ProduitDAO; // Pour mettre à jour les stocks
import dao.CompteComptableDAO; // Pour les écritures comptables
import dao.TransactionComptableDAO; // Pour les écritures comptables
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

    protected FournisseurDAO fournisseurDAO;
    protected LigneApprovisionnementDAO ligneApprovisionnementDAO;
    protected ProduitDAO produitDAO; 
    protected CompteComptableDAO compteComptableDAO;
    protected TransactionComptableDAO transactionComptableDAO;

    // Comptes comptables clés (initialisés une seule fois si possible)
    protected CompteComptable compteAchatsMarchandises;
    protected CompteComptable compteTVADeductible;
    protected CompteComptable compteCaisse;
    protected CompteComptable compteFournisseurs; 

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
            System.err.println("Erreur lors de l'initialisation des comptes comptables dans ApprovisionnementDAOImpl: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossible d'initialiser les comptes comptables requis pour l'approvisionnement.", e);
        }
    }

    private void initComptesComptables() throws SQLException {
        compteAchatsMarchandises = compteComptableDAO.getCompteByNumero("607");
        compteTVADeductible = compteComptableDAO.getCompteByNumero("4456");
        compteCaisse = compteComptableDAO.getCompteByNumero("530");
        compteFournisseurs = compteComptableDAO.getCompteByNumero("401"); // Compte Fournisseurs

        if (compteAchatsMarchandises == null || compteTVADeductible == null || compteCaisse == null || compteFournisseurs == null) {
            throw new SQLException("Un ou plusieurs comptes comptables nécessaires (607, 4456, 530, 401) sont introuvables en base de données.");
        }
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
            throw e;
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
                if (!produitDAO.mettreAJourQuantite(conn, produitActuel.getReference(), nouveauStock)) { // Utiliser la surcharge avec connexion
                    return false;
                }
            }

            // --- NOUVEAU: 3. Générer les écritures comptables pour l'approvisionnement ---
            LocalDateTime transactionDate = approvisionnement.getDateApprovisionnement();
            String reference = approvisionnement.getReferenceBonCommande();
            int sourceId = approvisionnement.getId();
            String descriptionBase = "Approvisionnement Fact. Fournisseur " + approvisionnement.getFournisseur().getNomFournisseur() + " (Réf: " + reference + ")";

            // Écriture 1: Débit des Achats de marchandises (HT)
            TransactionComptable transAchatsHT = new TransactionComptable(
                transactionDate,
                reference,
                descriptionBase + " - Achats HT",
                approvisionnement.getMontantTotalHt(),
                compteAchatsMarchandises,
                compteFournisseurs, // Assumons initialement que tous les achats sont à crédit via le compte Fournisseurs
                "ACHAT",
                sourceId
            );
            if (!transactionComptableDAO.addTransaction(conn, transAchatsHT)) {
                return false;
            }

            // Écriture 2: Débit de la TVA déductible
            if (approvisionnement.getMontantTva() > 0) {
                TransactionComptable transTVADeductible = new TransactionComptable(
                    transactionDate,
                    reference,
                    descriptionBase + " - TVA Déductible",
                    approvisionnement.getMontantTva(),
                    compteTVADeductible,
                    compteFournisseurs, // Assumons que la TVA est aussi due au fournisseur
                    "ACHAT_TVA",
                    sourceId
                );
                if (!transactionComptableDAO.addTransaction(conn, transTVADeductible)) {
                    return false;
                }
            }

            // Si l'approvisionnement est payé comptant, il faut générer une transaction de règlement
            // Pour l'instant, on assume que la méthode d'approvisionnement est juste l'enregistrement de la dette fournisseur.
            // Le paiement réel (Caisse -> Fournisseurs) sera une opération séparée à gérer.
            // Pour l'intégration actuelle de `ApprovisionnementPanel` qui ne gère pas le paiement, cette écriture est suffisante.
            // Si on veut simuler un paiement direct:
            /*
            TransactionComptable transPaiementFournisseur = new TransactionComptable(
                transactionDate,
                reference,
                descriptionBase + " - Paiement Fournisseur",
                approvisionnement.getMontantTotalTtc(), // Montant TTC payé
                compteFournisseurs, // Débit du compte Fournisseurs (la dette diminue)
                compteCaisse, // Crédit de la Caisse (l'argent sort)
                "PAIEMENT_ACHAT",
                sourceId
            );
            if (!transactionComptableDAO.addTransaction(conn, transPaiementFournisseur)) {
                return false;
            }
            */
            // FIN NOUVEAU: 3. Générer les écritures comptables

            return true;
        } finally {
            DatabaseManager.close(null, pstmtAppro, rsAppro); // Ne ferme pas la connexion 'conn' ici
        }
    }


    @Override
    public Approvisionnement getApprovisionnementById(int id) throws SQLException {
        String sql = "SELECT a.id_approvisionnement, a.date_approvisionnement, a.montant_total_ht, a.montant_total_ttc, a.montant_tva, a.reference_bon_commande, " +
                     "f.id_fournisseur, f.nom_fournisseur, f.contact_fournisseur, f.telephone_fournisseur, f.email_fournisseur, f.adresse_fournisseur " +
                     "FROM approvisionnements a JOIN fournisseurs f ON a.id_fournisseur = f.id_fournisseur WHERE a.id_approvisionnement = ?";
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
        String sql = "SELECT a.id_approvisionnement, a.date_approvisionnement, a.montant_total_ht, a.montant_total_ttc, a.montant_tva, a.reference_bon_commande, " +
                     "f.id_fournisseur, f.nom_fournisseur, f.contact_fournisseur, f.telephone_fournisseur, f.email_fournisseur, f.adresse_fournisseur " +
                     "FROM approvisionnements a JOIN fournisseurs f ON a.id_fournisseur = f.id_fournisseur WHERE a.reference_bon_commande = ?";
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
        String sql = "SELECT a.id_approvisionnement, a.date_approvisionnement, a.montant_total_ht, a.montant_total_ttc, a.montant_tva, a.reference_bon_commande, " +
                     "f.id_fournisseur, f.nom_fournisseur, f.contact_fournisseur, f.telephone_fournisseur, f.email_fournisseur, f.adresse_fournisseur" +
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
        Fournisseur fournisseur = fournisseurDAO.getFournisseurById(rs.getInt("id_fournisseur")); // Récupérer l'objet Fournisseur
        if (fournisseur == null) {
            throw new SQLException("Fournisseur associé à l'approvisionnement ID " + rs.getInt("id_approvisionnement") + " introuvable.");
        }

        Approvisionnement approvisionnement = new Approvisionnement(
            rs.getInt("id_approvisionnement"),
            rs.getTimestamp("date_approvisionnement").toLocalDateTime(),
            fournisseur,
            rs.getDouble("montant_total_ht"),
            rs.getDouble("montant_total_ttc"),
            rs.getDouble("montant_tva"),
            rs.getString("reference_bon_commande")
        );
        // Charger les lignes d'approvisionnement pour cet approvisionnement
        approvisionnement.setLignesApprovisionnement(ligneApprovisionnementDAO.getLignesApprovisionnementByApproId(approvisionnement.getId()));
        return approvisionnement;
    }
}
