package controller;

import app.Photos;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Session;
import model.User;
import model.UserStorage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private Button loginButton;

    private Stage stage;

    public void setStage(Stage s) {
        this.stage = s;
    }

    @FXML
    public void onLogin() {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            showError("Username cannot be empty.");
            return;
        }

        if (username.equalsIgnoreCase("admin")) {
            Session.setUser(null);
            Photos.go("Admin.fxml");
            return;
        }

        User user = UserStorage.getUser(username);
        if (user == null) {
            showError("User not found.");
            return;
        }

        Session.setUser(username);
        Photos.go("Albums.fxml");
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
