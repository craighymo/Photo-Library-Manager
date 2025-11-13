package view;

import app.Photos;
import javafx.fxml.FXML;

public class AlbumView {
    @FXML
    private void onBack() {
        Photos.go("Albums.fxml");
    }
}