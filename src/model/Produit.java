package model;

import java.io.Serializable;
import java.util.Objects;

// Cette classe est la base pour Medicament et ProduitParaPharmacie
public class Produit implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int id;
    protected String nom;
    protected String reference; 
    protected String description;
    protected double prixHt;
    protected int quantite;
    protected String typeProduit; 
    protected boolean estRemboursable; // NOUVEAU: Indique si le produit est remboursable par les assurances

    // Constructeur complet (pour la lecture depuis la BDD)
    public Produit(int id, String nom, String reference, String description, double prixHt, int quantite, String typeProduit, boolean estRemboursable) {
        this.id = id;
        this.nom = nom;
        this.reference = reference;
        this.description = description;
        this.prixHt = prixHt;
        this.quantite = quantite;
        this.typeProduit = typeProduit;
        this.estRemboursable = estRemboursable; // Initialisation du nouveau champ
    }

    // Constructeur sans ID (pour la création d'un nouveau produit avant insertion en BDD)
    public Produit(String nom, String reference, String description, double prixHt, int quantite, String typeProduit, boolean estRemboursable) {
        this(0, nom, reference, description, prixHt, quantite, typeProduit, estRemboursable);
    }

    // Constructeur de base (si nécessaire pour des types génériques, mais Medicament/ParaPharmacie devraient être utilisés)
    // ATTENTION: ce constructeur ne gère PAS estRemboursable, il sera donc FALSE par défaut
    public Produit(int id, String nom, String reference, String description, double prixHt, int quantite, String typeProduit) {
        this(id, nom, reference, description, prixHt, quantite, typeProduit, false); // Par défaut non remboursable
    }


    // Getters
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

    public String getTypeProduit() {
        return typeProduit;
    }

    public boolean isEstRemboursable() { 
        return estRemboursable;
    }

    // Setters
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
        if (prixHt < 0) {
            throw new IllegalArgumentException("Le prix HT ne peut pas être négatif.");
        }
        this.prixHt = prixHt;
    }

    public void setQuantite(int quantite) {
        if (quantite < 0) {
            throw new IllegalArgumentException("La quantité ne peut pas être négative.");
        }
        this.quantite = quantite;
    }

    public void setTypeProduit(String typeProduit) {
        this.typeProduit = typeProduit;
    }

    public void setEstRemboursable(boolean estRemboursable) {
        this.estRemboursable = estRemboursable;
    }

    // Calcul du prix TTC (peut être surchargé si des TVA différentes sont appliquées)
    public double calculerPrixTTC() {
        double tauxTVA = 0.18; 
        return prixHt * (1 + tauxTVA);
    }

    @Override
    public String toString() {
        return nom + " (Ref: " + reference + ", Stock: " + quantite + ", Prix TTC: " + String.format("%.2f", calculerPrixTTC()) + " FCFA)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produit produit = (Produit) o;
        return id == produit.id && Objects.equals(reference, produit.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reference);
    }
}
