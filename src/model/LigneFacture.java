package model;

import java.io.Serializable;

public class LigneFacture implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int idFacture; // L'ID de la facture à laquelle cette ligne appartient
    private Produit produit; // Le produit vendu
    private int quantite;
    private double prixUnitaire; // Prix du produit au moment de la vente (peut être différent du prix actuel du produit)
    private double sousTotal;

    // Constructeur pour créer une nouvelle ligne de facture (sans ID, l'ID sera généré par la DB)
    public LigneFacture(Produit produit, int quantite) {
        this.produit = produit;
        this.quantite = quantite;
        this.prixUnitaire = produit.calculerPrixTTC(); // Prix TTC du produit au moment de l'ajout
        this.sousTotal = this.prixUnitaire * this.quantite;
    }

    // Constructeur pour charger une ligne de facture existante depuis la DB (avec ID et idFacture)
    public LigneFacture(int id, int idFacture, Produit produit, int quantite, double prixUnitaire, double sousTotal) {
        this.id = id;
        this.idFacture = idFacture;
        this.produit = produit;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.sousTotal = sousTotal;
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    public int getIdFacture() {
        return idFacture;
    }

    // CORRIGÉ/EXISTANT: getProduit
    public Produit getProduit() {
        return produit;
    }

    // CORRIGÉ/EXISTANT: getQuantite
    public int getQuantite() {
        return quantite;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public double getSousTotal() {
        return sousTotal;
    }

    // --- Setters ---
    public void setId(int id) {
        this.id = id;
    }

    public void setIdFacture(int idFacture) {
        this.idFacture = idFacture;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        // Recalculer le prix unitaire et sous-total si le produit est changé (moins courant)
        this.prixUnitaire = produit.calculerPrixTTC();
        this.sousTotal = this.prixUnitaire * this.quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
        this.sousTotal = this.prixUnitaire * this.quantite; // Recalcule le sous-total
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
        this.sousTotal = this.prixUnitaire * this.quantite; // Recalcule le sous-total
    }

    public void setSousTotal(double sousTotal) {
        this.sousTotal = sousTotal;
    }

    @Override
    public String toString() {
        return "LigneFacture{" +
               "id=" + id +
               ", idFacture=" + idFacture +
               ", produit=" + (produit != null ? produit.getNom() : "N/A") +
               ", quantite=" + quantite +
               ", prixUnitaire=" + String.format("%.2f", prixUnitaire) +
               ", sousTotal=" + String.format("%.2f", sousTotal) +
               '}';
    }
}