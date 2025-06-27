package model;

import java.io.Serializable;
import java.util.Objects;

public class CompteComptable implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int id_CompteComptable;
    protected String numeroCompte;
    protected String nomCompte;
    protected String typeCompte;
    protected String description;

    // Constructeur complet pour la lecture depuis la BDD
    public CompteComptable(int id_CompteComptable, String numeroCompte, String nomCompte, String typeCompte,
            String description) {
        this.id_CompteComptable = id_CompteComptable;
        this.numeroCompte = numeroCompte;
        this.nomCompte = nomCompte;
        this.typeCompte = typeCompte;
        this.description = description;
    }

    // Constructeur pour la création d'un nouveau compte (sans ID)
    public CompteComptable(String numeroCompte, String nomCompte, String typeCompte, String description) {
        this(0, numeroCompte, nomCompte, typeCompte, description);
    }

    // Getters
    public int getId_compteComptable() {
        return id_CompteComptable;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public String getNomCompte() {
        return nomCompte;
    }

    public String getTypeCompte() {
        return typeCompte;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setId_compteComptable(int id) {
        this.id_CompteComptable = id;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public void setNomCompte(String nomCompte) {
        this.nomCompte = nomCompte;
    }

    public void setTypeCompte(String typeCompte) {
        this.typeCompte = typeCompte;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return numeroCompte + " - " + nomCompte + " (" + typeCompte + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CompteComptable that = (CompteComptable) o;
        return id_CompteComptable == that.id_CompteComptable || Objects.equals(numeroCompte, that.numeroCompte);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_CompteComptable, numeroCompte);
    }
}
