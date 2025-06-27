package dao;

import model.Produit;
import java.sql.Connection; // NOUVEAU: Import pour la surcharge
import java.sql.SQLException;
import java.util.List;

public interface ProduitDAO {
    // Récupère un produit par son ID
    Produit findProduitById(int id) throws SQLException;

    // Récupère un produit par sa référence
    Produit trouverParReference(String reference) throws SQLException;

    // Récupère tous les produits
    List<Produit> getAllProduits() throws SQLException;

    // Recherche des produits par nom ou référence
    List<Produit> rechercherProduits(String critere) throws SQLException;

    // Ajoute un nouveau produit
    boolean ajouterProduit(Produit produit) throws SQLException;

    // Met à jour un produit existant (hors quantité)
    boolean mettreAJourProduit(Produit produit) throws SQLException;

    // Met à jour la quantité en stock d'un produit
    boolean mettreAJourQuantite(String reference, int nouvelleQuantite) throws SQLException;

    // NOUVEAU ET CRUCIAL: Met à jour la quantité en stock d'un produit dans le cadre d'une transaction existante
    boolean mettreAJourQuantite(Connection conn, String reference, int nouvelleQuantite) throws SQLException;

    // Supprime un produit par sa référence
    boolean supprimerProduit(String reference) throws SQLException;
}
