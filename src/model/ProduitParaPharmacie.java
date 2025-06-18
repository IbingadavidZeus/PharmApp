package model;

import java.io.Serializable;

public class ProduitParaPharmacie extends Produit implements Serializable {
    private static final long serialVersionUID = 1L;

    private String categorie;

    // --- CONSTRUCTEURS ---

    // Constructeur pour un NOUVEAU ProduitParaPharmacie (sans ID)
    public ProduitParaPharmacie(String nom, String reference, String description, double prixHt, int quantite,
            String categorie) {
        super(nom, reference, description, prixHt, quantite);
        this.categorie = categorie;
    }

    // Constructeur pour un ProduitParaPharmacie EXISTANT (avec ID)
    public ProduitParaPharmacie(int id, String nom, String reference, String description, double prixHt, int quantite,
            String categorie) {
        super(id, nom, reference, description, prixHt, quantite);
        this.categorie = categorie;
    }

    // --- GETTERS ---
    public String getCategorie() {
        return categorie;
    }

    // --- SETTERS ---
    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    // --- Méthodes Spécifiques ---
    @Override
    public double calculerPrixTTC() {
        return getPrixHt() * (1 + TAUX_TVA_STANDARD);
    }

    @Override
    public String toString() {
        return super.toString() +
                ", Type: Parapharmacie" +
                ", Catégorie: " + categorie + // Affiche la catégorie
                ", Prix TTC: " + String.format("%.2f", calculerPrixTTC());
    }
}