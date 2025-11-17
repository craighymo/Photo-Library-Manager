package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import app.Photos;
import model.Session;

public class LoginController {

    @FXML private TextField usernameField;

    @FXML
    private void onLogin() {
        String username = usernameField.getText().toLowerCase().trim();
        if (username.isEmpty()) return;

        Session.setCurrentUser(username);

        if (Session.isAdmin()) {
            Photos.go("Admin.fxml");
        } else {
            Photos.go("Albums.fxml");
        }
    }
}