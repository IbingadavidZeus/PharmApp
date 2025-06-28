package dao.impl;

import dao.DatabaseManager;
import dao.FournisseurDAO;
import model.Fournisseur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FournisseurDAOImpl implements FournisseurDAO {

    @Override
    public Fournisseur getFournisseurById(int id) throws SQLException {
        String sql = "SELECT id_fournisseur, nom_fournisseur, contact_fournisseur, telephone_fournisseur, email_fournisseur, adresse_fournisseur FROM fournisseurs WHERE id_fournisseur = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractFournisseurFromResultSet(rs);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public Fournisseur getFournisseurByNom(String nomFournisseur) throws SQLException {
        String sql = "SELECT id_fournisseur, nom_fournisseur, contact_fournisseur, telephone_fournisseur, email_fournisseur, adresse_fournisseur FROM fournisseurs WHERE nom_fournisseur = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nomFournisseur);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractFournisseurFromResultSet(rs);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public List<Fournisseur> getAllFournisseurs() throws SQLException {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String sql = "SELECT id_fournisseur, nom_fournisseur, contact_fournisseur, telephone_fournisseur, email_fournisseur, adresse_fournisseur FROM fournisseurs ORDER BY nom_fournisseur";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                fournisseurs.add(extractFournisseurFromResultSet(rs));
            }
        } finally {
            DatabaseManager.close(conn, stmt, rs);
        }
        return fournisseurs;
    }

    @Override
    public boolean addFournisseur(Fournisseur fournisseur) throws SQLException {
        String sql = "INSERT INTO fournisseurs (nom_fournisseur, contact_fournisseur, telephone_fournisseur, email_fournisseur, adresse_fournisseur) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, fournisseur.getNomFournisseur());
            pstmt.setString(2, fournisseur.getContactPersonne());
            pstmt.setString(3, fournisseur.getTelephone());
            pstmt.setString(4, fournisseur.getEmail());
            pstmt.setString(5, fournisseur.getAdresse());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    fournisseur.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
    }

    @Override
    public boolean updateFournisseur(Fournisseur fournisseur) throws SQLException {
        String sql = "UPDATE fournisseurs SET nom_fournisseur = ?, contact_fournisseur = ?, telephone_fournisseur = ?, email_fournisseur = ?, adresse_fournisseur = ? WHERE id_fournisseur = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fournisseur.getNomFournisseur());
            pstmt.setString(2, fournisseur.getContactPersonne());
            pstmt.setString(3, fournisseur.getTelephone());
            pstmt.setString(4, fournisseur.getEmail());
            pstmt.setString(5, fournisseur.getAdresse());
            pstmt.setInt(6, fournisseur.getId());
            return pstmt.executeUpdate() > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public boolean deleteFournisseur(int id) throws SQLException {
        String sql = "DELETE FROM fournisseurs WHERE id_fournisseur = ?";
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

    private Fournisseur extractFournisseurFromResultSet(ResultSet rs) throws SQLException {
        return new Fournisseur(
                rs.getInt("id_fournisseur"),
                rs.getString("nom_fournisseur"),
                rs.getString("contact_fournisseur"),
                rs.getString("telephone_fournisseur"),
                rs.getString("email_fournisseur"),
                rs.getString("adresse_fournisseur")
        );
    }
}
