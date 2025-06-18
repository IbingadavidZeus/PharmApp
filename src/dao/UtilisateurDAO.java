package dao;

import model.Utilisateur;
import java.sql.SQLException;
import java.util.List;

public interface UtilisateurDAO {
    Utilisateur authentifierUtilisateur(String nomUtilisateur, String motDePasse) throws SQLException;

    boolean ajouterUtilisateur(Utilisateur utilisateur) throws SQLException;

    List<Utilisateur> getAllUtilisateurs() throws SQLException;

    boolean mettreAJourUtilisateur(Utilisateur utilisateur) throws SQLException;

    boolean supprimerUtilisateur(int id) throws SQLException;

    Utilisateur getUtilisateurById(int id) throws SQLException;
}