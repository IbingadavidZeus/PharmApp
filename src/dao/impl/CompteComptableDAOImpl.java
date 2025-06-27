package dao.impl;

import dao.CompteComptableDAO;
import dao.DatabaseManager;
import model.CompteComptable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CompteComptableDAOImpl implements CompteComptableDAO {

    @Override
    public CompteComptable getCompteById(int id) throws SQLException {
        String sql = "SELECT id_compte, numero_compte, nom_compte, type_compte, description FROM comptes_comptables WHERE id_compte = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractCompteFromResultSet(rs);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public CompteComptable getCompteByNumero(String numeroCompte) throws SQLException {
        String sql = "SELECT id_compte, numero_compte, nom_compte, type_compte, description FROM comptes_comptables WHERE numero_compte = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, numeroCompte);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractCompteFromResultSet(rs);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public List<CompteComptable> getAllComptes() throws SQLException {
        List<CompteComptable> comptes = new ArrayList<>();
        String sql = "SELECT id_compte, numero_compte, nom_compte, type_compte, description FROM comptes_comptables ORDER BY numero_compte";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                comptes.add(extractCompteFromResultSet(rs));
            }
        } finally {
            DatabaseManager.close(conn, stmt, rs);
        }
        return comptes;
    }

    @Override
    public boolean addCompte(CompteComptable compte) throws SQLException {
        String sql = "INSERT INTO comptes_comptables (numero_compte, nom_compte, type_compte, description) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, compte.getNumeroCompte());
            pstmt.setString(2, compte.getNomCompte());
            pstmt.setString(3, compte.getTypeCompte());
            pstmt.setString(4, compte.getDescription());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    compte.setId_compteComptable(rs.getInt(1));
                }
                return true;
            }
            return false;
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
    }

    @Override
    public boolean updateCompte(CompteComptable compte) throws SQLException {
        String sql = "UPDATE comptes_comptables SET numero_compte = ?, nom_compte = ?, type_compte = ?, description = ? WHERE id_compte = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, compte.getNumeroCompte());
            pstmt.setString(2, compte.getNomCompte());
            pstmt.setString(3, compte.getTypeCompte());
            pstmt.setString(4, compte.getDescription());
            pstmt.setInt(5, compte.getId_compteComptable());
            return pstmt.executeUpdate() > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public boolean deleteCompte(int id) throws SQLException {
        String sql = "DELETE FROM comptes_comptables WHERE id_compte = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    // Méthode utilitaire pour extraire un CompteComptable d'un ResultSet
    private CompteComptable extractCompteFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_compte");
        String numeroCompte = rs.getString("numero_compte");
        String nomCompte = rs.getString("nom_compte");
        String typeCompte = rs.getString("type_compte");
        String description = rs.getString("description");
        return new CompteComptable(id, numeroCompte, nomCompte, typeCompte, description);
    }
}
