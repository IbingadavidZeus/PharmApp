package dao;

import model.LigneFacture;
import java.sql.Connection; 
import java.sql.SQLException;
import java.util.List;

public interface LigneFactureDAO {
    boolean ajouterLigneFacture(Connection conn, LigneFacture ligneFacture) throws SQLException;
    boolean ajouterLigneFacture(LigneFacture ligneFacture) throws SQLException;
    List<LigneFacture> getLignesFactureByFactureId(int idFacture) throws SQLException;

    LigneFacture getLigneFactureById(int id) throws SQLException;

    boolean mettreAJourLigneFacture(LigneFacture ligneFacture) throws SQLException;

    boolean supprimerLigneFacture(int id) throws SQLException;

    boolean supprimerLignesFactureByFactureId(int idFacture) throws SQLException;

}
