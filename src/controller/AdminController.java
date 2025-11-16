package controller;

import app.Photos;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import model.User;
import model.UserStorage;

import java.io.IOException;

public class AdminController {

    @FXML
    private ListView<String> userList;

    @FXML
    private TextField newUserField;

    @FXML
    private Button addUserButton;

    @FXML
    private Button deleteUserButton;

    @FXML
    private Button logoutButton;

    @FXML
    private void initialize() {
        refreshList();
    }

    private void refreshList() {
        userList.getItems().clear();
        for (String name : UserStorage.getAllUsers().keySet()) {
            userList.getItems().add(name);
        }
    }

    @FXML
    private void addUser() {
        String name = newUserField.getText().trim();

        if (name.isEmpty()) {
            showError("Username cannot be empty.");
            return;
        }

        if (UserStorage.getUser(name) != null) {
            showError("User already exists.");
            return;
        }

        try {
            User newUser = new User(name);
            UserStorage.putUser(newUser);
            refreshList();
            newUserField.clear();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error saving user.");
        }
    }

    @FXML
    private void deleteUser() {
        String selected = userList.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Select a user first.");
            return;
        }

        if (selected.equalsIgnoreCase("admin") || selected.equalsIgnoreCase("stock")) {
            showError("Cannot delete admin or stock user.");
            return;
        }

        try {
            UserStorage.getAllUsers().remove(selected);
            UserStorage.save();
            refreshList();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error deleting user.");
        }
    }

    @FXML
    private void logout() {
        Photos.go("Login.fxml");
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
