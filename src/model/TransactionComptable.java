package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class TransactionComptable implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int id_transaction;
    protected LocalDateTime dateTransaction;
    protected String referencePiece;
    protected String descriptionTransaction;
    protected double montant;
    protected CompteComptable compteDebit;
    protected CompteComptable compteCredit;
    protected String sourceType;
    protected int sourceId;

    // Constructeur complet pour la lecture depuis la BDD
    public TransactionComptable(int id_transaction, LocalDateTime dateTransaction, String referencePiece,
            String descriptionTransaction, double montant,
            CompteComptable compteDebit, CompteComptable compteCredit,
            String sourceType, int sourceId) {
        this.id_transaction = id_transaction;
        this.dateTransaction = dateTransaction;
        this.referencePiece = referencePiece;
        this.descriptionTransaction = descriptionTransaction;
        this.montant = montant;
        this.compteDebit = compteDebit;
        this.compteCredit = compteCredit;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
    }

    // Constructeur pour la création d'une nouvelle transaction (sans ID)
    public TransactionComptable(LocalDateTime dateTransaction, String referencePiece,
            String descriptionTransaction, double montant,
            CompteComptable compteDebit, CompteComptable compteCredit,
            String sourceType, int sourceId) {
        this(0, dateTransaction, referencePiece, descriptionTransaction, montant,
                compteDebit, compteCredit, sourceType, sourceId);
    }

    // Constructeur simplifié pour les cas où la source n'est pas directement un ID
    // de table
    public TransactionComptable(LocalDateTime dateTransaction, String referencePiece,
            String descriptionTransaction, double montant,
            CompteComptable compteDebit, CompteComptable compteCredit,
            String sourceType) {
        this(0, dateTransaction, referencePiece, descriptionTransaction, montant,
                compteDebit, compteCredit, sourceType, 0); // sourceId à 0 ou null
    }

    // Getters
    public int getId_transaction() {
        return id_transaction;
    }

    public LocalDateTime getDateTransaction() {
        return dateTransaction;
    }

    public String getReferencePiece() {
        return referencePiece;
    }

    public String getDescriptionTransaction() {
        return descriptionTransaction;
    }

    public double getMontant() {
        return montant;
    }

    public CompteComptable getCompteDebit() {
        return compteDebit;
    }

    public CompteComptable getCompteCredit() {
        return compteCredit;
    }

    public String getSourceType() {
        return sourceType;
    }

    public int getSourceId() {
        return sourceId;
    }

    // Setters
    public void setId_transaction(int id) {
        this.id_transaction = id;
    }

    public void setDateTransaction(LocalDateTime dateTransaction) {
        this.dateTransaction = dateTransaction;
    }

    public void setReferencePiece(String referencePiece) {
        this.referencePiece = referencePiece;
    }

    public void setDescriptionTransaction(String descriptionTransaction) {
        this.descriptionTransaction = descriptionTransaction;
    }

    public void setMontant(double montant) {
        if (montant < 0) {
            throw new IllegalArgumentException("Le montant de la transaction ne peut pas être négatif.");
        }
        this.montant = montant;
    }

    public void setCompteDebit(CompteComptable compteDebit) {
        this.compteDebit = compteDebit;
    }

    public void setCompteCredit(CompteComptable compteCredit) {
        this.compteCredit = compteCredit;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    @Override
    public String toString() {
        return "TransactionComptable [ID=" + id_transaction + ", Date=" + dateTransaction.toLocalDate() +
                ", Ref='" + referencePiece + "', Desc='" + descriptionTransaction +
                "', Montant=" + String.format("%.2f", montant) +
                ", Débit=" + (compteDebit != null ? compteDebit.getNumeroCompte() : "N/A") +
                ", Crédit=" + (compteCredit != null ? compteCredit.getNumeroCompte() : "N/A") +
                ", Type Source=" + sourceType + ", ID Source=" + sourceId + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TransactionComptable that = (TransactionComptable) o;
        return id_transaction == that.id_transaction; // L'égalité est basée sur l'ID unique
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_transaction);
    }
}
