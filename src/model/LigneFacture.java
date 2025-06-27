package model;

import java.io.Serializable;

public class LigneFacture implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id; // ID de la ligne de facture (auto-incrémenté par la BDD)
    private int idFacture; // ID de la facture à laquelle cette ligne appartient
    private Produit produit; // Le produit vendu
    private int quantite; // Quantité de ce produit vendue
    private double prixUnitaireHT; // Prix unitaire HT du produit au moment de la vente
    private double prixUnitaire; // Prix unitaire TTC du produit au moment de la vente (utilisé pour sous-total)
    private double sousTotal; 
    
    public LigneFacture(int id, int idFacture, Produit produit, int quantite, double prixUnitaireHT, double prixUnitaire, double sousTotal) {
        this.id = id;
        this.idFacture = idFacture;
        this.produit = produit;
        this.quantite = quantite;
        this.prixUnitaireHT = prixUnitaireHT;
        this.prixUnitaire = prixUnitaire; // Prix TTC
        this.sousTotal = sousTotal;
    }

    public LigneFacture(Produit produit, int quantite, double prixUnitaireHT, double prixUnitaire) {
        this(0, 0, produit, quantite, prixUnitaireHT, prixUnitaire, quantite * prixUnitaire);
    }
    
    public LigneFacture(Produit produit, int quantite) {
        this(0, 0, produit, quantite, produit.getPrixHt(), produit.calculerPrixTTC(), quantite * produit.calculerPrixTTC());
    }


    // Getters
    public int getId() {
        return id;
    }

    public int getIdFacture() {
        return idFacture;
    }

    public Produit getProduit() {
        return produit;
    }

    public int getQuantite() {
        return quantite;
    }

    public double getPrixUnitaireHT() {
        return prixUnitaireHT;
    }

    public double getPrixUnitaire() { // C'est le prix unitaire TTC de la ligne
        return prixUnitaire;
    }

    public double getSousTotal() {
        return sousTotal;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setIdFacture(int idFacture) {
        this.idFacture = idFacture;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
        // Recalculer le sous-total lorsque la quantité change
        this.sousTotal = this.quantite * this.prixUnitaire;
    }

    public void setPrixUnitaireHT(double prixUnitaireHT) {
        this.prixUnitaireHT = prixUnitaireHT;
    }

    public void setPrixUnitaire(double prixUnitaire) { // Setter pour le prix unitaire TTC
        this.prixUnitaire = prixUnitaire;
        this.sousTotal = this.quantite * this.prixUnitaire;
    }

    public void setSousTotal(double sousTotal) {
        this.sousTotal = sousTotal;
    }

    @Override
    public String toString() {
        return "LigneFacture [ID=" + id + ", FactureID=" + idFacture + ", Produit=" + produit.getNom() + 
               ", Quantité=" + quantite + ", PrixUHT=" + String.format("%.2f", prixUnitaireHT) +
               ", PrixUTTC=" + String.format("%.2f", prixUnitaire) + ", SousTotal=" + String.format("%.2f", sousTotal) + "]";
    }
}
