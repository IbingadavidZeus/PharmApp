package model;

import dao.FactureDAO;
import dao.LigneFactureDAO;
import dao.ProduitDAO;
import dao.UtilisateurDAO;
import dao.AssuranceSocialDAO;
import dao.CompteComptableDAO; 
import dao.TransactionComptableDAO; 
import dao.impl.FactureDAOImpl;
import dao.impl.LigneFactureDAOImpl;
import dao.impl.ProduitDAOImpl;
import dao.impl.UtilisateurDAOImpl;
import dao.impl.AssuranceSocialDAOImpl;
import dao.impl.CompteComptableDAOImpl; 
import dao.impl.TransactionComptableDAOImpl; 
import dao.DatabaseManager; 
import java.io.*;
import java.sql.Connection; 
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Pharmacie implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nom;
    private String adresse;

    private transient ProduitDAO produitDAO;
    private transient UtilisateurDAO utilisateurDAO;
    private transient FactureDAO factureDAO;
    private transient LigneFactureDAO ligneFactureDAO;
    private transient AssuranceSocialDAO AssuranceSocialDAO;
    private transient CompteComptableDAO compteComptableDAO; 
    private transient TransactionComptableDAO transactionComptableDAO; 

    // Comptes comptables spécifiques aux approvisionnements (pour optimisation)
    private transient CompteComptable compteAchatsMarchandises;
    private transient CompteComptable compteTVADeductible;
    private transient CompteComptable compteCaissePourAchats; // ou Banque, ou Fournisseurs

    public Pharmacie(String nom, String adresse) {
        this.nom = nom;
        this.adresse = adresse;
        initDAOs();
        // Initialiser les comptes comptables requis après l'initialisation des DAOs
        try {
            initAccountingAccounts();
        } catch (SQLException e) {
            System.err.println("Erreur fatale lors de l'initialisation des comptes comptables: " + e.getMessage());
            // Il est critique que ces comptes existent. Si ce n'est pas le cas, l'application peut ne pas fonctionner correctement.
            // Vous pourriez vouloir lancer une RuntimeException ici si l'application ne peut pas démarrer sans eux.
             throw new RuntimeException("Échec de l'initialisation des comptes comptables requis.", e);
        }
    }

    private void initDAOs() {
        this.produitDAO = new ProduitDAOImpl();
        this.utilisateurDAO = new UtilisateurDAOImpl();
        this.ligneFactureDAO = new LigneFactureDAOImpl(produitDAO);
        this.AssuranceSocialDAO = new AssuranceSocialDAOImpl();
        this.compteComptableDAO = new CompteComptableDAOImpl(); 
        this.transactionComptableDAO = new TransactionComptableDAOImpl(compteComptableDAO); 
        
        this.factureDAO = new FactureDAOImpl(utilisateurDAO, ligneFactureDAO, produitDAO, AssuranceSocialDAO,
                                             compteComptableDAO, transactionComptableDAO); 
    }
    
    // NOUVEAU: Méthode pour initialiser les objets CompteComptable nécessaires aux transactions
    private void initAccountingAccounts() throws SQLException {
        // Ces numéros de compte doivent correspondre à ceux de votre BDD
        compteAchatsMarchandises = compteComptableDAO.getCompteByNumero("607");
        compteTVADeductible = compteComptableDAO.getCompteByNumero("4456");
        compteCaissePourAchats = compteComptableDAO.getCompteByNumero("530"); // Assumons le paiement en caisse

        if (compteAchatsMarchandises == null || compteTVADeductible == null || compteCaissePourAchats == null) {
            throw new SQLException("Un ou plusieurs comptes comptables nécessaires (607, 4456, 530) sont introuvables en base de données.");
        }
    }

    // Gère la désérialisation: réinitialise les transients DAOs et les comptes comptables
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        initDAOs();
        try {
            initAccountingAccounts(); // Réinitialiser aussi les comptes comptables
        } catch (SQLException e) {
            throw new IOException("Erreur lors de la réinitialisation des comptes comptables après désérialisation", e);
        }
    }

    // --- Getters et Setters pour les informations de la pharmacie ---
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    // --- Méthodes pour la gestion des produits (délèguent à ProduitDAO) ---
    public boolean ajouterProduit(Produit produit) throws SQLException {
        return produitDAO.ajouterProduit(produit);
    }

    public boolean mettreAJourProduit(Produit produit) throws SQLException {
        return produitDAO.mettreAJourProduit(produit);
    }

    public boolean supprimerProduit(String reference) throws SQLException {
        return produitDAO.supprimerProduit(reference);
    }

    public Produit getProduitByReference(String reference) {
        try {
            return produitDAO.trouverParReference(reference);
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la récupération du produit par référence: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public Produit getProduitById(int id) { 
        try {
            return produitDAO.findProduitById(id);
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la récupération du produit par ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    public List<Produit> getProduits() throws SQLException {
        return produitDAO.getAllProduits();
    }
    
    public List<Produit> rechercherProduits(String critere) throws SQLException {
        return produitDAO.rechercherProduits(critere);
    }

    public boolean mettreAJourQuantiteProduit(String reference, int nouvelleQuantite) throws SQLException {
        return produitDAO.mettreAJourQuantite(reference, nouvelleQuantite);
    }

    // Cette méthode a été mise à jour dans la logique
    public boolean approvisionnerProduit(String reference, int quantiteAAjouter) throws SQLException {
        if (quantiteAAjouter <= 0) {
            throw new IllegalArgumentException("La quantité à ajouter doit être positive.");
        }

        Connection conn = null; // Déclaration de la connexion pour la transaction
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Démarrer la transaction

            Produit produit = getProduitByReference(reference);
            if (produit == null) {
                throw new IllegalArgumentException("Produit avec la référence '" + reference + "' introuvable.");
            }

            int nouvelleQuantiteTotale = produit.getQuantite() + quantiteAAjouter;
            
            // Mettre à jour la quantité du produit dans la même transaction
            boolean stockUpdated = produitDAO.mettreAJourQuantite(conn, reference, nouvelleQuantiteTotale);
            if (!stockUpdated) {
                conn.rollback();
                return false;
            }

            // --- NOUVEAU: Générer les écritures comptables pour l'approvisionnement ---
            LocalDateTime transactionDate = LocalDateTime.now();
            String referencePiece = "APPR-" + produit.getReference() + "-" + transactionDate.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")); // Ex: APPR-REFPROD-20231027143000
            String description = "Approvisionnement de " + quantiteAAjouter + " " + produit.getNom();
            
            // Calcul du montant HT et TTC de cet approvisionnement
            double montantHTAppro = produit.getPrixHt() * quantiteAAjouter;
            double montantTTCAppro = produit.calculerPrixTTC() * quantiteAAjouter;
            double montantTVAAppro = montantTTCAppro - montantHTAppro;

            // 1. Écriture pour la charge d'achat (HT)
            TransactionComptable transAchatHT = new TransactionComptable(
                transactionDate,
                referencePiece,
                description + " (HT)",
                montantHTAppro,
                compteAchatsMarchandises, 
                compteCaissePourAchats,   // Crédit (sera ajusté par la TVA)
                "ACHAT",
                produit.getId() // ID du produit comme source
            );
            // NOTE: Pour que le débit = crédit soit respecté dans une entrée séparée,
            // il faudrait un compte "intérim". L'idéal est de faire 3 transactions ou 1 transaction composée.
            // Avec notre modèle actuel:
            // D: Achats (HT)
            // D: TVA Déductible
            // C: Caisse (TTC)
            // Simplifions en 2 transactions pour respecter Débit/Crédit par transaction:
            // Transaction 1: Débit Achats (HT) / Crédit Caisse (HT temporaire)
            // Transaction 2: Débit TVA Déductible / Crédit Caisse (TVA temporaire)
            // Transaction 3: Ajustement Caisse - Fournisseurs...
            // Pour notre modèle, allons-y directement:
            // 1. Débit: Achats de marchandises (Montant HT)
            //    Crédit: Caisse (Montant HT) -- C'est une simplification pour enregistrer la dépense comme une charge
            //    La différence sera que la caisse sera créditée 2 fois pour le même achat TTC.
            //    L'approche la plus fidèle au modèle Double Entry Bookkeeping est la suivante:

            // Écriture comptable (débit = crédit) pour l'approvisionnement
            // Débit Achats (charge) et Débit TVA (actif) / Crédit Caisse (actif)
            
            // On peut faire deux transactions distinctes pour un même événement si les comptes sont différents:
            // Transaction 1: Coût des marchandises (HT)
            TransactionComptable transAchats = new TransactionComptable(
                transactionDate,
                referencePiece,
                description + " (Coût HT)",
                montantHTAppro,
                compteAchatsMarchandises,
                compteCaissePourAchats, // Temporaire, le crédit réel sera sur le TTC
                "ACHAT",
                produit.getId()
            );
            // Le problème de ce modèle TransactionComptable simple est qu'il force D=C par transaction.
            // Pour la simplicité, on va faire des transactions qui reflètent la nature de la dépense/recepte.
            // La somme des débits DOIT égaler la somme des crédits sur l'ensemble des opérations.

            // Solution plus robuste pour le double entry avec un modèle simple de TransactionComptable (une paire D/C par entrée)
            // On enregistre les deux côtés de l'opération:
            // 1. Enregistrement de la charge/actif (Achats/TVA)
            // 2. Enregistrement du paiement (diminution Caisse/augmentation Fournisseur)

            // Enregistrement de l'augmentation des achats (charge)
            TransactionComptable transAchatsCharge = new TransactionComptable(
                transactionDate,
                referencePiece,
                "Augmentation Achats Stock " + produit.getNom() + " (HT)",
                montantHTAppro,
                compteAchatsMarchandises,
                compteCaissePourAchats, // on assume un paiement pour cette part
                "ACHAT",
                produit.getId()
            );
            if (!transactionComptableDAO.addTransaction(conn, transAchatsCharge)) {
                conn.rollback();
                return false;
            }

            // Enregistrement de la TVA déductible (actif)
            if (montantTVAAppro > 0) {
                TransactionComptable transTVADeductible = new TransactionComptable(
                    transactionDate,
                    referencePiece,
                    "TVA déductible sur Achat " + produit.getNom(),
                    montantTVAAppro,
                    compteTVADeductible,
                    compteCaissePourAchats, // on assume un paiement pour cette part
                    "ACHAT_TVA",
                    produit.getId()
                );
                if (!transactionComptableDAO.addTransaction(conn, transTVADeductible)) {
                    conn.rollback();
                    return false;
                }
            }
            
            // Enregistrement du paiement réel (TTC)
            // Pour équilibrer les débits précédents, le compte caisse a été crédité du montant HT et du montant TVA.
            // Donc le débit total sur Achats et TVA déductible est montantHT + montantTVA = montantTTC.
            // Ici, on crédite Caisse pour le montant TTC global.
            // Cela implique que les transactions précédentes de "crédit caisse" sont juste des place-holders.

            // Repensons la logique comptable pour notre modèle simple (un seul débit et un seul crédit par TransactionComptable):
            // L'achat impacte le stock (ou une charge) et la trésorerie.
            // Le plus simple est d'enregistrer la *dépense* totale TTC contre la *caisse*.
            // Et ensuite enregistrer la *charge d'achat HT* contre un compte intermédiaire, et *TVA déductible* contre ce même compte intermédiaire.
            // Cela devient complexe avec notre modèle de transaction simple.

            // Simplification Maximale:
            // Pour l'approvisionnement, le flux de trésorerie est direct: de la Caisse vers l'extérieur.
            // L'impact sur les charges est séparé.
            // Débit : Achats (pour HT) et TVA Déductible (pour TVA)
            // Crédit : Caisse (pour TTC)

            // Avec un seul CompteDebit et un seul CompteCredit par TransactionComptable, on ne peut pas faire D1, D2 / C1 directement.
            // Option la plus simple et la plus courante pour un tel modèle:
            // 1. Débit Achats de marchandises (pour HT) / Crédit Caisse (pour HT)
            // 2. Débit TVA déductible (pour TVA) / Crédit Caisse (pour TVA)
            // Ainsi, le total crédité à Caisse est bien TTC.
            // Cette approche sépare les débits et permet de les assigner au crédit de Caisse.

            // Écriture 1: Coût des marchandises (HT)
            TransactionComptable transAchatsForfaits = new TransactionComptable(
                transactionDate,
                referencePiece,
                "Achat marchandises " + produit.getNom() + " (HT)",
                montantHTAppro,
                compteAchatsMarchandises,
                compteCaissePourAchats, 
                "ACHAT",
                produit.getId()
            );
            if (!transactionComptableDAO.addTransaction(conn, transAchatsForfaits)) {
                conn.rollback();
                return false;
            }

            // Écriture 2: TVA déductible
            if (montantTVAAppro > 0) {
                TransactionComptable transTVADeductibleComp = new TransactionComptable(
                    transactionDate,
                    referencePiece,
                    "TVA déductible sur achat " + produit.getNom(),
                    montantTVAAppro,
                    compteTVADeductible,
                    compteCaissePourAchats, 
                    "ACHAT_TVA",
                    produit.getId()
                );
                if (!transactionComptableDAO.addTransaction(conn, transTVADeductibleComp)) {
                    conn.rollback();
                    return false;
                }
            }
            // --- FIN NOUVEAU: Générer les écritures comptables ---

            conn.commit(); // Valider la transaction
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Annuler la transaction en cas d'erreur
                } catch (SQLException ex) {
                    System.err.println("Erreur lors du rollback de l'approvisionnement et des transactions comptables: " + ex.getMessage());
                }
            }
            throw e; // Renvoyer l'exception pour être gérée plus haut
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // Restaurer l'auto-commit par défaut
                conn.close(); // Fermer la connexion
            }
        }
    }

    public double calculerValeurTotaleStock() throws SQLException {
        double valeurTotale = 0.0;
        List<Produit> allProducts = produitDAO.getAllProduits();
        for (Produit p : allProducts) {
            valeurTotale += p.getQuantite() * p.calculerPrixTTC();
        }
        return valeurTotale;
    }


    // --- Méthodes pour l'authentification et la gestion des utilisateurs (délèguent à UtilisateurDAO) ---

    public Utilisateur authentifier(String nomUtilisateur, String motDePasse) {
        try {
            return utilisateurDAO.authentifierUtilisateur(nomUtilisateur, motDePasse);
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'authentification: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean ajouterUtilisateur(Utilisateur utilisateur, String plainPassword) throws SQLException {
        utilisateur.setMotDePasse(plainPassword);
        return utilisateurDAO.ajouterUtilisateur(utilisateur);
    }

    public boolean mettreAJourUtilisateur(Utilisateur utilisateur, String newPlainPassword) throws SQLException {
        if (newPlainPassword != null && !newPlainPassword.trim().isEmpty()) {
            utilisateur.setMotDePasse(newPlainPassword);
        } else {
            Utilisateur existingUser = utilisateurDAO.getUtilisateurById(utilisateur.getId());
            if (existingUser != null) {
                utilisateur.setMotDePasse(existingUser.getMotDePasse());
            } else {
                throw new SQLException("Utilisateur à mettre à jour non trouvé dans la base de données.");
            }
        }
        return utilisateurDAO.mettreAJourUtilisateur(utilisateur);
    }

    public boolean supprimerUtilisateur(int idUtilisateur) throws SQLException {
        return utilisateurDAO.supprimerUtilisateur(idUtilisateur);
    }

    public List<Utilisateur> getAllUtilisateurs() throws SQLException {
        return utilisateurDAO.getAllUtilisateurs();
    }

    public Utilisateur getUtilisateurById(int id) throws SQLException {
        return utilisateurDAO.getUtilisateurById(id);
    }

    // --- Méthodes pour la finalisation des ventes (délèguent à FactureDAO) ---
    public boolean finaliserVente(Facture facture) throws SQLException {
        return factureDAO.ajouterFacture(facture);
    }

    // Méthodes pour accéder aux factures via la classe Pharmacie
    public List<Facture> getAllFactures() throws SQLException {
        return factureDAO.getAllFactures();
    }

    public List<Facture> getFacturesByUtilisateur(Utilisateur utilisateur) throws SQLException {
        return factureDAO.getFacturesByUtilisateur(utilisateur);
    }

    public List<Facture> getFacturesByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return factureDAO.getFacturesByDateRange(startDate, endDate);
    }

    public Facture getFactureById(int id) throws SQLException {
        return factureDAO.getFactureById(id);
    }

    public List<AssuranceSocial> getAllAssurancesSocial() throws SQLException {
        return AssuranceSocialDAO.getAllAssurances();
    }

    public AssuranceSocial getAssuranceSocialById(int id) throws SQLException {
        return AssuranceSocialDAO.getAssuranceById(id);
    }

    public List<AssuranceSocial> getAllAssurancesSociales() throws SQLException {
        return AssuranceSocialDAO.getAllAssurances();
    }

    public AssuranceSocial getAssuranceSocialeById(int id) throws SQLException {
        return AssuranceSocialDAO.getAssuranceById(id);
    }

    public AssuranceSocial getAssuranceSocialByName(String name) throws SQLException {
        return AssuranceSocialDAO.getAssuranceByName(name);
    }

    public boolean ajouterAssuranceSocial(AssuranceSocial assurance) throws SQLException {
        return AssuranceSocialDAO.ajouterAssurance(assurance);
    }

    public boolean mettreAJourAssuranceSocial(AssuranceSocial assurance) throws SQLException {
        return AssuranceSocialDAO.mettreAJourAssurance(assurance);
    }

    public boolean supprimerAssuranceSocial(int id) throws SQLException {
        return AssuranceSocialDAO.supprimerAssurance(id);
    }

    // --- Méthodes pour la gestion des Comptes Comptables (délèguent à CompteComptableDAO) ---
    public List<CompteComptable> getAllComptesComptables() throws SQLException {
        return compteComptableDAO.getAllComptes();
    }

    public CompteComptable getCompteComptableById(int id) throws SQLException {
        return compteComptableDAO.getCompteById(id);
    }

    public CompteComptable getCompteComptableByNumero(String numeroCompte) throws SQLException {
        return compteComptableDAO.getCompteByNumero(numeroCompte);
    }

    public boolean ajouterCompteComptable(CompteComptable compte) throws SQLException {
        return compteComptableDAO.addCompte(compte);
    }

    public boolean mettreAJourCompteComptable(CompteComptable compte) throws SQLException {
        return compteComptableDAO.updateCompte(compte);
    }

    public boolean supprimerCompteComptable(int id) throws SQLException {
        return compteComptableDAO.deleteCompte(id);
    }

    // --- Méthodes pour la gestion des Transactions Comptables (délèguent à TransactionComptableDAO) ---
    public boolean ajouterTransactionComptable(TransactionComptable transaction) throws SQLException {
        return transactionComptableDAO.addTransaction(transaction);
    }
    
    public boolean ajouterTransactionComptable(Connection conn, TransactionComptable transaction) throws SQLException {
        return transactionComptableDAO.addTransaction(conn, transaction);
    }

    public List<TransactionComptable> getAllTransactionsComptables() throws SQLException {
        return transactionComptableDAO.getAllTransactions();
    }

    public List<TransactionComptable> getTransactionsComptablesByCompte(int idCompte) throws SQLException {
        return transactionComptableDAO.getTransactionsByCompte(idCompte);
    }

    public List<TransactionComptable> getTransactionsComptablesByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return transactionComptableDAO.getTransactionsByDateRange(startDate, endDate);
    }

    public List<TransactionComptable> getTransactionsComptablesBySourceType(String sourceType) throws SQLException {
        return transactionComptableDAO.getTransactionsBySourceType(sourceType);
    }

    // --- Méthodes de sauvegarde et chargement (pour les propriétés de la Pharmacie) ---
    public void sauvegarderDansFichier(String nomFichier) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(nomFichier))) {
            oos.writeObject(this);
            System.out.println("Informations de la pharmacie sauvegardées avec succès dans " + nomFichier);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des informations de la pharmacie: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Pharmacie chargerDepuisFichier(String nomFichier) {
        File file = new File(nomFichier);
        if (!file.exists()) {
            System.out.println("Fichier de sauvegarde non trouvé: " + nomFichier + ". Une nouvelle Pharmacie sera créée.");
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(nomFichier))) {
            Pharmacie pharmacie = (Pharmacie) ois.readObject();
            System.out.println("Informations de la pharmacie chargées avec succès depuis " + nomFichier);
            return pharmacie;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erreur lors du chargement des informations de la pharmacie: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
