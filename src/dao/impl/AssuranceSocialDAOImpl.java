package dao.impl;

import dao.AssuranceSocialDAO;
import dao.DatabaseManager;
import model.AssuranceSocial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

public class AssuranceSocialDAOImpl implements AssuranceSocialDAO {
    
    @Override
    public AssuranceSocial getAssuranceById(int id_assurance) throws SQLException {
        String sql = "SELECT id_assurance, nom_assurance, taux_de_prise_en_charge FROM assurances_social WHERE id_assurance = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id_assurance);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractAssuranceFromResultSet(rs);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public AssuranceSocial getAssuranceByName(String nom_assurance) throws SQLException {
        String sql = "SELECT id_assurance, nom_assurance, taux_de_prise_en_charge FROM assurances_social WHERE nom_assurance = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nom_assurance);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractAssuranceFromResultSet(rs);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public List<AssuranceSocial> getAllAssurances() throws SQLException {
        List<AssuranceSocial> assurances = new ArrayList<>();
        String sql = "SELECT id_assurance, nom_assurance, taux_de_prise_en_charge FROM assurances_social";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                assurances.add(extractAssuranceFromResultSet(rs));
            }
        } finally {
            DatabaseManager.close(conn, stmt, rs);
        }
        return assurances;
    }

    @Override
    public boolean ajouterAssurance(AssuranceSocial assurance) throws SQLException {
        String sql = "INSERT INTO assurances_social (nom_assurance, taux_de_prise_en_charge) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, assurance.getNom_assurance());
            pstmt.setDouble(2, assurance.getTauxDePriseEnCharge());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    assurance.setId_assurance(rowsAffected);
                }
                return true;
            }
            return false;
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
    }

    @Override
    public boolean mettreAJourAssurance(AssuranceSocial assurance) throws SQLException {
        String sql = "UPDATE assurances_social SET nom_assurance = ?, taux_de_prise_en_charge = ? WHERE id_assurance = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, assurance.getNom_assurance());
            pstmt.setDouble(2, assurance.getTauxDePriseEnCharge());
            pstmt.setInt(3, assurance.getId_assurance());
            return pstmt.executeUpdate() > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public boolean supprimerAssurance(int id_assurance) throws SQLException {
        String sql = "DELETE FROM assurances_social WHERE id_assurance = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id_assurance);
            return pstmt.executeUpdate() > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    private AssuranceSocial extractAssuranceFromResultSet(ResultSet rs) throws SQLException {
        int id_assurance = rs.getInt("id_assurance");
        String nom_assurance = rs.getString("nom_assurance");
        double taux_de_prise_en_charge = rs.getDouble("taux_de_prise_en_charge");
        return new AssuranceSocial(id_assurance, nom_assurance, taux_de_prise_en_charge);
    }
}
