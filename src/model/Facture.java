package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Facture implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String numeroFacture;
    private LocalDateTime dateFacture;
    private Utilisateur utilisateur; // L'utilisateur (vendeur) qui a créé la facture
    private List<LigneFacture> lignesFacture;
    private double totalHt;
    private double montantTotal; // Montant total TTC de la facture

    // NOUVEAU: Champs pour la gestion des assurances sociales
    protected AssuranceSocial assuranceSocial; // Assurance utilisée (peut être null si pas d'assurance)
    private double montantPrisEnChargeAssurance; // Montant pris en charge par l'assurance
    private double montantRestantAPayerClient; // Montant restant à payer par le client

    // Constructeur complet pour la lecture depuis la BDD
    public Facture(int id, String numeroFacture, LocalDateTime dateFacture, Utilisateur utilisateur, double totalHt, double montantTotal, AssuranceSocial assuranceSocial, double montantPrisEnChargeAssurance, double montantRestantAPayerClient) {
        this.id = id;
        this.numeroFacture = numeroFacture;
        this.dateFacture = dateFacture;
        this.utilisateur = utilisateur;
        this.lignesFacture = new ArrayList<>(); // Initialisé vide, sera rempli par le DAO
        this.totalHt = totalHt;
        this.montantTotal = montantTotal;
        this.assuranceSocial = assuranceSocial;
        this.montantPrisEnChargeAssurance = montantPrisEnChargeAssurance;
        this.montantRestantAPayerClient = montantRestantAPayerClient;
    }

    // Constructeur pour la création d'une nouvelle facture (ID sera auto-généré)
    // Permet de passer directement l'assurance et les montants calculés.
    public Facture(String numeroFacture, LocalDateTime dateFacture, Utilisateur utilisateur, List<LigneFacture> lignesFacture, double totalHt, double montantTotal, AssuranceSocial assuranceSocial, double montantPrisEnChargeAssurance, double montantRestantAPayerClient) {
        this(0, numeroFacture, dateFacture, utilisateur, totalHt, montantTotal, assuranceSocial, montantPrisEnChargeAssurance, montantRestantAPayerClient);
        this.lignesFacture = new ArrayList<>(lignesFacture); // Copie des lignes passées
    }

    // Ancien constructeur si toujours utilisé (sera mis à jour pour les nouveaux champs)
    public Facture(int id, String numeroFacture, LocalDateTime dateFacture, Utilisateur utilisateur, double totalHt, double montantTotal) {
        this(id, numeroFacture, dateFacture, utilisateur, totalHt, montantTotal, null, 0.0, montantTotal); // Par défaut sans assurance
    }


    // Getters
    public int getId() {
        return id;
    }

    public String getNumeroFacture() {
        return numeroFacture;
    }

    public LocalDateTime getDateFacture() {
        return dateFacture;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public List<LigneFacture> getLignesFacture() {
        return lignesFacture;
    }

    public double getTotalHt() {
        return totalHt;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    
    public AssuranceSocial getAssuranceSocial() {
        return assuranceSocial;
    }

    public double getMontantPrisEnChargeAssurance() {
        return montantPrisEnChargeAssurance;
    }

    public double getMontantRestantAPayerClient() {
        return montantRestantAPayerClient;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setNumeroFacture(String numeroFacture) {
        this.numeroFacture = numeroFacture;
    }

    public void setDateFacture(LocalDateTime dateFacture) {
        this.dateFacture = dateFacture;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public void setLignesFacture(List<LigneFacture> lignesFacture) {
        this.lignesFacture = new ArrayList<>(lignesFacture); // Utilise une copie
    }

    public void setTotalHt(double totalHt) {
        this.totalHt = totalHt;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    // NOUVEAU: Setters pour les informations d'assurance
    public void setAssuranceSocial(AssuranceSocial assuranceSociale) {
        this.assuranceSocial = assuranceSociale;
    }

    public void setMontantPrisEnChargeAssurance(double montantPrisEnChargeAssurance) {
        this.montantPrisEnChargeAssurance = montantPrisEnChargeAssurance;
    }

    public void setMontantRestantAPayerClient(double montantRestantAPayerClient) {
        this.montantRestantAPayerClient = montantRestantAPayerClient;
    }

    @Override
    public String toString() {
        String assuranceInfo = (assuranceSocial != null) ? ", Assurance: " + assuranceSocial.getNom_assurance() + " (Prise en charge: " + String.format("%.2f", montantPrisEnChargeAssurance) + " FCFA, Reste client: " + String.format("%.2f", montantRestantAPayerClient) + " FCFA)" : "";
        return "Facture n°" + numeroFacture +
               " du " + dateFacture.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
               " par " + utilisateur.getNomUtilisateur() +
               ", Total TTC: " + String.format("%.2f", montantTotal) + " FCFA" +
               assuranceInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Facture facture = (Facture) o;
        return id == facture.id && Objects.equals(numeroFacture, facture.numeroFacture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, numeroFacture);
    }
}
