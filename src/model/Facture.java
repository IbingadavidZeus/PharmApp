package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Facture implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String numeroFacture;
    private LocalDateTime dateFacture;
    private double montantTotal; // Montant total TTC
    private double totalHt;      // NOUVEAU: Montant total HT
    private Utilisateur utilisateur;
    private List<LigneFacture> lignesFacture;

    // Taux de TVA (exemple: 18%)
    private static final double TAUX_TVA = 0.18; 

    // Constructeur pour créer une nouvelle facture
    public Facture(Utilisateur utilisateur) {
        this.dateFacture = LocalDateTime.now();
        this.montantTotal = 0.0;
        this.totalHt = 0.0; // Initialiser
        this.utilisateur = utilisateur;
        this.lignesFacture = new ArrayList<>();
        this.numeroFacture = null;
    }

    // NOUVEAU/MODIFIÉ: Constructeur pour charger une facture existante depuis la DB (avec totalHt)
    public Facture(int id, String numeroFacture, LocalDateTime dateFacture, double montantTotal, double totalHt, Utilisateur utilisateur) {
        this.id = id;
        this.numeroFacture = numeroFacture;
        this.dateFacture = dateFacture;
        this.montantTotal = montantTotal;
        this.totalHt = totalHt; // NOUVEAU: initialiser totalHt
        this.utilisateur = utilisateur;
        this.lignesFacture = new ArrayList<>();
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    public String getNumeroFacture() {
        return numeroFacture;
    }

    public LocalDateTime getDateFacture() {
        return dateFacture;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    // NOUVEAU: Getter pour totalHt
    public double getTotalHt() {
        return totalHt;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public List<LigneFacture> getLignesFacture() {
        return lignesFacture;
    }

    // --- Setters ---
    public void setId(int id) {
        this.id = id;
    }

    public void setNumeroFacture(String numeroFacture) {
        this.numeroFacture = numeroFacture;
    }

    public void setDateFacture(LocalDateTime dateFacture) {
        this.dateFacture = dateFacture;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
        this.totalHt = montantTotal / (1 + TAUX_TVA); 
    }

    
    public void setTotalHt(double totalHt) {
        this.totalHt = totalHt;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public void setLignesFacture(List<LigneFacture> lignesFacture) {
        this.lignesFacture = lignesFacture;
        calculerMontantTotal(); 
    }

    // --- Méthodes métier ---

    public void ajouterLigne(LigneFacture ligne) {
        if (ligne != null) {
            this.lignesFacture.add(ligne);
            calculerMontantTotal();
        }
    }

    // MODIFIÉ: Calcule montantTotal et totalHt
    public void calculerMontantTotal() {
        this.montantTotal = 0.0;
        for (LigneFacture ligne : lignesFacture) {
            this.montantTotal += ligne.getSousTotal();
        }
        this.totalHt = this.montantTotal / (1 + TAUX_TVA);
    }

    @Override
    public String toString() {
        return "Facture{" +
               "id=" + id +
               ", numeroFacture='" + numeroFacture + '\'' +
               ", dateFacture=" + dateFacture.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
               ", montantTotalTTC=" + String.format("%.2f", montantTotal) +
               ", totalHt=" + String.format("%.2f", totalHt) + // NOUVEAU: dans toString
               ", utilisateur=" + (utilisateur != null ? utilisateur.getNomUtilisateur() : "N/A") +
               ", nbLignes=" + lignesFacture.size() +
               '}';
    }
}
