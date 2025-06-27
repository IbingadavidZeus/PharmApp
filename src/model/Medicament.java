package model;

import java.io.Serializable;

public class Medicament extends Produit implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean generique;
    private boolean surOrdonnance;

    // Constructeur complet (avec ID, pour la lecture depuis la BDD)
    public Medicament(int id, String nom, String reference, String description, double prixHt, int quantite, boolean generique, boolean surOrdonnance) {
        super(id, nom, reference, description, prixHt, quantite, "MEDICAMENT", true); // Les médicaments sont généralement remboursables
        this.generique = generique;
        this.surOrdonnance = surOrdonnance;
    }

    // Constructeur pour la création d'un nouveau médicament (sans ID)
    public Medicament(String nom, String reference, String description, double prixHt, int quantite, boolean generique, boolean surOrdonnance) {
        super(nom, reference, description, prixHt, quantite, "MEDICAMENT", true); // Les médicaments sont généralement remboursables
        this.generique = generique;
        this.surOrdonnance = surOrdonnance;
    }

    // Getters
    public boolean isGenerique() {
        return generique;
    }

    public boolean isSurOrdonnance() {
        return surOrdonnance;
    }

    // Setters
    public void setGenerique(boolean generique) {
        this.generique = generique;
    }

    public void setSurOrdonnance(boolean surOrdonnance) {
        this.surOrdonnance = surOrdonnance;
    }

    @Override
    public String toString() {
        return "Médicament: " + super.toString() +
               ", Générique: " + (generique ? "Oui" : "Non") +
               ", Ordonnance: " + (surOrdonnance ? "Oui" : "Non");
    }
}
