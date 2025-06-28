package dao;

import model.LigneApprovisionnement;
import java.sql.Connection; 
import java.sql.SQLException;
import java.util.List;

public interface LigneApprovisionnementDAO {
    boolean addLigneApprovisionnement(LigneApprovisionnement ligneApprovisionnement) throws SQLException;
    boolean addLigneApprovisionnement(Connection conn, LigneApprovisionnement ligneApprovisionnement) throws SQLException;
    LigneApprovisionnement getLigneApprovisionnementById(int id) throws SQLException;
    List<LigneApprovisionnement> getLignesApprovisionnementByApproId(int approvisionnementId) throws SQLException;
    boolean updateLigneApprovisionnement(LigneApprovisionnement ligneApprovisionnement) throws SQLException;
    boolean deleteLigneApprovisionnement(int id) throws SQLException;
    boolean deleteLignesApprovisionnementByApproId(int approvisionnementId) throws SQLException;
}
