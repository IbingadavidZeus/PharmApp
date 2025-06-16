package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Facture implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private LocalDateTime dateFacture;
    private double montantTotal;
    private Utilisateur utilisateur; // L'utilisateur (vendeur) qui a créé la facture
    private List<LigneFacture> lignesFacture; // Les lignes de produits de cette facture

    // Constructeur pour créer une nouvelle facture (sans ID, l'ID sera généré par la DB)
    public Facture(Utilisateur utilisateur) {
        this.dateFacture = LocalDateTime.now(); // Date de la facture est la date actuelle
        this.montantTotal = 0.0; // Sera calculé après ajout des lignes
        this.utilisateur = utilisateur;
        this.lignesFacture = new ArrayList<>();
    }

    // NOUVEAU/CORRIGÉ: Constructeur pour charger une facture existante depuis la DB (avec ID)
    public Facture(int id, LocalDateTime dateFacture, double montantTotal, Utilisateur utilisateur) {
        this.id = id;
        this.dateFacture = dateFacture;
        this.montantTotal = montantTotal;
        this.utilisateur = utilisateur;
        this.lignesFacture = new ArrayList<>(); // Les lignes seront chargées séparément par le DAO
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    // CORRIGÉ/EXISTANT: getDateFacture
    public LocalDateTime getDateFacture() {
        return dateFacture;
    }

    // CORRIGÉ/EXISTANT: getMontantTotal
    public double getMontantTotal() {
        return montantTotal;
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

    public void setDateFacture(LocalDateTime dateFacture) {
        this.dateFacture = dateFacture;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    // NOUVEAU/CORRIGÉ: setLignesFacture
    public void setLignesFacture(List<LigneFacture> lignesFacture) {
        this.lignesFacture = lignesFacture;
        // Recalculer le montant total si les lignes sont réinitialisées
        calculerMontantTotal();
    }

    // --- Méthodes métier ---

    public void ajouterLigne(LigneFacture ligne) {
        if (ligne != null) {
            this.lignesFacture.add(ligne);
            calculerMontantTotal(); // Recalcule le total à chaque ajout de ligne
        }
    }

    public void calculerMontantTotal() {
        this.montantTotal = 0.0;
        for (LigneFacture ligne : lignesFacture) {
            this.montantTotal += ligne.getSousTotal();
        }
    }

    @Override
    public String toString() {
        return "Facture{" +
               "id=" + id +
               ", dateFacture=" + dateFacture.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
               ", montantTotal=" + String.format("%.2f", montantTotal) +
               ", utilisateur=" + (utilisateur != null ? utilisateur.getNomUtilisateur() : "N/A") +
               ", nbLignes=" + lignesFacture.size() +
               '}';
    }
}