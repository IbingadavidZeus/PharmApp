package view;

import model.Pharmacie;
import model.Utilisateur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
// Pas besoin d'importer UtilisateurDAO ni DatabaseManager ici, la logique est dans App.java

// Interface pour notifier MainFrame des événements de connexion
interface LoginListener {
    void onLoginSuccess(Utilisateur user);
}

// Interface pour notifier MainFrame des changements de données dans la Pharmacie
interface PharmacieDataListener {
    void onPharmacieDataChanged();
}

public class MainFrame extends JFrame implements LoginListener, PharmacieDataListener {
    private Pharmacie pharmacie;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JMenuBar menuBar;

    private LoginPanel loginPanel;
    private WelcomePanel welcomePanel;
    private AjouterProduitPanel ajouterProduitPanel;
    private StockPanel stockPanel;
    private InfoPanel infoPanel; // Ajout du panneau d'information

    private Utilisateur currentUser; // Pour stocker l'utilisateur actuellement connecté

    public MainFrame() {
        // Le chargement initial de la pharmacie doit se faire ici.
        // Puisque les données sont en BDD, la sérialisation ne concerne plus que les paramètres de base.
        // La création de l'admin par défaut est maintenant dans App.java
        Pharmacie loadedPharmacie = Pharmacie.chargerDepuisFichier("pharmacie.ser");
        if (loadedPharmacie == null) {
            // Si le fichier n'existe pas ou est corrompu, crée une nouvelle instance avec des valeurs par défaut.
            this.pharmacie = new Pharmacie("Ma Pharmacie", "123 Rue Principale");
        } else {
            // Si le fichier existe, utilise l'instance chargée.
            this.pharmacie = loadedPharmacie;
        }

        setTitle("Gestion de Pharmacie");
        setSize(1000, 700); // Augmenter la taille pour plus de confort
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Géré par WindowAdapter
        setLocationRelativeTo(null); // Centrer la fenêtre

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Sauvegarde seulement les informations de base de la pharmacie (nom, adresse)
                // les produits, utilisateurs et factures sont dans la BDD.
                pharmacie.sauvegarderDansFichier("pharmacie.ser");
                JOptionPane.showMessageDialog(MainFrame.this, "Informations de la pharmacie sauvegardées. Au revoir !");
                System.exit(0);
            }
        });

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialisation des panels
        loginPanel = new LoginPanel(pharmacie, this); // 'this' fait référence à MainFrame qui implémente LoginListener
        welcomePanel = new WelcomePanel();
        ajouterProduitPanel = new AjouterProduitPanel(pharmacie, this); // 'this' pour le dataListener
        stockPanel = new StockPanel(pharmacie, this); // 'this' pour le dataListener
        infoPanel = new InfoPanel(pharmacie, this); // 'this' pour le dataListener

        mainPanel.add(loginPanel, "Login");
        mainPanel.add(welcomePanel, "Welcome");
        mainPanel.add(ajouterProduitPanel, "AjouterProduit");
        mainPanel.add(stockPanel, "Stock");
        mainPanel.add(infoPanel, "Info"); // Ajout du panneau d'information
        // Ajoutez d'autres panels ici

        add(mainPanel);

        showLoginPanel(); // Afficher le panneau de connexion au démarrage
    }

    private void showLoginPanel() {
        cardLayout.show(mainPanel, "Login");
        // Supprimer la barre de menu tant que l'utilisateur n'est pas connecté
        if (menuBar != null) {
            setJMenuBar(null);
            revalidate();
            repaint();
        }
    }

    private void showWelcomePanel(Utilisateur user) {
        welcomePanel.setWelcomeMessage("Bienvenue, " + user.getNomUtilisateur() + " (" + user.getRole() + ")!");
        cardLayout.show(mainPanel, "Welcome");
        createMenuBar(user.getRole()); // Créer la barre de menu en fonction du rôle
    }
    
    // Méthode utilitaire pour obtenir le nom de la carte actuellement affichée
    private String getCurrentCardName() {
        for (Component comp : mainPanel.getComponents()) {
            if (comp.isVisible()) {
                CardLayout layout = (CardLayout) mainPanel.getLayout();
                return ((BorderLayout) mainPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER).getName(); // This is a placeholder, won't work with CardLayout name
            }
        }
        return null;
    }

    private void createMenuBar(String role) {
        // S'assurer qu'une seule barre de menu est créée/affichée
        if (menuBar != null) {
            setJMenuBar(null); // Retire l'ancienne barre avant de créer la nouvelle
        }
        
        menuBar = new JMenuBar();

        // Menu "Accueil"
        JMenu homeMenu = new JMenu("Accueil");
        JMenuItem homeItem = new JMenuItem("Accueil Principal");
        homeItem.addActionListener(_ -> showWelcomePanel(currentUser));
        homeMenu.add(homeItem);
        menuBar.add(homeMenu);

        // Menu "Gestion"
        JMenu gestionMenu = new JMenu("Gestion");
        if ("admin".equalsIgnoreCase(role)) { // Utiliser equalsIgnoreCase pour la robustesse
            JMenuItem gererUtilisateursItem = new JMenuItem("Gérer Utilisateurs");
            // gererUtilisateursItem.addActionListener(e -> cardLayout.show(mainPanel, "GererUtilisateurs")); // À décommenter une fois le panel créé
            gestionMenu.add(gererUtilisateursItem);
        }
        
        JMenuItem ajouterProduitItem = new JMenuItem("Ajouter Produit");
        ajouterProduitItem.addActionListener(_ -> cardLayout.show(mainPanel, "AjouterProduit"));
        gestionMenu.add(ajouterProduitItem);

        JMenuItem voirStockItem = new JMenuItem("Voir Stock");
        voirStockItem.addActionListener(_ -> {
            stockPanel.remplirTable(); // Rafraîchir le stock avant d'afficher
            cardLayout.show(mainPanel, "Stock");
        });
        gestionMenu.add(voirStockItem);

        // Menu Vente (pour tout le monde)
        JMenu venteMenu = new JMenu("Vente");
        JMenuItem effectuerVenteItem = new JMenuItem("Effectuer une Vente");
        // effectuerVenteItem.addActionListener(e -> cardLayout.show(mainPanel, "EffectuerVente")); // À décommenter une fois le panel créé
        venteMenu.add(effectuerVenteItem);

        JMenuItem historiqueVentesItem = new JMenuItem("Historique des Ventes");
        // historiqueVentesItem.addActionListener(e -> cardLayout.show(mainPanel, "HistoriqueVentes")); // À décommenter une fois le panel créé
        venteMenu.add(historiqueVentesItem);

        menuBar.add(gestionMenu);
        menuBar.add(venteMenu); // Ajoute le menu vente à la barre de menu

        // Menu "Pharmacie" (pour accéder aux informations et la gestion de la pharmacie elle-même)
        JMenu pharmacieMenu = new JMenu("Pharmacie");
        JMenuItem infoPharmacieItem = new JMenuItem("Informations Pharmacie");
        infoPharmacieItem.addActionListener(_ -> {
            infoPanel.updatePharmacyInfo(); // S'assurer que les infos sont à jour
            cardLayout.show(mainPanel, "Info");
        });
        pharmacieMenu.add(infoPharmacieItem);
        menuBar.add(pharmacieMenu); // Ajoute le menu pharmacie à la barre de menu


        // Menu "Déconnexion"
        JMenu logoutMenu = new JMenu("Session");
        JMenuItem logoutItem = new JMenuItem("Déconnexion");
        logoutItem.addActionListener(_ -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Voulez-vous vraiment vous déconnecter ?", "Déconnexion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                currentUser = null; // Effacer l'utilisateur connecté
                showLoginPanel(); // Revenir au panneau de connexion
            }
        });
        logoutMenu.add(logoutItem);
        menuBar.add(logoutMenu); // Ajoute le menu session/déconnexion à la barre de menu

        setJMenuBar(menuBar);
        revalidate(); // Revalider le cadre pour afficher la nouvelle barre de menu
        repaint();
    }

    // Implémentation de l'interface LoginListener
    @Override
    public void onLoginSuccess(Utilisateur user) {
        this.currentUser = user; // Stocker l'utilisateur connecté
        showWelcomePanel(user);
    }

    // Implémentation de l'interface PharmacieDataListener
    @Override
    public void onPharmacieDataChanged() {
        // Rafraîchir les panels qui affichent des données de la pharmacie si nécessaire
        // Une méthode plus robuste pour obtenir la carte actuelle est nécessaire si on veut rafraîchir SELECTIVEMENT.
        // Pour l'instant, on peut forcer le rafraîchissement si le panel est actif ou juste le StockPanel.
        stockPanel.remplirTable(); // Le stock peut avoir été affecté par une vente ou ajout
        infoPanel.updatePharmacyInfo(); // Les infos de la pharmacie peuvent avoir été chargées/sauvegardées
        // Ajoutez d'autres rafraîchissements si vous avez d'autres panels dépendants
    }
}
