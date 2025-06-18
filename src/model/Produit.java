package model;

import java.io.Serializable;

public abstract class Produit implements Serializable {
    private static final long serialVersionUID = 1L;

    protected static final double TAUX_TVA_STANDARD = 0.18;

    private int id;
    private String nom;
    private String reference;
    private String description;
    private double prixHt;
    private int quantite;

    // Constructeur pour un nouveau produit (sans ID, car il est généré par la BDD)
    public Produit(String nom, String reference, String description, double prixHt, int quantite) {
        this.nom = nom;
        this.reference = reference;
        this.description = description;
        this.prixHt = prixHt;
        this.quantite = quantite;
        this.id = 0;
    }

    // Constructeur pour un produit existant (avec ID, utilisé lors du chargement
    // depuis la BDD)

    public Produit(int id, String nom, String reference, String description, double prixHt, int quantite) {
        this(nom, reference, description, prixHt, quantite);
        this.id = id;
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public double getPrixHt() {
        return prixHt;
    }

    public int getQuantite() {
        return quantite;
    }

    // --- Setters ---
    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrixHt(double prixHt) {
        this.prixHt = prixHt;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    // Méthode abstraite pour calculer le Prix TTC
    // Les sous-classes devront implémenter cette méthode, potentiellement en
    // utilisant TAUX_TVA_STANDARD
    public abstract double calculerPrixTTC();

    @Override
    public String toString() {
        return "ID: " + id + ", Nom: " + nom + ", Ref: " + reference + ", Desc: " + description + ", Prix HT: "
                + String.format("%.2f", prixHt) + ", Qte: " + quantite;
    }

    // Méthode pour vérifier si deux produits sont égaux (basé sur la référence)
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Produit other = (Produit) obj;
        return this.reference.equals(other.reference);
    }

    @Override
    public int hashCode() {
        return reference.hashCode();
    }
}