package dao;

import model.Produit;

import java.sql.SQLException;
import java.util.List;

public interface ProduitDAO {
    boolean ajouterProduit(Produit produit) throws SQLException;
    Produit trouverParReference(String reference) throws SQLException;
    Produit trouverParId(int id) throws SQLException; // Renommé de trouverParId
    List<Produit> getAllProduits() throws SQLException;
    boolean mettreAJourProduit(Produit produit) throws SQLException;
    boolean supprimerProduit(String reference) throws SQLException;
    boolean mettreAJourQuantite(String reference, int nouvelleQuantite) throws SQLException;
    List<Produit> rechercherProduits(String critere) throws SQLException;
    
    // Ajout de la méthode findProduitById pour LigneFactureDAO
    Produit findProduitById(int id) throws SQLException; 
}