package model;

import java.io.Serializable;

public class ProduitParaPharmacie extends Produit implements Serializable {
    private static final long serialVersionUID = 1L;

    private String categorie; // Renommé 'type' en 'categorie' pour correspondre à la BDD

    // --- CONSTRUCTEURS ---

    // Constructeur pour un NOUVEAU ProduitParaPharmacie (sans ID)
    public ProduitParaPharmacie(String nom, String reference, String description, double prixHt, int quantite, String categorie) {
        // Appelle le constructeur de la classe parente Produit
        super(nom, reference, description, prixHt, quantite);
        this.categorie = categorie; // Initialisation de la catégorie
    }

    // Constructeur pour un ProduitParaPharmacie EXISTANT (avec ID)
    public ProduitParaPharmacie(int id, String nom, String reference, String description, double prixHt, int quantite, String categorie) {
        // Appelle le constructeur de la classe parente Produit qui inclut l'ID
        super(id, nom, reference, description, prixHt, quantite);
        this.categorie = categorie; // Initialisation de la catégorie
    }

    // --- GETTERS ---
    public String getCategorie() { // Renommé de getType() à getCategorie()
        return categorie;
    }

    // --- SETTERS ---
    public void setCategorie(String categorie) { // Renommé de setType() à setCategorie()
        this.categorie = categorie;
    }

    // --- Méthodes Spécifiques ---

    @Override
    public double calculerPrixTTC() {
        // Utilise le taux de TVA standard défini dans la classe Produit (par exemple 20% si c'est le cas)
        // ou vous pouvez définir un taux spécifique ici si nécessaire.
        // Ici, nous utilisons la constante TAUX_TVA_STANDARD de la classe Produit.
        return getPrixHt() * (1 + TAUX_TVA_STANDARD);
    }

    @Override
    public String toString() {
        // Appelle la méthode toString() de la classe parente pour les informations de base du produit
        return super.toString() +
               ", Type: Parapharmacie" +
               ", Catégorie: " + categorie + // Affiche la catégorie
               ", Prix TTC: " + String.format("%.2f", calculerPrixTTC());
    }
}