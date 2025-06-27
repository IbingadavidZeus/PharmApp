package dao;

import model.CompteComptable;
import java.sql.SQLException;
import java.util.List;

public interface CompteComptableDAO {
    // Récupère un compte par son ID
    CompteComptable getCompteById(int id_compteComptable) throws SQLException;

    // Récupère un compte par son numéro de compte (unique)
    CompteComptable getCompteByNumero(String numeroCompte) throws SQLException;

    // Récupère tous les comptes comptables
    List<CompteComptable> getAllComptes() throws SQLException;

    // Ajoute un nouveau compte comptable
    boolean addCompte(CompteComptable compte) throws SQLException;

    // Met à jour un compte comptable existant
    boolean updateCompte(CompteComptable compte) throws SQLException;

    // Supprime un compte comptable par son ID
    boolean deleteCompte(int id) throws SQLException;
}
