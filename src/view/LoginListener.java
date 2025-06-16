package view;

import model.Utilisateur; 

/**
 * Interface pour écouter les événements de connexion réussie.
 * Typiquement implémentée par la MainFrame pour gérer la transition après connexion.
 */
public interface LoginListener {
    /**
     * Cette méthode est appelée lorsqu'un utilisateur se connecte avec succès.
     * @param utilisateur L'objet Utilisateur représentant l'utilisateur qui vient de se connecter.
     */
    void onLoginSuccess(Utilisateur utilisateur);
}