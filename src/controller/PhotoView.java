package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.Tag;

public class PhotoView {

    @FXML private ImageView imageView;
    @FXML private Label captionLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label status;
    @FXML private ListView<Tag> tagList; 

    @FXML
    private void initialize() {
        status.setText("Photo viewer");
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) imageView.getScene().getWindow();
        stage.close();
    }
}