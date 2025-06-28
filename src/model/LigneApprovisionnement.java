package model;

import java.io.Serializable;
import java.util.Objects;

public class LigneApprovisionnement implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int id;
    protected int idApprovisionnement; // Clé étrangère vers Approvisionnement
    protected Produit produit; // Le produit concerné par cette ligne
    protected int quantiteCommandee;
    protected double prixUnitaireAchatHt; // Prix HT au moment de l'achat

    // Constructeur complet pour la lecture depuis la BDD
    public LigneApprovisionnement(int id, int idApprovisionnement, Produit produit, int quantiteCommandee, double prixUnitaireAchatHt) {
        this.id = id;
        this.idApprovisionnement = idApprovisionnement;
        this.produit = produit;
        this.quantiteCommandee = quantiteCommandee;
        this.prixUnitaireAchatHt = prixUnitaireAchatHt;
    }

    // Constructeur pour une nouvelle ligne (sans ID)
    public LigneApprovisionnement(int idApprovisionnement, Produit produit, int quantiteCommandee, double prixUnitaireAchatHt) {
        this(0, idApprovisionnement, produit, quantiteCommandee, prixUnitaireAchatHt);
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getIdApprovisionnement() {
        return idApprovisionnement;
    }

    public Produit getProduit() {
        return produit;
    }

    public int getQuantiteCommandee() {
        return quantiteCommandee;
    }

    public double getPrixUnitaireAchatHt() {
        return prixUnitaireAchatHt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setIdApprovisionnement(int idApprovisionnement) {
        this.idApprovisionnement = idApprovisionnement;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public void setQuantiteCommandee(int quantiteCommandee) {
        this.quantiteCommandee = quantiteCommandee;
    }

    public void setPrixUnitaireAchatHt(double prixUnitaireAchatHt) {
        this.prixUnitaireAchatHt = prixUnitaireAchatHt;
    }

    // Méthode pour calculer le sous-total HT de la ligne
    public double getSousTotalHt() {
        return quantiteCommandee * prixUnitaireAchatHt;
    }

    @Override
    public String toString() {
        return "LigneApprovisionnement{" +
               "id=" + id +
               ", idAppro=" + idApprovisionnement +
               ", produit=" + (produit != null ? produit.getNom() : "N/A") +
               ", qte=" + quantiteCommandee +
               ", prixAchat=" + String.format("%.2f", prixUnitaireAchatHt) +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LigneApprovisionnement that = (LigneApprovisionnement) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
