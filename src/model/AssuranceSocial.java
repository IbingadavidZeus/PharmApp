package model;

import java.io.Serializable;
import java.util.Objects; // Ajout pour Objects.hash et Objects.equals

public class AssuranceSocial implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id_assurance;
    private String nom_assurance;
    private double tauxDePriseEnCharge; // Taux en décimal (ex: 0.70 pour 70%)

    // CONSTRUCTEUR PRINCIPAL: pour un objet déjà existant dans la BDD (avec ID)
    public AssuranceSocial(int id_assurance, String nom_assurance, double tauxDePriseEnCharge) {
        this.id_assurance = id_assurance;
        this.nom_assurance = nom_assurance;
        this.tauxDePriseEnCharge = tauxDePriseEnCharge;
    }

    // CONSTRUCTEUR POUR NOUVEL OBJET: pour une nouvelle assurance avant insertion en BDD (ID sera auto-généré)
    // C'est celui qui correspond à votre snippet, mais qui sera maintenant fonctionnel.
    public AssuranceSocial(String nom, double tauxDePriseEnCharge) {
        // Appelle le constructeur principal avec un ID de 0, qui sera mis à jour après l'insertion en BDD.
        this(0, nom, tauxDePriseEnCharge); 
    }

    // Getters
    public int getId_assurance() {
        return id_assurance;
    }

    public String getNom_assurance
    () {
        return nom_assurance;
    }

    public double getTauxDePriseEnCharge() {
        return tauxDePriseEnCharge;
    }

    // Setters (si nécessaire, pour des mises à jour par exemple)
    public void setId_assurance(int id) {
        this.id_assurance = id;
    }

    public void setNom_assurance(String nom) {
        this.nom_assurance = nom;
    }

    public void setTauxDePriseEnCharge(double tauxDePriseEnCharge) {
        this.tauxDePriseEnCharge = tauxDePriseEnCharge;
    }

    @Override
    public String toString() {
        // Formate le taux pour afficher un pourcentage plus lisible
        return nom_assurance + " (" + String.format("%.0f%%", tauxDePriseEnCharge * 100) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Correction: Vérifier le nom de la classe correctement
        if (o == null || getClass() != o.getClass()) return false; 
        AssuranceSocial that = (AssuranceSocial) o;
        return id_assurance == that.id_assurance; // L'égalité est basée sur l'ID unique
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_assurance);
    }
}
