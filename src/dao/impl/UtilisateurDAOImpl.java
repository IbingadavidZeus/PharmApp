package dao.impl;

import dao.DatabaseManager;
import dao.UtilisateurDAO;
import model.Utilisateur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAOImpl implements UtilisateurDAO {

    @Override
    public boolean ajouterUtilisateur(Utilisateur utilisateur) throws SQLException {
        String sql = "INSERT INTO Utilisateurs (nom_utilisateur, mot_de_passe_hash, role) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, utilisateur.getNomUtilisateur());
            pstmt.setString(2, utilisateur.getMotDePasse());
            pstmt.setString(3, utilisateur.getRole());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public Utilisateur authentifierUtilisateur(String nomUtilisateur, String motDePasse) throws SQLException {
        String sql = "SELECT id_utilisateur, nom_utilisateur, mot_de_passe_hash, role FROM Utilisateurs WHERE nom_utilisateur = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Utilisateur utilisateur = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nomUtilisateur);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int idUtilisateur = rs.getInt("id_utilisateur");
                String dbNomUtilisateur = rs.getString("nom_utilisateur");
                String motDePasseHash = rs.getString("mot_de_passe_hash");
                String role = rs.getString("role");

                if (motDePasse.equals(motDePasseHash)) {
                    utilisateur = new Utilisateur(idUtilisateur, dbNomUtilisateur, motDePasseHash, role);
                }
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return utilisateur;
    }

    @Override
    public List<Utilisateur> getAllUtilisateurs() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT id_utilisateur, nom_utilisateur, mot_de_passe_hash, role FROM Utilisateurs";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Utilisateur utilisateur = new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("nom_utilisateur"),
                        rs.getString("mot_de_passe_hash"),
                        rs.getString("role"));
                utilisateurs.add(utilisateur);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return utilisateurs;
    }

    @Override
    public boolean mettreAJourUtilisateur(Utilisateur utilisateur) throws SQLException {
        String sql = "UPDATE Utilisateurs SET nom_utilisateur = ?, mot_de_passe_hash = ?, role = ? WHERE id_utilisateur = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, utilisateur.getNomUtilisateur());
            pstmt.setString(2, utilisateur.getMotDePasse());
            pstmt.setString(3, utilisateur.getRole());
            pstmt.setInt(4, utilisateur.getId());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public boolean supprimerUtilisateur(int id) throws SQLException {
        String sql = "DELETE FROM Utilisateurs WHERE id_utilisateur = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public Utilisateur getUtilisateurById(int id) throws SQLException {
        String sql = "SELECT id_utilisateur, nom_utilisateur, mot_de_passe_hash, role FROM Utilisateurs WHERE id_utilisateur = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Utilisateur utilisateur = null;

        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int idUtilisateur = rs.getInt("id_utilisateur");
                String nomUtilisateur = rs.getString("nom_utilisateur");
                String motDePasseHash = rs.getString("mot_de_passe_hash");
                String role = rs.getString("role");

                utilisateur = new Utilisateur(idUtilisateur, nomUtilisateur, motDePasseHash, role);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return utilisateur;
    }
}