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
        // Assume utilisateur.getMotDePasse() returns the HASHED password now
        String sql = "INSERT INTO Utilisateurs (nom_utilisateur, mot_de_passe_hash, role) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, utilisateur.getNomUtilisateur());
            pstmt.setString(2, utilisateur.getMotDePasse()); // This should be the HASHED password
            pstmt.setString(3, utilisateur.getRole());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public Utilisateur authentifierUtilisateur(String nomUtilisateur, String motDePasse) throws SQLException {
        // This method will need proper password hashing comparison in a real app.
        // For now, it will retrieve the user and the calling service will compare the hash.
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
                // CORRECTION: Retrieve id_utilisateur and use the constructor that includes ID
                int idUtilisateur = rs.getInt("id_utilisateur");
                String dbNomUtilisateur = rs.getString("nom_utilisateur");
                String motDePasseHash = rs.getString("mot_de_passe_hash");
                String role = rs.getString("role");
                
                // In a real application, you'd hash the 'motDePasse' provided by the user
                // and compare it with 'motDePasseHash' from the DB here.
                // For this example, we return the user if found, assuming comparison happens elsewhere.
                // If this method is for actual authentication, add password verification logic here.
                if (motDePasse.equals(motDePasseHash)) { // Placeholder: In production, use a secure hashing library
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
        // CORRECTION: Select id_utilisateur
        String sql = "SELECT id_utilisateur, nom_utilisateur, mot_de_passe_hash, role FROM Utilisateurs";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                // CORRECTION: Retrieve id_utilisateur and use the constructor that includes ID
                Utilisateur utilisateur = new Utilisateur(
                    rs.getInt("id_utilisateur"), // Get the ID
                    rs.getString("nom_utilisateur"),
                    rs.getString("mot_de_passe_hash"),
                    rs.getString("role")
                );
                utilisateurs.add(utilisateur);
            }
        } finally {
            DatabaseManager.close(conn, pstmt, rs);
        }
        return utilisateurs;
    }

    @Override
    public boolean mettreAJourUtilisateur(Utilisateur utilisateur) throws SQLException {
        // CORRECTION: Use id_utilisateur for WHERE clause for robust updates
        // Assume utilisateur.getMotDePasse() returns the HASHED password now
        String sql = "UPDATE Utilisateurs SET nom_utilisateur = ?, mot_de_passe_hash = ?, role = ? WHERE id_utilisateur = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, utilisateur.getNomUtilisateur()); // Can update username as well
            pstmt.setString(2, utilisateur.getMotDePasse()); // This should be the HASHED password
            pstmt.setString(3, utilisateur.getRole());
            pstmt.setInt(4, utilisateur.getId()); // Use the ID for the WHERE clause
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    // CORRECTION: Changed parameter from String nomUtilisateur to int id to match interface
    public boolean supprimerUtilisateur(int id) throws SQLException {
        String sql = "DELETE FROM Utilisateurs WHERE id_utilisateur = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id); // Use the ID for deletion
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            DatabaseManager.close(conn, pstmt);
        }
    }

    @Override
    public Utilisateur getUtilisateurById(int id) throws SQLException {
        // This method was mostly correct, just ensuring column names match
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