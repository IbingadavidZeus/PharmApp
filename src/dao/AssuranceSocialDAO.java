package dao;

import model.AssuranceSocial;
import java.sql.SQLException;
import java.util.List;

public interface AssuranceSocialDAO {
    AssuranceSocial getAssuranceById(int id_assurance) throws SQLException;

    AssuranceSocial getAssuranceByName(String nom_assurance) throws SQLException;

    List<AssuranceSocial> getAllAssurances() throws SQLException;

    boolean ajouterAssurance(AssuranceSocial assurance) throws SQLException;

    boolean mettreAJourAssurance(AssuranceSocial assurance) throws SQLException;

    boolean supprimerAssurance(int id) throws SQLException;
}
