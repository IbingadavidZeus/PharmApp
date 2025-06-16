package model;

import java.io.Serializable;

public class Medicament extends Produit implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean generique;
    private boolean surOrdonnance;

    // --- CONSTRUCTEURS ---

    // Constructeur pour un NOUVEAU Medicament (sans ID, généré par la BDD)
    public Medicament(String nom, String reference, String description, double prixHt, int quantite, boolean generique, boolean surOrdonnance) {
        // Appelle le constructeur de la classe parente Produit
        super(nom, reference, description, prixHt, quantite);
        this.generique = generique;
        this.surOrdonnance = surOrdonnance;
    }

    // Constructeur pour un Medicament EXISTANT (avec ID, chargé depuis la BDD)
    public Medicament(int id, String nom, String reference, String description, double prixHt, int quantite, boolean generique, boolean surOrdonnance) {
        // Appelle le constructeur de la classe parente Produit qui inclut l'ID
        super(id, nom, reference, description, prixHt, quantite);
        this.generique = generique;
        this.surOrdonnance = surOrdonnance;
    }

    // --- GETTERS ---
    public boolean isGenerique() {
        return generique;
    }

    public boolean isSurOrdonnance() {
        return surOrdonnance;
    }

    // --- SETTERS ---
    public void setGenerique(boolean generique) {
        this.generique = generique;
    }

    public void setSurOrdonnance(boolean surOrdonnance) {
        this.surOrdonnance = surOrdonnance;
    }

    // --- Méthodes Spécifiques ---

    @Override
    public double calculerPrixTTC() {
        // Supposons une TVA spécifique pour les médicaments, par exemple 5.5%
        // Nous pouvons utiliser la constante de la classe Produit si le taux était standard pour TOUS les produits
        // ou définir une constante ici si le taux est spécifique aux médicaments.
        // Pour l'instant, on utilise 5.5% comme vous l'avez spécifié.
        double tauxTvaMedicament = 0.055; // 5.5%
        return getPrixHt() * (1 + tauxTvaMedicament);
    }

    @Override
    public String toString() {
        // Appelle la méthode toString() de la classe parente pour les informations de base du produit
        return super.toString() +
               ", Type: Médicament" +
               ", Générique: " + (generique ? "Oui" : "Non") +
               ", Ordonnance: " + (surOrdonnance ? "Oui" : "Non") +
               ", Prix TTC: " + String.format("%.2f", calculerPrixTTC());
    }
}