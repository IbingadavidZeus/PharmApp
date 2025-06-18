package dao;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/pharmacie_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "mysql@01091995";

    public static Connection getConnection() throws SQLException {
        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Pilote JDBC MySQL introuvable ! Assurez-vous que le JAR est dans le classpath.");
            throw new SQLException("Pilote JDBC manquant", e);
        }
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    /**
     * Ferme la connexion, le statement et le resultSet.
     * 
     * @param conn La connexion à fermer.
     * @param stmt Le statement à fermer.
     * @param rs   Le resultSet à fermer.
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture du ResultSet: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture du Statement: " + e.getMessage());
            } finally {
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture de la Connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Ferme la connexion, le statement et le resultSet.
     * 
     * @param conn La connexion à fermer.
     * @param stmt Le statement à fermer.
     * @param rs   Le resultSet à fermer.
     */
    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }

    /**
     * Ferme la connexion à la base de données
     * 
     * @param conn La connexion à fermer.
     */
    public static void close(Connection conn) {
        close(conn, null, null);
    }
}