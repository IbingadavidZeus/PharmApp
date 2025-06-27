package model;

import java.io.Serializable;

public class AssuranceSocial implements Serializable {
    protected static final long serialVersionUID = 1L;

    protected int id_assurance;
    protected String nom_assurance;
    protected double tauxDePriseEnCharge;

    public AssuranceSocial(int id_assurance, String nom_assurance, double tauxDePriseEnCharge) {
        this.id_assurance = id_assurance;
        this.nom_assurance = nom_assurance;
        this.tauxDePriseEnCharge = tauxDePriseEnCharge;
    }

    public int getId_assurance() {
        return id_assurance;
    }

    public void setId_assurance(int id_assurance) {
        this.id_assurance = id_assurance;
    }

    public String getNom_assurance() {
        return nom_assurance;
    }

    public void setNom_assurance(String nom_assurance) {
        this.nom_assurance = nom_assurance;
    }

    public double getTauxDePriseEnCharge() {
        return tauxDePriseEnCharge;
    }

    public void setTauxDePriseEnCharge(double tauxDePriseEnCharge) {
        this.tauxDePriseEnCharge = tauxDePriseEnCharge;
    }

    @Override
    public String toString() {
        return nom_assurance + " (ID: " + id_assurance + ", Taux: " + tauxDePriseEnCharge + "%)";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof AssuranceSocial))
            return false;
        AssuranceSocial other = (AssuranceSocial) obj;
        return id_assurance == other.id_assurance &&
                nom_assurance.equals(other.nom_assurance) &&
                Double.compare(other.tauxDePriseEnCharge, tauxDePriseEnCharge) == 0;
    }
    // @Override
    // public int hashCode() {
    // return Object.hash(id_assurance, nom_assurance, tauxDePriseEnCharge);}
}
