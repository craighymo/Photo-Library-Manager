package view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import model.Session;

public class Albums {
    @FXML private Label welcome;

    @FXML
    private void initialize() {
        String user = Session.getUsername();
        if (welcome != null && user != null) {
            welcome.setText("Welcome, " + user);
        }
        // load albums for user here...
    }
}