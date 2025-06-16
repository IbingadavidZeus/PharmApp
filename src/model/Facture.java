package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // Importation pour le formatage dans toString
import java.util.ArrayList;
import java.util.List;

public class Facture implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String numeroFacture; // NOUVEAU: Numéro de facture personnalisé
    private LocalDateTime dateFacture;
    private double montantTotal;
    private Utilisateur utilisateur; // L'utilisateur (vendeur) qui a créé la facture
    private List<LigneFacture> lignesFacture; // Les lignes de produits de cette facture

    // Constructeur pour créer une nouvelle facture (sans ID ni numéro de facture initial, ils seront générés par la DB)
    public Facture(Utilisateur utilisateur) {
        this.dateFacture = LocalDateTime.now(); // Date de la facture est la date actuelle
        this.montantTotal = 0.0; // Sera calculé après ajout des lignes
        this.utilisateur = utilisateur;
        this.lignesFacture = new ArrayList<>();
        this.numeroFacture = null; // Sera défini après l'insertion en DB
    }

    // Constructeur pour charger une facture existante depuis la DB (avec ID et numéro de facture)
    public Facture(int id, String numeroFacture, LocalDateTime dateFacture, double montantTotal, Utilisateur utilisateur) {
        this.id = id;
        this.numeroFacture = numeroFacture; // NOUVEAU: Initialiser le numéro de facture
        this.dateFacture = dateFacture;
        this.montantTotal = montantTotal;
        this.utilisateur = utilisateur;
        this.lignesFacture = new ArrayList<>(); // Les lignes seront chargées séparément par le DAO
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    // NOUVEAU: Getter pour le numéro de facture
    public String getNumeroFacture() {
        return numeroFacture;
    }

    public LocalDateTime getDateFacture() {
        return dateFacture;
    }

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

    // NOUVEAU: Setter pour le numéro de facture
    public void setNumeroFacture(String numeroFacture) {
        this.numeroFacture = numeroFacture;
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
               ", numeroFacture='" + numeroFacture + '\'' + // NOUVEAU: dans toString
               ", dateFacture=" + dateFacture.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
               ", montantTotal=" + String.format("%.2f", montantTotal) +
               ", utilisateur=" + (utilisateur != null ? utilisateur.getNomUtilisateur() : "N/A") +
               ", nbLignes=" + lignesFacture.size() +
               '}';
    }
}
