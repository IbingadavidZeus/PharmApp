package model;

import java.io.Serializable;
import java.util.Objects;

public class Fournisseur implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int id;
    protected String nomFournisseur;
    protected String contactPersonne;
    protected String telephone;
    protected String email;
    protected String adresse;

    // Constructeur complet pour la lecture depuis la BDD
    public Fournisseur(int id, String nomFournisseur, String contactPersonne, String telephone, String email, String adresse) {
        this.id = id;
        this.nomFournisseur = nomFournisseur;
        this.contactPersonne = contactPersonne;
        this.telephone = telephone;
        this.email = email;
        this.adresse = adresse;
    }

    // Constructeur pour la création d'un nouveau fournisseur (sans ID)
    public Fournisseur(String nomFournisseur, String contactPersonne, String telephone, String email, String adresse) {
        this(0, nomFournisseur, contactPersonne, telephone, email, adresse);
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNomFournisseur() {
        return nomFournisseur;
    }

    public String getContactPersonne() {
        return contactPersonne;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getEmail() {
        return email;
    }

    public String getAdresse() {
        return adresse;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setNomFournisseur(String nomFournisseur) {
        this.nomFournisseur = nomFournisseur;
    }

    public void setContactPersonne(String contactPersonne) {
        this.contactPersonne = contactPersonne;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    @Override
    public String toString() {
        return nomFournisseur;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fournisseur that = (Fournisseur) o;
        return id == that.id; // L'égalité est basée sur l'ID unique
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
