package dao;

import model.Facture;
import model.Utilisateur;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface FactureDAO {
    boolean ajouterFacture(Facture facture) throws SQLException;

    Facture getFactureById(int id) throws SQLException;

    List<Facture> getAllFactures() throws SQLException;

    List<Facture> getFacturesByUtilisateur(Utilisateur utilisateur) throws SQLException;

    List<Facture> getFacturesByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException;

    boolean mettreAJourFacture(Facture facture) throws SQLException;

    boolean supprimerFacture(int id) throws SQLException;
}
