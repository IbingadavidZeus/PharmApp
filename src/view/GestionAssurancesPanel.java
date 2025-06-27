package view;

import model.AssuranceSocial;
import model.Pharmacie;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class GestionAssurancesPanel extends JPanel {
    private Pharmacie pharmacie;
    private PharmacieDataListener dataListener;

    private JTable assurancesTable;
    private DefaultTableModel tableModel;
    private JTextField nomAssuranceField;
    private JFormattedTextField tauxPriseEnChargeField;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JLabel messageLabel;

    private final String[] columnNames = {"ID", "Nom Assurance", "Taux Prise en Charge (%)"};

    public GestionAssurancesPanel(Pharmacie pharmacie, PharmacieDataListener listener) {
        this.pharmacie = pharmacie;
        this.dataListener = listener;
        initUI();
        loadAssurances();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237)),
                "Détails de l'Assurance",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), new Color(70, 130, 180)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Nom Assurance:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        nomAssuranceField = new JTextField(25);
        formPanel.add(nomAssuranceField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Taux de Prise en Charge (%):"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;

        NumberFormat percentFormat = new DecimalFormat("#0.00");
        NumberFormatter percentFormatter = new NumberFormatter(percentFormat);
        percentFormatter.setValueClass(Double.class);
        percentFormatter.setAllowsInvalid(false);
        percentFormatter.setMinimum(0.0);
        percentFormatter.setMaximum(100.0);

        tauxPriseEnChargeField = new JFormattedTextField(percentFormatter);
        tauxPriseEnChargeField.setValue(0.00);
        formPanel.add(tauxPriseEnChargeField, gbc);
        row++;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        addButton = new JButton("Ajouter Assurance");
        addButton.setBackground(new Color(60, 179, 113));
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> addAssurance());

        updateButton = new JButton("Modifier Assurance");
        updateButton.setBackground(new Color(255, 165, 0));
        updateButton.setForeground(Color.WHITE);
        updateButton.addActionListener(e -> updateAssurance());

        deleteButton = new JButton("Supprimer Assurance");
        deleteButton.setBackground(new Color(220, 20, 60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.addActionListener(e -> deleteAssurance());

        clearButton = new JButton("Vider les champs");
        clearButton.setBackground(new Color(100, 149, 237));
        clearButton.setForeground(Color.WHITE);
        clearButton.addActionListener(e -> clearFields());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setForeground(Color.BLACK);
        formPanel.add(messageLabel, gbc);

        add(formPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                if (columnIndex == 2) return Double.class;
                return String.class;
            }
        };
        assurancesTable = new JTable(tableModel);
        assurancesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        assurancesTable.setAutoCreateRowSorter(true);
        assurancesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && assurancesTable.getSelectedRow() != -1) {
                populateFieldsFromTable();
            }
        });

        JScrollPane scrollPane = new JScrollPane(assurancesTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    void loadAssurances() {
        tableModel.setRowCount(0);
        try {
            List<AssuranceSocial> assurances = pharmacie.getAllAssurancesSocial();
            for (AssuranceSocial ass : assurances) {
                tableModel.addRow(new Object[]{
                        ass.getId_assurance(),
                        ass.getNom_assurance(),
                        ass.getTauxDePriseEnCharge() * 100
                });
            }
            setMessage("Chargement des assurances terminé.", Color.BLACK);
        } catch (SQLException e) {
            setMessage("Erreur lors du chargement des assurances: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        }
    }

    private void clearFields() {
        nomAssuranceField.setText("");
        tauxPriseEnChargeField.setValue(0.00);
        assurancesTable.clearSelection();
        setMessage("Champs réinitialisés.", Color.BLACK);
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private void populateFieldsFromTable() {
        int selectedRow = assurancesTable.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = assurancesTable.convertRowIndexToModel(selectedRow);

            int id = (int) tableModel.getValueAt(modelRow, 0);
            String nom = (String) tableModel.getValueAt(modelRow, 1);
            double tauxPourcentage = (double) tableModel.getValueAt(modelRow, 2);

            nomAssuranceField.setText(nom);
            tauxPriseEnChargeField.setValue(tauxPourcentage);

            addButton.setEnabled(false);
            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);
            setMessage("Assurance sélectionnée (ID: " + id + ").", Color.BLUE);
        } else {
            clearFields();
        }
    }

    private void addAssurance() {
        String nom = nomAssuranceField.getText().trim();
        Double tauxPourcentage = (Double) tauxPriseEnChargeField.getValue();

        if (nom.isEmpty()) {
            setMessage("Le nom de l'assurance est obligatoire.", Color.RED);
            return;
        }
        if (tauxPourcentage == null || tauxPourcentage < 0 || tauxPourcentage > 100) {
            setMessage("Le taux de prise en charge doit être entre 0 et 100.", Color.RED);
            return;
        }

        double tauxDecimal = tauxPourcentage / 100.0;
        AssuranceSocial nouvelleAssurance = new AssuranceSocial(nom, tauxDecimal);

        try {
            if (pharmacie.getAssuranceSocialByName(nom) != null) {
                setMessage("Une assurance avec ce nom existe déjà.", Color.RED);
                return;
            }

            boolean success = pharmacie.ajouterAssuranceSocial(nouvelleAssurance);
            if (success) {
                setMessage("Assurance '" + nom + "' ajoutée avec succès !", Color.GREEN);
                loadAssurances();
                clearFields();
                notifyDataChange();
            } else {
                setMessage("Erreur lors de l'ajout de l'assurance.", Color.RED);
            }
        } catch (SQLException e) {
            setMessage("Erreur de base de données: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        } catch (Exception e) {
            setMessage("Une erreur inattendue est survenue: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        }
    }

    private void updateAssurance() {
        int selectedRow = assurancesTable.getSelectedRow();
        if (selectedRow == -1) {
            setMessage("Veuillez sélectionner une assurance à modifier.", Color.ORANGE);
            return;
        }
        int modelRow = assurancesTable.convertRowIndexToModel(selectedRow);

        int id = (int) tableModel.getValueAt(modelRow, 0);
        String nom = nomAssuranceField.getText().trim();
        Double tauxPourcentage = (Double) tauxPriseEnChargeField.getValue();

        if (nom.isEmpty()) {
            setMessage("Le nom de l'assurance est obligatoire.", Color.RED);
            return;
        }
        if (tauxPourcentage == null || tauxPourcentage < 0 || tauxPourcentage > 100) {
            setMessage("Le taux de prise en charge doit être entre 0 et 100.", Color.RED);
            return;
        }

        double tauxDecimal = tauxPourcentage / 100.0;
        AssuranceSocial assuranceToUpdate = new AssuranceSocial(id, nom, tauxDecimal);

        try {
            AssuranceSocial existingAssuranceByName = pharmacie.getAssuranceSocialByName(nom);
            if (existingAssuranceByName != null && existingAssuranceByName.getId_assurance() != id) {
                setMessage("Une autre assurance avec ce nom existe déjà.", Color.RED);
                return;
            }

            boolean success = pharmacie.mettreAJourAssuranceSocial(assuranceToUpdate);
            if (success) {
                setMessage("Assurance '" + nom + "' mise à jour avec succès !", Color.GREEN);
                loadAssurances();
                clearFields();
                notifyDataChange();
            } else {
                setMessage("Erreur lors de la mise à jour de l'assurance.", Color.RED);
            }
        } catch (SQLException e) {
            setMessage("Erreur de base de données: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        } catch (Exception e) {
            setMessage("Une erreur inattendue est survenue: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        }
    }

    private void deleteAssurance() {
        int selectedRow = assurancesTable.getSelectedRow();
        if (selectedRow == -1) {
            setMessage("Veuillez sélectionner une assurance à supprimer.", Color.ORANGE);
            return;
        }
        int modelRow = assurancesTable.convertRowIndexToModel(selectedRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String nom = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer l'assurance '" + nom + "' (ID: " + id + ") ?",
                "Confirmer Suppression", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = pharmacie.supprimerAssuranceSocial(id);
                if (success) {
                    setMessage("Assurance '" + nom + "' supprimée avec succès !", Color.GREEN);
                    loadAssurances();
                    clearFields();
                    notifyDataChange();
                } else {
                    setMessage("Erreur lors de la suppression de l'assurance.", Color.RED);
                }
            } catch (SQLException e) {
                setMessage("Erreur de base de données lors de la suppression: " + e.getMessage(), Color.RED);
                e.printStackTrace();
            } catch (Exception e) {
                setMessage("Une erreur inattendue est survenue: " + e.getMessage(), Color.RED);
                e.printStackTrace();
            }
        }
    }

    private void setMessage(String msg, Color color) {
        messageLabel.setText(msg);
        messageLabel.setForeground(color);
    }

    private void notifyDataChange() {
        if (dataListener != null) {
            dataListener.onPharmacieDataChanged();
        }
    }
}