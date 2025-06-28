package view;

import model.Pharmacie;
import model.Utilisateur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

interface LoginListener {
    void onLoginSuccess(Utilisateur user);
}

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
    private ComptabilitePanel comptabilitePanel;
    private VentePanel ventePanel;
    private GestionUtilisateursPanel gestionUtilisateursPanel;
    private HistoriqueVentesPanel historiqueVentesPanel;
    private ApprovisionnementPanel approvisionnementPanel;
    private GestionAssurancesPanel gestionAssurancesPanel;
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

        try {
            Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/image/app_icon.png"));
            this.setIconImage(icon);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'icône: " + e.getMessage());
            e.printStackTrace();
        }

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

        loginPanel = new LoginPanel(pharmacie, this);
        welcomePanel = new WelcomePanel();
        ajouterProduitPanel = new AjouterProduitPanel(pharmacie, this);
        stockPanel = new StockPanel(pharmacie, this);
        infoPanel = new InfoPanel(pharmacie, this);
        ventePanel = new VentePanel(pharmacie, currentUser, this);
        gestionUtilisateursPanel = new GestionUtilisateursPanel(pharmacie, this);
        historiqueVentesPanel = new HistoriqueVentesPanel(pharmacie);
        approvisionnementPanel = new ApprovisionnementPanel(pharmacie, this);
        comptabilitePanel = new ComptabilitePanel(pharmacie, this);
        gestionAssurancesPanel = new GestionAssurancesPanel(pharmacie, this);

        mainPanel.add(loginPanel, "Login");
        mainPanel.add(welcomePanel, "Welcome");
        mainPanel.add(ajouterProduitPanel, "AjouterProduit");
        mainPanel.add(stockPanel, "Stock");
        mainPanel.add(infoPanel, "Info");
        mainPanel.add(ventePanel, "Vente");
        mainPanel.add(gestionUtilisateursPanel, "GererUtilisateurs");
        mainPanel.add(historiqueVentesPanel, "HistoriqueVentes");
        mainPanel.add(approvisionnementPanel, "Approvisionnement");
        mainPanel.add(gestionAssurancesPanel, "GestionAssurances");
        mainPanel.add(comptabilitePanel, "Comptabilite");

        add(mainPanel);

        showLoginPanel();
    }

    private void showCard(String cardName) {
        cardLayout.show(mainPanel, cardName);
        this.currentCardName = cardName;

        if (Objects.equals(cardName, "Vente")) {
            ventePanel.setCurrentUser(currentUser);
            ventePanel.refreshProductSelectionTable();
        } else if (Objects.equals(cardName, "Stock")) {
            stockPanel.remplirTable();
        } else if (Objects.equals(cardName, "Info")) {
            infoPanel.updatePharmacyInfo();
        } else if (Objects.equals(cardName, "GererUtilisateurs")) {
            gestionUtilisateursPanel.refreshUsersTable();
        } else if (Objects.equals(cardName, "HistoriqueVentes")) {
            historiqueVentesPanel.refreshFacturesTable();
        } else if (Objects.equals(cardName, "Approvisionnement")) {
            approvisionnementPanel.refreshAllData();
        } else if (Objects.equals(cardName, "GestionAssurances")) {
            gestionAssurancesPanel.loadAssurances();
        } else if (Objects.equals(cardName, "Comptabilite")) {
            comptabilitePanel.refreshAccountingData();
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
        homeItem.addActionListener(e -> showCard("Welcome"));
        homeMenu.add(homeItem);
        menuBar.add(homeMenu);

        JMenu gestionMenu = new JMenu("Gestion");
        if ("admin".equalsIgnoreCase(role)) {
            JMenuItem gererUtilisateursItem = new JMenuItem("Gérer Utilisateurs");
            gererUtilisateursItem.addActionListener(e -> showCard("GererUtilisateurs"));
            gestionMenu.add(gererUtilisateursItem);

            JMenuItem approvisionnementItem = new JMenuItem("Approvisionner Stock");
            approvisionnementItem.addActionListener(e -> showCard("Approvisionnement"));
            gestionMenu.add(approvisionnementItem);

            JMenuItem gererAssurancesItem = new JMenuItem("Gérer Assurances Sociales");
            gererAssurancesItem.addActionListener(e -> showCard("GestionAssurances"));
            gestionMenu.add(gererAssurancesItem);
        }
        JMenuItem comptabiliteItem = new JMenuItem("Comptabilité");
        comptabiliteItem.addActionListener(e -> showCard("Comptabilite"));
        gestionMenu.add(comptabiliteItem);

        JMenuItem ajouterProduitItem = new JMenuItem("Ajouter Produit");
        ajouterProduitItem.addActionListener(e -> showCard("AjouterProduit"));
        gestionMenu.add(ajouterProduitItem);

        JMenuItem voirStockItem = new JMenuItem("Voir Stock");
        voirStockItem.addActionListener(e -> showCard("Stock"));
        gestionMenu.add(voirStockItem);

        JMenu venteMenu = new JMenu("Vente");
        JMenuItem effectuerVenteItem = new JMenuItem("Effectuer une Vente");
        effectuerVenteItem.addActionListener(e -> showCard("Vente"));
        venteMenu.add(effectuerVenteItem);

        JMenuItem historiqueVentesItem = new JMenuItem("Historique des Ventes");
        historiqueVentesItem.addActionListener(e -> showCard("HistoriqueVentes"));
        venteMenu.add(historiqueVentesItem);

        menuBar.add(gestionMenu);
        menuBar.add(venteMenu);

        JMenu pharmacieMenu = new JMenu("Pharmacie");
        JMenuItem infoPharmacieItem = new JMenuItem("Informations Pharmacie");
        infoPharmacieItem.addActionListener(e -> showCard("Info"));
        pharmacieMenu.add(infoPharmacieItem);
        menuBar.add(pharmacieMenu);

        JMenu logoutMenu = new JMenu("Session");
        JMenuItem logoutItem = new JMenuItem("Déconnexion");
        logoutItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Voulez-vous vraiment vous déconnecter ?", "Déconnexion",
                    JOptionPane.YES_NO_OPTION);
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
        stockPanel.remplirTable();
        infoPanel.updatePharmacyInfo();
        gestionUtilisateursPanel.refreshUsersTable();
        ventePanel.refreshProductSelectionTable();
        historiqueVentesPanel.refreshFacturesTable();
        approvisionnementPanel.refreshAllData();
        gestionAssurancesPanel.loadAssurances();
        comptabilitePanel.refreshAccountingData();
    }
}