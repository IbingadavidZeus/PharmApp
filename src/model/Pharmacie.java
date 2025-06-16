package model;

import dao.ProduitDAO;
import dao.UtilisateurDAO;
import dao.FactureDAO;
import dao.LigneFactureDAO;
import dao.impl.ProduitDAOImpl;
import dao.impl.UtilisateurDAOImpl;
import dao.impl.FactureDAOImpl;
import dao.impl.LigneFactureDAOImpl;

import java.io.*;
import java.sql.SQLException;
import java.util.List;

public class Pharmacie implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nom;
    private String adresse;

    // Dépendances DAO (ne pas sérialiser, seront instanciées au chargement)
    private transient ProduitDAO produitDAO;
    private transient UtilisateurDAO utilisateurDAO;
    private transient FactureDAO factureDAO;
    private transient LigneFactureDAO ligneFactureDAO;

    public Pharmacie(String nom, String adresse) {
        this.nom = nom;
        this.adresse = adresse;
        initDAOs(); // Initialise les DAOs lors de la création
    }

    // Méthode pour initialiser tous les DAOs
    private void initDAOs() {
        this.produitDAO = new ProduitDAOImpl();
        this.utilisateurDAO = new UtilisateurDAOImpl();
        // Initialiser les nouveaux DAOs en leur passant leurs dépendances
        // LigneFactureDAO a besoin de ProduitDAO pour charger les objets Produit associés aux lignes.
        this.ligneFactureDAO = new LigneFactureDAOImpl(this.produitDAO);
        // FactureDAO a besoin d'UtilisateurDAO (pour l'utilisateur qui fait la vente)
        // et de LigneFactureDAO (pour gérer les lignes de la facture).
        this.factureDAO = new FactureDAOImpl(this.utilisateurDAO, this.ligneFactureDAO);
    }

    // --- Gestion de la sérialisation pour les DAOs transitoires ---
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject(); // Lit les champs non-transitoires de Pharmacie
        initDAOs(); // Réinitialise les DAOs après la désérialisation
    }

    // --- Getters et Setters ---
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

    // --- Méthodes métier pour interagir avec les DAOs ---

    // -- Gestion des Produits --
    public boolean ajouterProduit(Produit produit) throws SQLException {
        return produitDAO.ajouterProduit(produit);
    }

    public List<Produit> getProduits() throws SQLException {
        return produitDAO.getAllProduits();
    }

    public Produit getProduitByReference(String reference) throws SQLException {
        return produitDAO.trouverParReference(reference);
    }

    // Il serait bon d'avoir une méthode pour récupérer un produit par son ID également,
    // car LigneFactureDAOImpl utilise findProduitById.
    public Produit getProduitById(int id) throws SQLException {
        return produitDAO.findProduitById(id);
    }

    public boolean mettreAJourQuantiteProduit(String reference, int nouvelleQuantite) throws SQLException {
        return produitDAO.mettreAJourQuantite(reference, nouvelleQuantite);
    }
    
    public boolean supprimerProduit(String reference) throws SQLException {
        return produitDAO.supprimerProduit(reference);
    }
    // Ajoutez d'autres méthodes pour le produit si nécessaire (modifier, rechercher, etc.)


    // -- Gestion des Utilisateurs --
    public Utilisateur authentifierUtilisateur(String nomUtilisateur, String motDePasse) throws SQLException {
        return utilisateurDAO.authentifierUtilisateur(nomUtilisateur, motDePasse);
    }
    
    public List<Utilisateur> getAllUtilisateurs() throws SQLException {
        return utilisateurDAO.getAllUtilisateurs();
    }
    // Ajoutez d'autres méthodes pour l'utilisateur si nécessaire

    // -- Gestion des Factures (Nouveau) --
    /**
     * Finalise une vente en créant une facture et ses lignes, puis met à jour le stock.
     * Cette opération est transactionnelle.
     * @param facture La facture à enregistrer (doit contenir ses lignes).
     * @return true si la vente est enregistrée avec succès.
     * @throws SQLException Si une erreur de base de données survient.
     */
    public boolean finaliserVente(Facture facture) throws SQLException {
        // La méthode ajouterFacture de FactureDAOImpl gère déjà l'ajout des lignes
        // et la mise à jour des quantités de produits en stock dans une transaction.
        return factureDAO.ajouterFacture(facture);
    }

    public List<Facture> getAllFactures() throws SQLException {
        return factureDAO.getAllFactures();
    }

    public Facture getFactureById(int id) throws SQLException {
        return factureDAO.getFactureById(id);
    }
    // Ajoutez d'autres méthodes pour FactureDAO si nécessaire, par exemple :
    // public List<Facture> getFacturesByUtilisateur(Utilisateur utilisateur) throws SQLException {
    //     return factureDAO.getFacturesByUtilisateur(utilisateur);
    // }


    // --- Méthodes de sérialisation (pour les informations de base de la pharmacie) ---
    // Ces méthodes sont principalement pour les informations statiques de la pharmacie (nom, adresse).
    // Les données des produits, utilisateurs, factures, etc., sont maintenant gérées par la BDD via les DAOs.
    public void sauvegarderDansFichier(String nomFichier) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(nomFichier))) {
            oos.writeObject(this);
            System.out.println("Données de la pharmacie sauvegardées avec succès dans " + nomFichier);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde de la pharmacie: " + e.getMessage());
        }
    }

    public static Pharmacie chargerDepuisFichier(String nomFichier) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(nomFichier))) {
            Pharmacie pharmacie = (Pharmacie) ois.readObject();
            System.out.println("Données de la pharmacie chargées depuis " + nomFichier);
            return pharmacie;
        } catch (FileNotFoundException e) {
            System.out.println("Fichier de sauvegarde non trouvé. Création d'une nouvelle pharmacie.");
            return null; // Retourne null si le fichier n'existe pas, indiquant de créer une nouvelle instance
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erreur lors du chargement de la pharmacie: " + e.getMessage());
            return null;
        }
    }

    public List<Produit> rechercherProduits(String critere) {
       
        throw new UnsupportedOperationException("Unimplemented method 'rechercherProduits'");
    }
}