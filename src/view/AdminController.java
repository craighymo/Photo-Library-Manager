package view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import app.Photos;
import model.Session;

public class AdminController {
    @FXML private Label welcome;

    public void initUsername(String username) {
        if (welcome != null) welcome.setText("Admin: " + username);
    }

    @FXML
    private void onLogout() {
        Session.clear();
        Photos.go("Login.fxml");
    }
}