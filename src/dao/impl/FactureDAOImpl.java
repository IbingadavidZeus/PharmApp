package dao.impl;

import dao.FactureDAO;
import dao.LigneFactureDAO;
import dao.ProduitDAO;
import dao.UtilisateurDAO;
import dao.AssuranceSocialDAO; 
import dao.CompteComptableDAO; // NOUVEAU
import dao.TransactionComptableDAO; // NOUVEAU
import dao.DatabaseManager;
import model.Facture;
import model.LigneFacture;
import model.Produit;
import model.AssuranceSocial; 
import model.Utilisateur;
import model.CompteComptable; // NOUVEAU
import model.TransactionComptable; // NOUVEAU

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FactureDAOImpl implements FactureDAO {
    protected UtilisateurDAO utilisateurDAO;
    protected LigneFactureDAO ligneFactureDAO;
    protected ProduitDAO produitDAO;
    protected AssuranceSocialDAO assuranceSocialDAO; 
    protected CompteComptableDAO compteComptableDAO;     // NOUVEAU: pour accéder aux comptes comptables
    protected TransactionComptableDAO transactionComptableDAO; // NOUVEAU: pour enregistrer les transactions

    // Comptes comptables clés (initialisés une seule fois si possible)
    private CompteComptable compteVentesMarchandises;
    private CompteComptable compteTVACollectee;
    private CompteComptable compteCaisse;
    private CompteComptable compteClients;
    // private CompteComptable compteBanque; // Si vous gérez Caisse et Banque séparément pour l'encaissement

    public FactureDAOImpl(UtilisateurDAO utilisateurDAO, LigneFactureDAO ligneFactureDAO, ProduitDAO produitDAO, AssuranceSocialDAO assuranceSocialDAO,
                          CompteComptableDAO compteComptableDAO, TransactionComptableDAO transactionComptableDAO) { // NOUVEAU: constructeur mis à jour
        this.utilisateurDAO = utilisateurDAO;
        this.ligneFactureDAO = ligneFactureDAO;
        this.produitDAO = produitDAO;
        this.assuranceSocialDAO = assuranceSocialDAO;
        this.compteComptableDAO = compteComptableDAO;
        this.transactionComptableDAO = transactionComptableDAO;
        
        try {
            initComptesComptables();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'initialisation des comptes comptables dans FactureDAOImpl: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossible d'initialiser les comptes comptables requis pour la vente.", e);
        }
    }

    private void initComptesComptables() throws SQLException {
        compteVentesMarchandises = compteComptableDAO.getCompteByNumero("707");
        compteTVACollectee = compteComptableDAO.getCompteByNumero("4457");
        compteCaisse = compteComptableDAO.getCompteByNumero("530"); 
        compteClients = compteComptableDAO.getCompteByNumero("411");
        
        if (compteVentesMarchandises == null || compteTVACollectee == null || compteCaisse == null || compteClients == null) {
            throw new SQLException("Un ou plusieurs comptes comptables nécessaires (707, 4457, 530, 411) sont introuvables en base de données.");
        }
    }

    @Override
    public boolean ajouterFacture(Facture facture) throws SQLException {
        Connection conn = null;
        PreparedStatement stmtFacture = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Début de la transaction

            // 1. Insérer la facture
            String sqlFacture = "INSERT INTO factures (numero_facture, date_facture, id_utilisateur, total_ht, total_ttc, id_assurance, montant_prise_en_charge, montant_restant_a_payer_client) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            stmtFacture = conn.prepareStatement(sqlFacture, Statement.RETURN_GENERATED_KEYS);
            stmtFacture.setString(1, facture.getNumeroFacture());
            stmtFacture.setTimestamp(2, Timestamp.valueOf(facture.getDateFacture()));
            stmtFacture.setInt(3, facture.getUtilisateur().getId());
            stmtFacture.setDouble(4, facture.getTotalHt());
            stmtFacture.setDouble(5, facture.getMontantTotal()); // Montant Total TTC de la facture
            
            if (facture.getAssuranceSocial() != null) {
                stmtFacture.setInt(6, facture.getAssuranceSocial().getId_assurance());
            } else {
                stmtFacture.setNull(6, Types.INTEGER);
            }
            stmtFacture.setDouble(7, facture.getMontantPrisEnChargeAssurance()); 
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

            // --- NOUVEAU: 3. Générer les écritures comptables ---
            LocalDateTime transactionDate = facture.getDateFacture();
            String reference = facture.getNumeroFacture();
            int sourceId = facture.getId();

            // Calcul de la TVA (Total TTC - Total HT)
            double montantTVA = facture.getMontantTotal() - facture.getTotalHt();

            // Écriture 1: Reconnaissance du revenu et de la TVA collectée
            // Débit: Compte Client (pour le montant total de la facture, qu'il soit payé par le client ou l'assurance)
            // Crédit: Ventes de marchandises (HT)
            // Crédit: TVA collectée (TVA)
            TransactionComptable transactionVenteGlobale = new TransactionComptable(
                transactionDate,
                reference,
                "Vente de produits (Facture " + reference + ")",
                facture.getMontantTotal(), // Montant total TTC
                compteClients, // Le client est initialement débité du montant total TTC
                compteVentesMarchandises, // Crédit de la vente HT
                "VENTE",
                sourceId
            );
            // Pour les débits et crédits multiples sur une même transaction, il est parfois préférable de faire 2 transactions distinctes.
            // Option 1: Une transaction pour le total TTC (Débit Client / Crédit Ventes + TVA) -> non supporté directement par notre modèle simple
            // Option 2: Plusieurs transactions simples (plus facile à gérer avec notre modèle actuel)

            // Simplifions pour notre modèle CompteDebit / CompteCredit:
            // 1. Reconnaissance de la Vente (HT)
            // D: Clients (ou Banque/Caisse si paiement direct)
            // C: Ventes de marchandises
            TransactionComptable transVenteHT = new TransactionComptable(
                transactionDate,
                reference,
                "Vente HT (Facture " + reference + ")",
                facture.getTotalHt(),
                compteClients, // Ou Caisse/Banque si paiement comptant toujours
                compteVentesMarchandises,
                "VENTE",
                sourceId
            );
            if (!transactionComptableDAO.addTransaction(conn, transVenteHT)) {
                conn.rollback();
                return false;
            }

            // 2. Reconnaissance de la TVA collectée
            // D: Clients (ou Banque/Caisse si paiement direct)
            // C: TVA collectée
            if (montantTVA > 0) {
                TransactionComptable transTVA = new TransactionComptable(
                    transactionDate,
                    reference,
                    "TVA collectée (Facture " + reference + ")",
                    montantTVA,
                    compteClients, // Ou Caisse/Banque si paiement comptant toujours
                    compteTVACollectee,
                    "VENTE_TVA",
                    sourceId
                );
                if (!transactionComptableDAO.addTransaction(conn, transTVA)) {
                    conn.rollback();
                    return false;
                }
            }
            
            // 3. Encaissement du client (partie client)
            if (facture.getMontantRestantAPayerClient() > 0) {
                TransactionComptable transEncaissementClient = new TransactionComptable(
                    transactionDate,
                    reference,
                    "Encaissement client (Facture " + reference + ")",
                    facture.getMontantRestantAPayerClient(),
                    compteCaisse, // Assumons que le paiement client est toujours en caisse pour l'instant
                    compteClients,
                    "ENCADD_VENTE_CLIENT",
                    sourceId
                );
                if (!transactionComptableDAO.addTransaction(conn, transEncaissementClient)) {
                    conn.rollback();
                    return false;
                }
            }

            // 4. Montant pris en charge par l'assurance (créance sur l'assurance)
            if (facture.getAssuranceSocial() != null && facture.getMontantPrisEnChargeAssurance() > 0) {
                // Si l'assurance est une créance distincte, elle reste sur le compte client, mais nous la reconnaissons ici.
                // L'encaissement de l'assurance se fera plus tard par une autre transaction.
                // Pour notre modèle simple, la "créance client" est une abstraction qui contient aussi l'assurance.
                // Ici, nous n'avons pas besoin d'une transaction séparée, car la créance sur l'assurance
                // est déjà implicitement gérée par la différence entre le montant total de la vente et ce que le client a payé.
                // L'important est que le "compte Clients" reflète correctement la somme due par l'assurance.
                // Pour la traçabilité:
                 TransactionComptable transCreanceAssurance = new TransactionComptable(
                    transactionDate,
                    reference,
                    "Créance assurance (Facture " + reference + ") - " + facture.getAssuranceSocial().getNom_assurance(),
                    facture.getMontantPrisEnChargeAssurance(),
                    compteClients, // Débit de clients (ou un sous-compte spécifique pour créances assurances)
                    compteVentesMarchandises, // Crédit à ventes (cela double un peu, donc ajustons)
                                                // La meilleure approche serait:
                                                // 1. D: Client TTA, C: Ventes HT, C: TVA (global)
                                                // 2. D: Caisse, C: Client (pour la partie client)
                                                // => La partie assurance reste "débitée" sur le client tant qu'elle n'est pas payée.
                                                // Notre modèle actuel gère l'Écriture 1, et l'Écriture 2 ne couvre que la part client.
                                                // Donc la différence (montant assurance) reste dans le débit de `compteClients`.
                                                // Nous n'avons pas besoin d'une transaction explicite pour cela pour le moment.
                                                // La "créance" sur l'assurance est la partie du `compteClients` qui n'a pas été encaissée.
                    "VENTE_ASSURANCE",
                    sourceId
                );
                // ATTENTION: La transaction ci-dessus est REDONDANTE avec la première si on la met en place telle quelle.
                // Le `compteClients` doit être débité du montant total de la vente, puis crédité des paiements (client ou assurance).
                // Initialement, `compteClients` est débité du `montantTotal` (TTC).
                // Ensuite, `compteCaisse` est débité et `compteClients` est crédité du `montantRestantAPayerClient`.
                // Il reste donc sur `compteClients` le `montantPrisEnChargeAssurance` qui représente la créance sur l'assurance.
                // Pas besoin d'une écriture supplémentaire ici pour la créance d'assurance.
                // Cette écriture serait pertinente si le `compteClients` n'était pas débité du total initial, mais juste de la part client.
                // Pour l'instant, on s'en tient aux transactions 1 et 2 ci-dessus.
            }
            // FIN NOUVEAU: 3. Générer les écritures comptables

            conn.commit(); // Valider la transaction si toutes les opérations réussissent
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Annuler la transaction en cas d'erreur
                } catch (SQLException ex) {
                    System.err.println("Erreur lors du rollback des transactions comptables: " + ex.getMessage());
                }
            }
            throw e; // Renvoyer l'exception pour être gérée plus haut
        } finally {
            if (stmtFacture != null) {
                stmtFacture.close();
            }
            if (conn != null) {
                conn.setAutoCommit(true); // Restaurer l'auto-commit par défaut
                conn.close(); // Fermer la connexion
            }
        }
    }

    // --- Les autres méthodes du FactureDAOImpl restent inchangées ---
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
