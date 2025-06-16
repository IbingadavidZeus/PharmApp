package dao;

import model.Facture;
import model.Utilisateur;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface FactureDAO {
    // CORRECTED: Ensure method name is 'ajouterFacture' (not 'ajouterFature')
    boolean ajouterFacture(Facture facture) throws SQLException;

    // Récupère une facture par son ID
    Facture getFactureById(int id) throws SQLException;

    // Récupère toutes les factures
    List<Facture> getAllFactures() throws SQLException;

    // Récupère les factures par utilisateur
    List<Facture> getFacturesByUtilisateur(Utilisateur utilisateur) throws SQLException;

    // Récupère les factures dans une plage de dates
    List<Facture> getFacturesByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException;

    // Met à jour une facture existante (utile si on modifie le montant total, bien que souvent recalculé)
    boolean mettreAJourFacture(Facture facture) throws SQLException;

    // Supprime une facture par son ID
    boolean supprimerFacture(int id) throws SQLException;
}