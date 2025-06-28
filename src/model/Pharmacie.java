package model;

import dao.FactureDAO;
import dao.LigneFactureDAO;
import dao.ProduitDAO;
import dao.UtilisateurDAO;
import dao.AssuranceSocialDAO;
import dao.CompteComptableDAO; 
import dao.TransactionComptableDAO;
import dao.FournisseurDAO;
import dao.ApprovisionnementDAO;
import dao.LigneApprovisionnementDAO;

import dao.impl.FactureDAOImpl;
import dao.impl.LigneFactureDAOImpl;
import dao.impl.ProduitDAOImpl;
import dao.impl.UtilisateurDAOImpl;
import dao.impl.AssuranceSocialDAOImpl;
import dao.impl.CompteComptableDAOImpl; 
import dao.impl.TransactionComptableDAOImpl;
import dao.impl.FournisseurDAOImpl;
import dao.impl.ApprovisionnementDAOImpl;
import dao.impl.LigneApprovisionnementDAOImpl;

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

    protected transient ProduitDAO produitDAO;
    protected transient UtilisateurDAO utilisateurDAO;
    protected transient FactureDAO factureDAO;
    protected transient LigneFactureDAO ligneFactureDAO;
    protected transient AssuranceSocialDAO AssuranceSocialDAO;
    protected transient CompteComptableDAO compteComptableDAO; 
    protected transient TransactionComptableDAO transactionComptableDAO;
    protected transient FournisseurDAO fournisseurDAO; // NOUVEAU
    protected transient ApprovisionnementDAO approvisionnementDAO; // NOUVEAU
    protected transient LigneApprovisionnementDAO ligneApprovisionnementDAO; // NOUVEAU


    // Comptes comptables spécifiques aux approvisionnements (pour optimisation)
    private transient CompteComptable compteAchatsMarchandises;
    private transient CompteComptable compteTVADeductible;
    private transient CompteComptable compteCaissePourAchats; 
    private transient CompteComptable compteFournisseurs;
    private transient CompteComptable compteBanque; // NOUVEAU: Pour les transactions bancaires

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
        this.fournisseurDAO = new FournisseurDAOImpl();
        this.ligneApprovisionnementDAO = new LigneApprovisionnementDAOImpl(produitDAO); 
        this.approvisionnementDAO = new ApprovisionnementDAOImpl(fournisseurDAO, ligneApprovisionnementDAO, produitDAO, compteComptableDAO, transactionComptableDAO); 
        
        
        this.factureDAO = new FactureDAOImpl(utilisateurDAO, ligneFactureDAO, produitDAO, AssuranceSocialDAO,
                                             compteComptableDAO, transactionComptableDAO); 
    }
    
    // NOUVEAU: Méthode pour initialiser les objets CompteComptable nécessaires aux transactions
    private void initAccountingAccounts() throws SQLException {
        // Ces numéros de compte doivent correspondre à ceux de votre BDD
        compteAchatsMarchandises = compteComptableDAO.getCompteByNumero("607");
        compteTVADeductible = compteComptableDAO.getCompteByNumero("4456");
        compteCaissePourAchats = compteComptableDAO.getCompteByNumero("530"); // Assumons le paiement en caisse
        compteFournisseurs = compteComptableDAO.getCompteByNumero("401");
        compteBanque = compteComptableDAO.getCompteByNumero("512");

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
    public List<Fournisseur> getAllFournisseurs() throws SQLException {
        return fournisseurDAO.getAllFournisseurs();
    }

    public Fournisseur getFournisseurById(int id) throws SQLException {
        return fournisseurDAO.getFournisseurById(id);
    }

    public Fournisseur getFournisseurByNom(String nom) throws SQLException {
        return fournisseurDAO.getFournisseurByNom(nom);
    }

    public boolean ajouterFournisseur(Fournisseur fournisseur) throws SQLException {
        return fournisseurDAO.addFournisseur(fournisseur);
    }

    public boolean mettreAJourFournisseur(Fournisseur fournisseur) throws SQLException {
        return fournisseurDAO.updateFournisseur(fournisseur);
    }
    public Approvisionnement getApprovisionnementByReference(String referenceBonCommande) throws SQLException {
        return approvisionnementDAO.getApprovisionnementByReference(referenceBonCommande);
    }
    public boolean supprimerFournisseur(int id) throws SQLException {
        return fournisseurDAO.deleteFournisseur(id);
    }

    // NOUVEAU: Méthodes pour la gestion des Approvisionnements (délèguent à ApprovisionnementDAO) ---
    public boolean ajouterApprovisionnement(Approvisionnement approvisionnement) throws SQLException {
        return approvisionnementDAO.addApprovisionnement(approvisionnement);
    }

    public List<Approvisionnement> getAllApprovisionnements() throws SQLException {
        return approvisionnementDAO.getAllApprovisionnements();
    }

    public Approvisionnement getApprovisionnementById(int id) throws SQLException {
        return approvisionnementDAO.getApprovisionnementById(id);
    }

    public List<Approvisionnement> getApprovisionnementsByFournisseur(int idFournisseur) throws SQLException {
        return approvisionnementDAO.getApprovisionnementsByFournisseur(idFournisseur);
    }

    public List<Approvisionnement> getApprovisionnementsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return approvisionnementDAO.getApprovisionnementsByDateRange(startDate, endDate);
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
