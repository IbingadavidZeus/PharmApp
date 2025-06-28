package dao;

import model.Approvisionnement;
import java.sql.Connection; 
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface ApprovisionnementDAO {
    boolean addApprovisionnement(Approvisionnement approvisionnement) throws SQLException;
    boolean addApprovisionnement(Connection conn, Approvisionnement approvisionnement) throws SQLException;
    Approvisionnement getApprovisionnementById(int id) throws SQLException;
    Approvisionnement getApprovisionnementByReference(String referenceBonCommande) throws SQLException;
    List<Approvisionnement> getAllApprovisionnements() throws SQLException;
    List<Approvisionnement> getApprovisionnementsByFournisseur(int idFournisseur) throws SQLException;
    List<Approvisionnement> getApprovisionnementsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException;
    boolean updateApprovisionnement(Approvisionnement approvisionnement) throws SQLException;
    boolean deleteApprovisionnement(int id) throws SQLException;
}
