package model;

import java.io.Serializable;

public class ProduitParaPharmacie extends Produit implements Serializable {
    private static final long serialVersionUID = 1L;

    private String categorie; // Ex: "Soin Visage", "Hygiène Corporelle", "Compléments Alimentaires"

    // Constructeur complet (avec ID, pour la lecture depuis la BDD)
    public ProduitParaPharmacie(int id, String nom, String reference, String description, double prixHt, int quantite, String categorie) {
        super(id, nom, reference, description, prixHt, quantite, "PARAPHARMACIE", false); // Les produits parapharmacie ne sont généralement PAS remboursables
        this.categorie = categorie;
    }

    // Constructeur pour la création d'un nouveau produit parapharmacie (sans ID)
    public ProduitParaPharmacie(String nom, String reference, String description, double prixHt, int quantite, String categorie) {
        super(nom, reference, description, prixHt, quantite, "PARAPHARMACIE", false); // Les produits parapharmacie ne sont généralement PAS remboursables
        this.categorie = categorie;
    }

    // Getter
    public String getCategorie() {
        return categorie;
    }

    // Setter
    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    @Override
    public String toString() {
        return "Parapharmacie: " + super.toString() + ", Catégorie: " + categorie;
    }
}
