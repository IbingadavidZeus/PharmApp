package dao;

import model.Fournisseur;
import java.sql.SQLException;
import java.util.List;

public interface FournisseurDAO {
    Fournisseur getFournisseurById(int id) throws SQLException;
    Fournisseur getFournisseurByNom(String nomFournisseur) throws SQLException;
    List<Fournisseur> getAllFournisseurs() throws SQLException;
    boolean addFournisseur(Fournisseur fournisseur) throws SQLException;
    boolean updateFournisseur(Fournisseur fournisseur) throws SQLException;
    boolean deleteFournisseur(int id) throws SQLException;
}
