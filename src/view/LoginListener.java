package view;

import model.Utilisateur; 

public interface LoginListener {
    void onLoginSuccess(Utilisateur utilisateur);
}