package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.Tag;
import model.Photo;
import model.Session;
import java.io.File;
import java.time.format.DateTimeFormatter;

public class PhotoView {

    @FXML private ImageView imageView;
    @FXML private Label captionLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label status;
    @FXML private ListView<Tag> tagList; 

    @FXML
    private void initialize() {
    	Photo photo = Session.getCurrentPhoto();
        if (photo == null) {
            status.setText("No photo selected.");
            return;
        }

        // uses filename if theres no caption
        String caption = photo.getCaption();
        if (caption == null || caption.isEmpty()) {
            File file = new File(photo.getFilePath());
            caption = file.getName();
        }
        captionLabel.setText(caption);


        if (photo.getDate() != null) {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formatted = photo.getDate().format(format);
            dateTimeLabel.setText(formatted);
        } else {
            dateTimeLabel.setText("");
        }

        File file = new File(photo.getFilePath());
        if (file.exists()) {
            Image image = new Image(file.toURI().toString(), true);
            imageView.setImage(image);
            status.setText("Viewing photo");
        } else {
            imageView.setImage(null);
            status.setText("File not found: " + photo.getFilePath());
        }
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) imageView.getScene().getWindow();
        stage.close();
    }
}