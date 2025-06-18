package view;

import model.Pharmacie;
import model.Utilisateur;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class GestionUtilisateursPanel extends JPanel {
    private Pharmacie pharmacie;
    private PharmacieDataListener dataListener;

    // Composants du formulaire

    private JTextField idField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JLabel messageLabel;

    // Tableau des utilisateurs

    private JTable usersTable;
    private DefaultTableModel tableModel;
    private final String[] columnNames = { "ID", "Nom d'utilisateur", "Rôle" };

    public GestionUtilisateursPanel(Pharmacie pharmacie, PharmacieDataListener listener) {
        this.pharmacie = pharmacie;
        this.dataListener = listener;
        initUI();
        refreshUsersTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // --- Panel Formulaire (Nord) ---

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Gestion des Utilisateurs"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // ID (lecture seule)

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("ID Utilisateur:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        idField = new JTextField(15);
        idField.setEditable(false);
        formPanel.add(idField, gbc);
        row++;

        // Nom d'utilisateur

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Nom d'utilisateur:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        usernameField = new JTextField(15);
        formPanel.add(usernameField, gbc);
        row++;

        // Mot de passe

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Mot de passe:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField, gbc);
        row++;

        // Rôle

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Rôle:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        roleComboBox = new JComboBox<>(new String[] { "vendeur", "admin" });
        formPanel.add(roleComboBox, gbc);
        row++;

        // Boutons d'action

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        addButton = new JButton("Ajouter");
        addButton.addActionListener(_ -> addUser());
        updateButton = new JButton("Modifier");
        updateButton.addActionListener(_ -> updateUser());
        deleteButton = new JButton("Supprimer");
        deleteButton.addActionListener(_ -> deleteUser());
        clearButton = new JButton("Vider Champs");
        clearButton.addActionListener(_ -> clearFields());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);
        row++;

        // Message Label

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        messageLabel = new JLabel(" ");
        messageLabel.setForeground(Color.RED);
        formPanel.add(messageLabel, gbc);

        add(formPanel, BorderLayout.NORTH);

        // --- Panel Tableau (Centre) ---

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        usersTable = new JTable(tableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        usersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && usersTable.getSelectedRow() != -1) {
                populateFieldsFromSelectedRow();
            }
        });

        add(new JScrollPane(usersTable), BorderLayout.CENTER);
    }

    public void refreshUsersTable() {
        tableModel.setRowCount(0);
        try {
            List<Utilisateur> users = pharmacie.getAllUtilisateurs();
            for (Utilisateur user : users) {
                tableModel.addRow(new Object[] { user.getId(), user.getNomUtilisateur(), user.getRole() });
            }
            messageLabel.setText("Tableau des utilisateurs rafraîchi.");
            messageLabel.setForeground(Color.BLACK);
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors du chargement des utilisateurs: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace();
        }
        clearFields();
    }

    private void populateFieldsFromSelectedRow() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow != -1) {
            idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
            usernameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
            roleComboBox.setSelectedItem(tableModel.getValueAt(selectedRow, 2).toString());
            passwordField.setText("");
            messageLabel.setText(" ");
        }
    }

    private void clearFields() {
        idField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        roleComboBox.setSelectedIndex(0);
        messageLabel.setText(" ");
        usersTable.clearSelection();
    }

    private void addUser() {
        String username = usernameField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        String plainPassword = new String(passwordChars);
        String role = (String) roleComboBox.getSelectedItem();

        // Efface immédiatement le tableau de caractères du mot de passe pour la
        // sécurité

        Arrays.fill(passwordChars, ' ');

        if (username.isEmpty() || plainPassword.isEmpty() || role == null) {
            messageLabel.setText("Tous les champs (Nom d'utilisateur, Mot de passe, Rôle) sont obligatoires.");
            messageLabel.setForeground(Color.RED);
            return;
        }

        try {
            Utilisateur newUser = new Utilisateur(username, plainPassword, role);

            boolean success = pharmacie.ajouterUtilisateur(newUser, plainPassword);

            if (success) {
                messageLabel.setText("Utilisateur '" + username + "' ajouté avec succès.");
                messageLabel.setForeground(Color.GREEN);
                refreshUsersTable();
                if (dataListener != null) {
                    dataListener.onPharmacieDataChanged();
                }
            } else {
                messageLabel
                        .setText("Échec de l'ajout de l'utilisateur. Peut-être que le nom d'utilisateur existe déjà.");
                messageLabel.setForeground(Color.RED);
            }
        } catch (SQLException e) {
            messageLabel.setText("Erreur SQL lors de l'ajout de l'utilisateur: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace();
        } catch (Exception e) {
            messageLabel.setText("Une erreur inattendue est survenue: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace();
        }
    }

    private void updateUser() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            messageLabel.setText("Veuillez sélectionner un utilisateur à modifier.");
            messageLabel.setForeground(Color.ORANGE);
            return;
        }

        try {
            int id = Integer.parseInt(idText);
            String username = usernameField.getText().trim();
            char[] passwordChars = passwordField.getPassword();
            String newPlainPassword = new String(passwordChars);
            String role = (String) roleComboBox.getSelectedItem();

            Arrays.fill(passwordChars, ' ');

            if (username.isEmpty() || role == null) {
                messageLabel.setText("Nom d'utilisateur et Rôle sont obligatoires.");
                messageLabel.setForeground(Color.RED);
                return;
            }

            Utilisateur updatedUser = new Utilisateur(id, username, null, role);
            boolean success = pharmacie.mettreAJourUtilisateur(updatedUser, newPlainPassword);

            if (success) {
                messageLabel.setText("Utilisateur '" + username + "' mis à jour avec succès.");
                messageLabel.setForeground(Color.GREEN);
                refreshUsersTable();
                if (dataListener != null) {
                    dataListener.onPharmacieDataChanged();
                }
            } else {
                messageLabel.setText("Échec de la mise à jour de l'utilisateur.");
                messageLabel.setForeground(Color.RED);
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("ID utilisateur invalide.");
            messageLabel.setForeground(Color.RED);
        } catch (SQLException e) {
            messageLabel.setText("Erreur SQL lors de la mise à jour de l'utilisateur: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace();
        } catch (Exception e) {
            messageLabel.setText("Une erreur inattendue est survenue: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace();
        }
    }

    private void deleteUser() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            messageLabel.setText("Veuillez sélectionner un utilisateur à supprimer.");
            messageLabel.setForeground(Color.ORANGE);
            return;
        }

        try {
            int id = Integer.parseInt(idText);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Êtes-vous sûr de vouloir supprimer l'utilisateur ID: " + id + " (" + usernameField.getText()
                            + ") ?",
                    "Confirmer la suppression", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = pharmacie.supprimerUtilisateur(id);

                if (success) {
                    messageLabel.setText("Utilisateur ID: " + id + " supprimé avec succès.");
                    messageLabel.setForeground(Color.GREEN);
                    refreshUsersTable();
                    if (dataListener != null) {
                        dataListener.onPharmacieDataChanged();
                    }
                } else {
                    messageLabel.setText("Échec de la suppression de l'utilisateur.");
                    messageLabel.setForeground(Color.RED);
                }
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("ID utilisateur invalide.");
            messageLabel.setForeground(Color.RED);
        } catch (SQLException e) {
            messageLabel.setText("Erreur SQL lors de la suppression de l'utilisateur: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace();
        } catch (Exception e) {
            messageLabel.setText("Une erreur inattendue est survenue: " + e.getMessage());
            messageLabel.setForeground(Color.RED);
            e.printStackTrace();
        }
    }
}
