
import dao.UtilisateurDAO;
import dao.impl.UtilisateurDAOImpl;
import model.Utilisateur;
import view.MainFrame;
import dao.DatabaseManager;

import javax.swing.*;
import java.sql.SQLException;

public class App {

    public static void main(String[] args) {
        // --- Étape 1: Vérification et initialisation de la base de données ---
        try {
            // Tente d'établir une connexion pour s'assurer que la base de données est accessible
            if (DatabaseManager.getConnection() != null) {
                System.out.println("Connexion à la base de données établie avec succès au démarrage.");

                // Vérifie si la table des utilisateurs est vide et ajoute un administrateur par défaut si nécessaire
                UtilisateurDAO utilisateurDAO = new UtilisateurDAOImpl();
                if (utilisateurDAO.getAllUtilisateurs().isEmpty()) {
                    System.out.println("Aucun utilisateur trouvé. Création d'un administrateur par défaut.");
                    // Utiliser un mot de passe fort en production et le hacher avant de le stocker!
                    Utilisateur admin = new Utilisateur("admin", "adminpass", "admin");
                    utilisateurDAO.ajouterUtilisateur(admin);
                    System.out.println("Utilisateur 'admin' (mot de passe: 'adminpass') créé.");
                }
            } else {
                // Si la connexion retourne null (problème non capturé par SQLException directe)
                System.err.println("Échec de la connexion à la base de données au démarrage.");
                JOptionPane.showMessageDialog(null, "Impossible de se connecter à la base de données. Veuillez vérifier la configuration.", "Erreur de base de données", JOptionPane.ERROR_MESSAGE);
                System.exit(1); // Quitter l'application si la connexion échoue
            }
        } catch (SQLException e) {
            // Capture les exceptions SQL spécifiques lors de la connexion ou de l'opération DAO
            System.err.println("Erreur SQL lors de la vérification de la base de données au démarrage: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Erreur SQL lors du démarrage de l'application: " + e.getMessage(), "Erreur de base de données", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Imprime la pile d'appels pour le débogage
            System.exit(1); // Quitter l'application en cas d'erreur critique de la base de données
        } catch (Exception e) {
            // Capture toute autre exception inattendue lors du démarrage
            System.err.println("Une erreur inattendue est survenue au démarrage: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Une erreur inattendue est survenue au démarrage: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        // --- Étape 2: Lancement de l'interface graphique Swing ---
        // S'assure que l'interface graphique est créée et mise à jour sur le Thread d'événements de Swing
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
