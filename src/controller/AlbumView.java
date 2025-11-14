package controller;

import app.Photos;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.*;

import java.io.File;

public class AlbumView {

    @FXML private Label albumNameLabel;
    @FXML private Label photoCountLabel;
    @FXML private Label status;

    @FXML private ListView<Photo> photoList;

    @FXML private Button removeButton;
    @FXML private Button editCaptionButton;

    private User user;
    private Album album;

    @FXML
    private void initialize() {
        String username = Session.getUsername();
        String albumName = Session.getCurrentAlbumName();

        if (username == null || albumName == null) {
            status.setText("No album selectected.");
            return;
        }
    }

    @FXML
    private void onBack() {
        Photos.go("Albums.fxml");
    }

    private void persist(String statusMsg) {
        try {
            UserStorage.putUser(user);
            status.setText(statusMsg);
        } catch (Exception e) {
            e.printStackTrace();
            alert("Save failed.");
        }
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    private boolean confirm(String msg) {
        return new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL)
                .showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}