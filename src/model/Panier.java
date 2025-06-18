package model;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.SQLException;

public class Panier {

    private List<LigneFacture> lignesPanier;
    private Pharmacie pharmacie;

    public Panier(Pharmacie pharmacie) {
        this.lignesPanier = new ArrayList<>();
        this.pharmacie = pharmacie;
    }

    /**
     * Ajoute un produit au panier par sa référence.
     * Vérifie la disponibilité du stock et gère l'ajout/mise à jour de la quantité.
     *
     * @param reference Référence du produit.
     * @param quantite  Quantité à ajouter.
     * @return Message d'erreur ou null si succès.
     */
    public String ajouterArticle(String reference, int quantite) {
        if (quantite <= 0) {
            return "La quantité doit être supérieure à zéro.";
        }

        // 1. Récupérer le produit depuis la base de données
        Produit produit = pharmacie.getProduitByReference(reference);
        if (produit == null) {
            return "Produit avec la référence '" + reference + "' introuvable.";
        }

        // 2. Vérifier si le produit existe déjà dans le panier
        Optional<LigneFacture> existingLigne = lignesPanier.stream()
                .filter(lf -> lf.getProduit().getId() == produit.getId())
                .findFirst();

        int quantiteActuelleDansPanier = existingLigne.map(LigneFacture::getQuantite).orElse(0);
        int quantiteDesireeTotale = quantiteActuelleDansPanier + quantite;

        // 3. Vérifier la disponibilité du stock
        if (produit.getQuantite() < quantiteDesireeTotale) {
            return "Quantité insuffisante en stock pour '" + produit.getNom() + "'. Stock disponible: "
                    + produit.getQuantite() + ".";
        }

        if (existingLigne.isPresent()) {
            LigneFacture ligne = existingLigne.get();
            ligne.setQuantite(quantiteDesireeTotale); // setQuantite recalcule le sousTotal
        } else {
            lignesPanier.add(new LigneFacture(produit, quantite));
        }
        return null; // Succès
    }

    /**
     * Ajoute un produit au panier en le recherchant par son nom.
     * Cherche le premier produit correspondant au nom.
     *
     * @param nomProduit Nom du produit.
     * @param quantite   Quantité à ajouter.
     * @return Message d'erreur ou null si succès.
     */
    public String ajouterArticleParNom(String nomProduit, int quantite) {
        if (quantite <= 0) {
            return "La quantité doit être supérieure à zéro.";
        }

        try {
            Produit foundProduct = null;
            List<Produit> allProducts = pharmacie.getProduits();
            for (Produit p : allProducts) {
                if (p.getNom().equalsIgnoreCase(nomProduit)) {
                    foundProduct = p;
                    break;
                }
            }

            if (foundProduct == null) {
                return "Produit avec le nom '" + nomProduit + "' introuvable.";
            }

            return ajouterArticle(foundProduct.getReference(), quantite);
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la recherche de produit par nom pour le panier: " + e.getMessage());
            e.printStackTrace();
            return "Erreur de base de données lors de la recherche du produit par nom.";
        }
    }

    /**
     * Retire une quantité spécifique d'un produit du panier.
     *
     * @param reference Référence du produit.
     * @param quantite  Quantité à retirer.
     * @return true si l'opération a réussi, false sinon.
     */
    public boolean retirerArticle(String reference, int quantite) {
        if (quantite <= 0) {
            System.err.println("La quantité à retirer doit être supérieure à zéro.");
            return false;
        }

        Optional<LigneFacture> ligneOpt = lignesPanier.stream()
                .filter(lf -> lf.getProduit().getReference().equalsIgnoreCase(reference))
                .findFirst();

        if (ligneOpt.isEmpty()) {
            System.out.println("Le produit " + reference + " n'est pas dans le panier.");
            return false;
        }

        LigneFacture ligne = ligneOpt.get();
        if (quantite >= ligne.getQuantite()) {
            lignesPanier.remove(ligne);
            System.out.println("Produit " + reference + " entièrement retiré du panier.");
        } else {
            ligne.setQuantite(ligne.getQuantite() - quantite);
            System.out.println("Quantité de " + reference + " réduite. Nouvelle quantité: " + ligne.getQuantite());
        }
        return true;
    }

    /**
     * Supprime complètement un produit du panier, quelle que soit la quantité.
     * 
     * @param reference Référence du produit à supprimer.
     * @return true si le produit a été supprimé, false sinon.
     */
    public boolean supprimerLigne(String reference) {
        boolean removed = lignesPanier.removeIf(lf -> lf.getProduit().getReference().equalsIgnoreCase(reference));
        if (removed) {
            System.out.println("Ligne pour le produit " + reference + " supprimée du panier.");
        } else {
            System.out.println("Produit " + reference + " non trouvé dans le panier pour suppression.");
        }
        return removed;
    }

    /**
     * Retourne la liste des lignes de facture contenues dans le panier.
     * 
     * @return List<LigneFacture> les lignes du panier.
     */
    public List<LigneFacture> getLignesPanier() {
        return new ArrayList<>(lignesPanier);
    }

    /**
     * Calcule le prix total (TTC) de tous les articles dans le panier.
     * Utilise les prix unitaires stockés dans les LigneFacture.
     * 
     * @return Le prix total du panier.
     */
    public double calculerTotalPanier() {
        return lignesPanier.stream()
                .mapToDouble(LigneFacture::getSousTotal)
                .sum();
    }

    /**
     * Vide tous les articles du panier.
     */
    public void viderPanier() {
        lignesPanier.clear();
        System.out.println("Le panier a été vidé.");
    }

    /**
     * Vérifie si le panier est vide.
     * 
     * @return true si le panier est vide, false sinon.
     */
    public boolean estVide() {
        return lignesPanier.isEmpty();
    }

    @Override
    public String toString() {
        if (lignesPanier.isEmpty()) {
            return "Le panier est vide.";
        }

        DecimalFormat df = new DecimalFormat("0.00");
        StringBuilder sb = new StringBuilder();
        sb.append("--- Contenu du Panier ---\n");
        sb.append(String.format("%-15s %-30s %-10s %-15s %-15s\n",
                "Ref.", "Nom", "Qté", "Prix U. TTC", "Sous-Total"));
        sb.append("-----------------------------------------------------------------------------------\n");

        for (LigneFacture ligne : lignesPanier) {
            Produit p = ligne.getProduit();
            double prixU = ligne.getPrixUnitaire();
            int qte = ligne.getQuantite();
            sb.append(String.format("%-15s %-30s %-10d %-15s %-15s\n",
                    p.getReference(), p.getNom(), qte, df.format(prixU), df.format(ligne.getSousTotal())));
        }
        sb.append("-----------------------------------------------------------------------------------\n");
        sb.append(String.format("%-75s %s FCFA\n", "Total provisoire:", df.format(calculerTotalPanier())));
        sb.append("-----------------------------------------------------------------------------------\n");
        return sb.toString();
    }
}