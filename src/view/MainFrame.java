package view;

import model.Pharmacie;
import model.Utilisateur;
// Supprimer les imports de DAO et DatabaseManager si App.java les gère
// import dao.UtilisateurDAO;
// import dao.impl.UtilisateurDAOImpl;
// import dao.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
// import java.sql.SQLException; // Pas toujours nécessaire ici si les exceptions sont gérées localement
import java.util.Objects;

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
    private InfoPanel infoPanel;
    private VentePanel ventePanel;
    private GestionUtilisateursPanel gestionUtilisateursPanel;
    private HistoriqueVentesPanel historiqueVentesPanel; // NOUVEAU: Instance du panneau HistoriqueVentes

    private Utilisateur currentUser;
    private String currentCardName = "Login";

    public MainFrame() {
        Pharmacie loadedPharmacie = Pharmacie.chargerDepuisFichier("pharmacie.ser");
        if (loadedPharmacie == null) {
            this.pharmacie = new Pharmacie("Ma Pharmacie", "123 Rue Principale");
        } else {
            this.pharmacie = loadedPharmacie;
        }

        setTitle("Gestion de Pharmacie");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                pharmacie.sauvegarderDansFichier("pharmacie.ser");
                JOptionPane.showMessageDialog(MainFrame.this, "Informations de la pharmacie sauvegardées. Au revoir !");
                System.exit(0);
            }
        });

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialisation des panels
        loginPanel = new LoginPanel(pharmacie, this);
        welcomePanel = new WelcomePanel();
        ajouterProduitPanel = new AjouterProduitPanel(pharmacie, this);
        stockPanel = new StockPanel(pharmacie, this);
        infoPanel = new InfoPanel(pharmacie, this);
        ventePanel = new VentePanel(pharmacie, currentUser, this);
        gestionUtilisateursPanel = new GestionUtilisateursPanel(pharmacie, this); 
        historiqueVentesPanel = new HistoriqueVentesPanel(pharmacie); // NOUVEAU: Initialisation du panneau HistoriqueVentes

        mainPanel.add(loginPanel, "Login");
        mainPanel.add(welcomePanel, "Welcome");
        mainPanel.add(ajouterProduitPanel, "AjouterProduit");
        mainPanel.add(stockPanel, "Stock");
        mainPanel.add(infoPanel, "Info");
        mainPanel.add(ventePanel, "Vente");
        mainPanel.add(gestionUtilisateursPanel, "GererUtilisateurs");
        mainPanel.add(historiqueVentesPanel, "HistoriqueVentes"); // NOUVEAU: Ajout du panneau

        add(mainPanel);

        showLoginPanel();
    }

    private void showCard(String cardName) {
        cardLayout.show(mainPanel, cardName);
        this.currentCardName = cardName;
        
        // Rafraîchir les panels si nécessaire lors du changement de carte
        if (Objects.equals(cardName, "Vente")) {
            ventePanel.setCurrentUser(currentUser);
            ventePanel.refreshProductSelectionTable();
        } else if (Objects.equals(cardName, "Stock")) {
            stockPanel.remplirTable();
        } else if (Objects.equals(cardName, "Info")) {
            infoPanel.updatePharmacyInfo();
        } else if (Objects.equals(cardName, "GererUtilisateurs")) {
            gestionUtilisateursPanel.refreshUsersTable();
        } else if (Objects.equals(cardName, "HistoriqueVentes")) { // NOUVEAU: Rafraîchir l'historique des ventes
            historiqueVentesPanel.refreshFacturesTable();
        }
    }

    private void showLoginPanel() {
        showCard("Login");
        if (menuBar != null) {
            setJMenuBar(null);
            revalidate();
            repaint();
        }
    }

    private void showWelcomePanel(Utilisateur user) {
        welcomePanel.setWelcomeMessage("Bienvenue, " + user.getNomUtilisateur() + " (" + user.getRole() + ")!");
        showCard("Welcome");
        createMenuBar(user.getRole());
    }

    private void createMenuBar(String role) {
        if (menuBar != null) {
            setJMenuBar(null);
        }
        
        menuBar = new JMenuBar();

        JMenu homeMenu = new JMenu("Accueil");
        JMenuItem homeItem = new JMenuItem("Accueil Principal");
        homeItem.addActionListener(_ -> showCard("Welcome"));
        homeMenu.add(homeItem);
        menuBar.add(homeMenu);

        JMenu gestionMenu = new JMenu("Gestion");
        if ("admin".equalsIgnoreCase(role)) {
            JMenuItem gererUtilisateursItem = new JMenuItem("Gérer Utilisateurs");
            gererUtilisateursItem.addActionListener(_ -> showCard("GererUtilisateurs"));
            gestionMenu.add(gererUtilisateursItem);
        }
        
        JMenuItem ajouterProduitItem = new JMenuItem("Ajouter Produit");
        ajouterProduitItem.addActionListener(_ -> showCard("AjouterProduit"));
        gestionMenu.add(ajouterProduitItem);

        JMenuItem voirStockItem = new JMenuItem("Voir Stock");
        voirStockItem.addActionListener(_ -> showCard("Stock"));
        gestionMenu.add(voirStockItem);

        JMenu venteMenu = new JMenu("Vente");
        JMenuItem effectuerVenteItem = new JMenuItem("Effectuer une Vente");
        effectuerVenteItem.addActionListener(_ -> showCard("Vente"));
        venteMenu.add(effectuerVenteItem);

        JMenuItem historiqueVentesItem = new JMenuItem("Historique des Ventes");
        historiqueVentesItem.addActionListener(_ -> showCard("HistoriqueVentes")); // NOUVEAU: Lier au panneau
        venteMenu.add(historiqueVentesItem);

        menuBar.add(gestionMenu);
        menuBar.add(venteMenu);

        JMenu pharmacieMenu = new JMenu("Pharmacie");
        JMenuItem infoPharmacieItem = new JMenuItem("Informations Pharmacie");
        infoPharmacieItem.addActionListener(_ -> showCard("Info"));
        pharmacieMenu.add(infoPharmacieItem);
        menuBar.add(pharmacieMenu);

        JMenu logoutMenu = new JMenu("Session");
        JMenuItem logoutItem = new JMenuItem("Déconnexion");
        logoutItem.addActionListener(_ -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Voulez-vous vraiment vous déconnecter ?", "Déconnexion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                currentUser = null;
                showLoginPanel();
            }
        });
        logoutMenu.add(logoutItem);
        menuBar.add(logoutMenu);

        setJMenuBar(menuBar);
        revalidate();
        repaint();
    }

    @Override
    public void onLoginSuccess(Utilisateur user) {
        this.currentUser = user;
        if (ventePanel != null) {
            ventePanel.setCurrentUser(user);
        }
        showWelcomePanel(user);
    }

    @Override
    public void onPharmacieDataChanged() {
        // Appelé lorsque des données de la pharmacie changent (e.g., ajout/suppression de produit/utilisateur, vente finalisée)
        stockPanel.remplirTable();
        infoPanel.updatePharmacyInfo();
        gestionUtilisateursPanel.refreshUsersTable();
        ventePanel.refreshProductSelectionTable();
        historiqueVentesPanel.refreshFacturesTable(); // NOUVEAU: Rafraîchir l'historique des ventes
    }
}
