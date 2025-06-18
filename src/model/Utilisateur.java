package model;

import java.io.Serializable;

public class Utilisateur implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nomUtilisateur;
    private String motDePasse;
    private String role;

    /**
     * Constructeur pour créer un NOUVEL utilisateur dans l'application.
     * L'ID est initialisé à 0 car il sera généré par la base de données lors de
     * l'insertion.
     */
    public Utilisateur(String nomUtilisateur, String motDePasse, String role) {
        this.nomUtilisateur = nomUtilisateur;
        this.motDePasse = motDePasse;
        this.role = role;
        this.id = 0;
    }

    /**
     * Constructeur pour créer un objet Utilisateur à partir de données existantes
     * récupérées de la base de données (l'ID est déjà connu).
     */
    public Utilisateur(int id, String nomUtilisateur, String motDePasse, String role) {
        this.id = id;
        this.nomUtilisateur = nomUtilisateur;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    // --- Getters: Pour accéder aux valeurs des attributs ---
    public int getId() {
        return id;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public String getRole() {
        return role;
    }

    // --- Setters: Pour modifier les valeurs des attributs ---
    // Le setId() est crucial pour mettre à jour l'objet avec l'ID généré par la BDD
    // après insertion.
    public void setId(int id) {
        this.id = id;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // --- Méthode Métier ---
    /**
     * Vérifie si le mot de passe fourni correspond à celui de l'utilisateur.
     * En production, cette comparaison DOIT se faire sur les hachages des mots de
     * passe.
     */
    public boolean verifierMotDePasse(String motDePasseSaisi) {

        return this.motDePasse.equals(motDePasseSaisi);

    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nomUtilisateur='" + nomUtilisateur + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}