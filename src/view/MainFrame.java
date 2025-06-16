package view;

import model.Pharmacie;
import model.Utilisateur;
import dao.UtilisateurDAO;
import dao.impl.UtilisateurDAOImpl;
import dao.DatabaseManager; // Pour la vérification de la BDD dans main (si non déjà dans App.java)

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.Objects; // Pour Objects.equals

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
    private VentePanel ventePanel; // NOUVEAU: Instance du VentePanel

    private Utilisateur currentUser; // Pour stocker l'utilisateur actuellement connecté
    private String currentCardName = "Login"; // Garde une trace de la carte actuellement affichée

    public MainFrame() {
        // Le chargement initial de la pharmacie doit se faire ici.
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
        // NOUVEAU: Initialisation du VentePanel
        // NOTE: currentUser est null au démarrage, il sera défini après connexion.
        // Le VentePanel aura une méthode pour le mettre à jour.
        ventePanel = new VentePanel(pharmacie, currentUser, this); 

        mainPanel.add(loginPanel, "Login");
        mainPanel.add(welcomePanel, "Welcome");
        mainPanel.add(ajouterProduitPanel, "AjouterProduit");
        mainPanel.add(stockPanel, "Stock");
        mainPanel.add(infoPanel, "Info");
        mainPanel.add(ventePanel, "Vente"); // NOUVEAU: Ajout du VentePanel

        add(mainPanel);

        showLoginPanel(); // Afficher le panneau de connexion au démarrage
    }

    // Méthode pour afficher une carte spécifique et mettre à jour le nom de la carte courante
    private void showCard(String cardName) {
        cardLayout.show(mainPanel, cardName);
        this.currentCardName = cardName; // Mettre à jour le nom de la carte courante
        // Si le panneau de vente est affiché, rafraîchissez son tableau de produits
        if (Objects.equals(cardName, "Vente")) {
            ventePanel.refreshProductSelectionTable();
            ventePanel.setCurrentUser(currentUser); // S'assurer que l'utilisateur est bien défini
        }
         if (Objects.equals(cardName, "Stock")) {
            stockPanel.remplirTable(); // S'assurer que le stock est à jour
        }
    }


    private void showLoginPanel() {
        showCard("Login"); // Utilise la nouvelle méthode
        if (menuBar != null) {
            setJMenuBar(null);
            revalidate();
            repaint();
        }
    }

    private void showWelcomePanel(Utilisateur user) {
        welcomePanel.setWelcomeMessage("Bienvenue, " + user.getNomUtilisateur() + " (" + user.getRole() + ")!");
        showCard("Welcome"); // Utilise la nouvelle méthode
        createMenuBar(user.getRole());
    }
    
    // Ancien getCurrentCardName simplifié
    // La méthode showCard() gère maintenant le suivi de la carte courante
    // Vous pouvez supprimer l'ancienne implémentation de getCurrentCardName si vous utilisez currentCardName.

    private void createMenuBar(String role) {
        if (menuBar != null) {
            setJMenuBar(null);
        }
        
        menuBar = new JMenuBar();

        JMenu homeMenu = new JMenu("Accueil");
        JMenuItem homeItem = new JMenuItem("Accueil Principal");
        homeItem.addActionListener(_ -> showCard("Welcome")); // Utilise showCard
        homeMenu.add(homeItem);
        menuBar.add(homeMenu);

        JMenu gestionMenu = new JMenu("Gestion");
        if ("admin".equalsIgnoreCase(role)) {
            JMenuItem gererUtilisateursItem = new JMenuItem("Gérer Utilisateurs");
            // gererUtilisateursItem.addActionListener(e -> showCard("GererUtilisateurs"));
            gestionMenu.add(gererUtilisateursItem);
        }
        
        JMenuItem ajouterProduitItem = new JMenuItem("Ajouter Produit");
        ajouterProduitItem.addActionListener(_ -> showCard("AjouterProduit")); // Utilise showCard
        gestionMenu.add(ajouterProduitItem);

        JMenuItem voirStockItem = new JMenuItem("Voir Stock");
        voirStockItem.addActionListener(_ -> showCard("Stock")); // Utilise showCard
        gestionMenu.add(voirStockItem);

        JMenu venteMenu = new JMenu("Vente");
        JMenuItem effectuerVenteItem = new JMenuItem("Effectuer une Vente");
        effectuerVenteItem.addActionListener(_ -> showCard("Vente")); // NOUVEAU: Lier au VentePanel
        venteMenu.add(effectuerVenteItem);

        JMenuItem historiqueVentesItem = new JMenuItem("Historique des Ventes");
        // historiqueVentesItem.addActionListener(e -> showCard("HistoriqueVentes"));
        venteMenu.add(historiqueVentesItem);

        menuBar.add(gestionMenu);
        menuBar.add(venteMenu);

        JMenu pharmacieMenu = new JMenu("Pharmacie");
        JMenuItem infoPharmacieItem = new JMenuItem("Informations Pharmacie");
        infoPharmacieItem.addActionListener(_ -> showCard("Info")); // Utilise showCard
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
        // Définir l'utilisateur dans le VentePanel dès la connexion
        if (ventePanel != null) {
            ventePanel.setCurrentUser(user);
        }
        showWelcomePanel(user);
    }

    @Override
    public void onPharmacieDataChanged() {
        // La méthode showCard gère déjà certains rafraîchissements lors du changement de panel.
        // Ici, on peut ajouter des rafraîchissements globaux ou spécifiques si le currentCardName le permet.
        if (Objects.equals(currentCardName, "Stock")) {
            stockPanel.remplirTable();
        } else if (Objects.equals(currentCardName, "Vente")) {
            ventePanel.refreshProductSelectionTable(); // Pour mettre à jour les stocks après une vente
        }
        infoPanel.updatePharmacyInfo(); // Toujours garder les infos de pharmacie à jour si elles sont modifiables.
    }
    
    // Main method is in App.java
}
