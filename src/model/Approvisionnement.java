package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Approvisionnement implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int id;
    protected LocalDateTime dateApprovisionnement;
    protected Fournisseur fournisseur; // Le fournisseur de cet approvisionnement
    protected double montantTotalHt;
    protected double montantTotalTtc;
    protected double montantTva;
    protected String referenceBonCommande; 

    private List<LigneApprovisionnement> lignesApprovisionnement; // Les produits de cet approvisionnement

    // Taux de TVA (exemple: 18%)
    private static final double TAUX_TVA = 0.18;

    // Constructeur pour un approvisionnement existant dans la BDD
    public Approvisionnement(int id, LocalDateTime dateApprovisionnement, Fournisseur fournisseur,
                             double montantTotalHt, double montantTotalTtc, double montantTva,
                             String referenceBonCommande) {
        this.id = id;
        this.dateApprovisionnement = dateApprovisionnement;
        this.fournisseur = fournisseur;
        this.montantTotalHt = montantTotalHt;
        this.montantTotalTtc = montantTotalTtc;
        this.montantTva = montantTva;
        this.referenceBonCommande = referenceBonCommande;
        this.lignesApprovisionnement = new ArrayList<>(); // Sera rempli par le DAO
    }

    // Constructeur pour un nouvel approvisionnement (sans ID, sera généré par la DB)
    public Approvisionnement(Fournisseur fournisseur, String referenceBonCommande) {
        this(0, LocalDateTime.now(), fournisseur, 0.0, 0.0, 0.0, referenceBonCommande);
        this.lignesApprovisionnement = new ArrayList<>();
    }

    // Getters
    public int getId() {
        return id;
    }

    public LocalDateTime getDateApprovisionnement() {
        return dateApprovisionnement;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public double getMontantTotalHt() {
        return montantTotalHt;
    }

    public double getMontantTotalTtc() {
        return montantTotalTtc;
    }

    public double getMontantTva() {
        return montantTva;
    }

    public String getReferenceBonCommande() {
        return referenceBonCommande;
    }

    public List<LigneApprovisionnement> getLignesApprovisionnement() {
        return lignesApprovisionnement;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setDateApprovisionnement(LocalDateTime dateApprovisionnement) {
        this.dateApprovisionnement = dateApprovisionnement;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }

    public void setMontantTotalHt(double montantTotalHt) {
        this.montantTotalHt = montantTotalHt;
    }

    public void setMontantTotalTtc(double montantTotalTtc) {
        this.montantTotalTtc = montantTotalTtc;
    }

    public void setMontantTva(double montantTva) {
        this.montantTva = montantTva;
    }

    public void setReferenceBonCommande(String referenceBonCommande) {
        this.referenceBonCommande = referenceBonCommande;
    }

    public void setLignesApprovisionnement(List<LigneApprovisionnement> lignesApprovisionnement) {
        this.lignesApprovisionnement = lignesApprovisionnement;
        calculerTotaux(); // Recalcule les totaux après avoir défini les lignes
    }

    // Méthodes métier
    public void ajouterLigne(LigneApprovisionnement ligne) {
        if (ligne != null) {
            this.lignesApprovisionnement.add(ligne);
            calculerTotaux();
        }
    }

    public void calculerTotaux() {
        double totalHt = 0.0;
        for (LigneApprovisionnement ligne : lignesApprovisionnement) {
            totalHt += ligne.getQuantiteCommandee() * ligne.getPrixUnitaireAchatHt();
        }
        this.montantTotalHt = totalHt;
        this.montantTva = totalHt * TAUX_TVA;
        this.montantTotalTtc = totalHt + this.montantTva;
    }

    @Override
    public String toString() {
        return "Approvisionnement{" +
               "id=" + id +
               ", date=" + dateApprovisionnement.toLocalDate() +
               ", fournisseur=" + (fournisseur != null ? fournisseur.getNomFournisseur() : "N/A") +
               ", refBC='" + referenceBonCommande + '\'' +
               ", totalTTC=" + String.format("%.2f", montantTotalTtc) +
               ", nbLignes=" + lignesApprovisionnement.size() +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Approvisionnement that = (Approvisionnement) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
