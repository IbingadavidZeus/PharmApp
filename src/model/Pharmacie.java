package model;

import dao.FactureDAO;
import dao.LigneFactureDAO;
import dao.ProduitDAO;
import dao.UtilisateurDAO;
import dao.impl.FactureDAOImpl;
import dao.impl.LigneFactureDAOImpl;
import dao.impl.ProduitDAOImpl;
import dao.impl.UtilisateurDAOImpl;
import java.io.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class Pharmacie implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nom;
    private String adresse;

    // Dépendances DAO (non sérialisables, initialisées au démarrage ou par injection)
    private transient ProduitDAO produitDAO;
    private transient UtilisateurDAO utilisateurDAO;
    private transient FactureDAO factureDAO;
    private transient LigneFactureDAO ligneFactureDAO;


    public Pharmacie(String nom, String adresse) {
        this.nom = nom;
        this.adresse = adresse;
        initDAOs();
    }

    // Méthode pour initialiser les DAOs
    private void initDAOs() {
        this.produitDAO = new ProduitDAOImpl();
        this.utilisateurDAO = new UtilisateurDAOImpl();
        // LigneFactureDAO a besoin de ProduitDAO pour charger les produits associés aux lignes
        this.ligneFactureDAO = new LigneFactureDAOImpl(produitDAO);
        // FactureDAO a besoin d'UtilisateurDAO et LigneFactureDAO pour charger les factures complètes
        this.factureDAO = new FactureDAOImpl(utilisateurDAO, ligneFactureDAO);
    }

    // Gère la désérialisation: réinitialise les transients DAOs
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject(); // Désérialise les champs non-transients
        initDAOs(); // Réinitialise les champs transients (DAOs) après la désérialisation
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

    public List<Produit> getProduits() throws SQLException {
        return produitDAO.getAllProduits();
    }
    
    public List<Produit> rechercherProduits(String critere) throws SQLException {
        return produitDAO.rechercherProduits(critere);
    }

    public boolean mettreAJourQuantiteProduit(String reference, int nouvelleQuantite) throws SQLException {
        return produitDAO.mettreAJourQuantite(reference, nouvelleQuantite);
    }

    // --- Méthodes pour l'authentification et la gestion des utilisateurs (délèguent à UtilisateurDAO) ---

    // AVERTISSEMENT DE SÉCURITÉ: Cette méthode ne hache plus les mots de passe.
    // Les mots de passe seront traités en TEXTE CLAIR. CE N'EST PAS SÉCURISÉ.
    public Utilisateur authentifier(String nomUtilisateur, String motDePasse) {
        try {
            // Passe le mot de passe en texte clair directement au DAO
            return utilisateurDAO.authentifierUtilisateur(nomUtilisateur, motDePasse);
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'authentification: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // AVERTISSEMENT DE SÉCURITÉ: Cette méthode ne hache plus les mots de passe.
    // Les mots de passe seront stockés en TEXTE CLAIR. CE N'EST PAS SÉCURISÉ.
    public boolean ajouterUtilisateur(Utilisateur utilisateur, String plainPassword) throws SQLException {
        utilisateur.setMotDePasse(plainPassword); // Définit le mot de passe en texte clair
        return utilisateurDAO.ajouterUtilisateur(utilisateur);
    }

    // AVERTISSEMENT DE SÉCURITÉ: Cette méthode ne hache plus les mots de passe.
    // Les mots de passe seront traités en TEXTE CLAIR. CE N'EST PAS SÉCURISÉ.
    public boolean mettreAJourUtilisateur(Utilisateur utilisateur, String newPlainPassword) throws SQLException {
        if (newPlainPassword != null && !newPlainPassword.trim().isEmpty()) {
            utilisateur.setMotDePasse(newPlainPassword); // Définit le nouveau mot de passe en texte clair
        } else {
            // Si le champ de mot de passe est vide, on doit récupérer l'ancien mot de passe EN CLAIR
            // depuis la base de données.
            // Si la base de données contenait des mots de passe hachés avant cette modification,
            // cette logique ne fonctionnera pas correctement sans réinitialiser ces mots de passe en clair.
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

    // NOUVEAU: Méthodes pour accéder aux factures via la classe Pharmacie
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
