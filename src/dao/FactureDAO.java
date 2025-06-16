package dao;

import model.Facture;
import model.Utilisateur;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface FactureDAO {
    boolean ajouterFacture(Facture facture) throws SQLException;

    // Mise à jour de la signature pour inclure le numéro de facture
    Facture getFactureById(int id) throws SQLException;

    // Mise à jour de la signature pour inclure le numéro de facture
    List<Facture> getAllFactures() throws SQLException;

    // Mise à jour de la signature pour inclure le numéro de facture
    List<Facture> getFacturesByUtilisateur(Utilisateur utilisateur) throws SQLException;

    // Mise à jour de la signature pour inclure le numéro de facture
    List<Facture> getFacturesByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException;

    // Mise à jour de la signature pour inclure le numéro de facture
    boolean mettreAJourFacture(Facture facture) throws SQLException;

    boolean supprimerFacture(int id) throws SQLException;
}
