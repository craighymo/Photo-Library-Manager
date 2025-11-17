package controller;

import app.Photos;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import model.*;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.io.File;

public class AlbumView {

    @FXML private Label albumNameLabel;
    @FXML private Label photoCountLabel;
    @FXML private Label status;
    
    @FXML private ListView<Photo> photoList;
    
    @FXML private Button removeButton;
    @FXML private Button editCaptionButton;
    @FXML private Button openPhotoButton;
    @FXML private Button copyButton;
    @FXML private Button moveButton;

    private User user;
    private Album album;

    @FXML
    private void initialize() {
        String username = Session.getUsername();
        String albumName = Session.getCurrentAlbumName();

        if (username == null || albumName == null) {
            status.setText("No album selectected.");
            disableAll();
            return;
        }

        user = UserStorage.getOrCreateUser(username);
        album = user.getAlbum(albumName);

        if (album == null) {
            status.setText("Album '" + albumName + "' not found.");
            disableAll();
            return;
        }

        albumNameLabel.setText(album.getName());
        updatePhotoCount();

        photoList.setItems(FXCollections.observableArrayList(album.getPhotos()));

        photoList.setCellFactory(list -> new ListCell<Photo>() {

            private final ImageView thumbNail = new ImageView();
            private final Label captionLabel = new Label();
            private final Label dateLabel = new Label();
            private final VBox textBox = new VBox(2, captionLabel, dateLabel);
            private final HBox box = new HBox(10, thumbNail, textBox);
            private final DateTimeFormatter format =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            {
                thumbNail.setFitWidth(80);
                thumbNail.setFitHeight(80);
                thumbNail.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Photo p, boolean empty) {
                super.updateItem(p, empty);

                if (empty || p == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String caption = p.getCaption();
                if (caption == null || caption.isEmpty()) {
                    caption = Path.of(p.getFilePath()).getFileName().toString();
                }
                captionLabel.setText(caption);

                if (p.getDate() != null) {
                    dateLabel.setText(p.getDate().format(format));
                } else {
                    dateLabel.setText("");
                }

                File f = new File(p.getFilePath());
                if (f.exists()) {
                    Image img = new Image(f.toURI().toString(), 80, 80, true, true);
                    thumbNail.setImage(img);
                } else {
                    thumbNail.setImage(null);
                }

                setText(null);   
                setGraphic(box);
            }
        });
        
        photoList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            boolean selected = newV != null;
            removeButton.setDisable(!selected);
            editCaptionButton.setDisable(!selected);
            openPhotoButton.setDisable(!selected);
            copyButton.setDisable(!selected);
            moveButton.setDisable(!selected);
        });
        
        photoList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                onOpenPhoto();
            }
        });

        status.setText("Viewing album: " + album.getName());
    }

    @FXML
    private void onBack() {
        Photos.go("Albums.fxml");
    }

    @FXML
    private void onAddPhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Photo");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp")
        ); 

        File selected = fc.showOpenDialog(null);
        if (selected == null) return;

        try {
            Photo p = new Photo(selected.getAbsolutePath());

            boolean exists = album.getPhotos().stream()
                    .anyMatch(photo -> photo.getFilePath().equals(p.getFilePath()));
            if (exists) {
                alert("This photo is already in the album.");
                return;
            }

            album.addPhoto(p);
            photoList.getItems().add(p);
            updatePhotoCount();
            persist("Added photo: " + p.toString());
        } catch (Exception e) {
            e.printStackTrace();
            alert("Unable to load photo.");
        }
    }

    @FXML
    private void onRemovePhoto() {
        Photo select = photoList.getSelectionModel().getSelectedItem();
        if (select == null) return;

        if (!confirm("Remove photo from the album?")) return;

        album.removePhoto(select);
        photoList.getItems().remove(select);
        updatePhotoCount();
        persist("Removed photo.");
    }

    @FXML
    private void onEditCaption() {
        Photo select = photoList.getSelectionModel().getSelectedItem();
        if (select == null) return;

        TextInputDialog dialog = new TextInputDialog(select.getCaption());
        dialog.setHeaderText("Edit Caption");
        dialog.setContentText("Caption:");
        dialog.setTitle("Photo Caption");

        dialog.showAndWait().ifPresent(caption -> {
            select.setCaption(caption);
            photoList.refresh();
            persist("Updated caption.");
        });
    }
    
    @FXML
    private void onOpenPhoto() {
        Photo select = photoList.getSelectionModel().getSelectedItem();
        if (select == null) return;

        Session.setCurrentPhoto(select);
        Photos.popup("PhotoView.fxml");
    }
    
    private Album targetAlbum(String title) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        dialog.setContentText("Enter album name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return null;                   
        }

        String name = result.get().trim();
        if (name.isEmpty()) {
            alert("Album name is empty.");
            return null;
        }

        Album target = user.getAlbum(name);
        if (target == null) {
            alert("Album: " + name + " not found.");
            return null;
        }

        if (target == album) {
            alert("That's the current album...");
            return null;
        }

        return target;
    }
    
    @FXML
    private void onCopyPhoto() {
        Photo selected = photoList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Album target = targetAlbum("Copy Photo");
        if (target == null) {
            return; 
        }

        target.addPhoto(selected);
        persist("Copied photo to album: " + target.getName());
    }
    
    @FXML
    private void onMovePhoto() {
        Photo selected = photoList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Album target = targetAlbum("Move Photo");
        if (target == null) {
            return;
        }

        target.addPhoto(selected);
        
        album.removePhoto(selected);
        photoList.getItems().remove(selected);
        updatePhotoCount();

        persist("Moved photo to album: " + target.getName());
    }

    private void disableAll() {
        if (removeButton != null) removeButton.setDisable(true);
        if (editCaptionButton != null) editCaptionButton.setDisable(true);
        if (photoList != null) photoList.setDisable(true);
    }

    private void updatePhotoCount() {
        if (photoCountLabel != null && album != null) {
            photoCountLabel.setText(Integer.toString(album.getPhotoCount()));
        }
    }
    
    private void persist(String message) {
        try {
            UserStorage.putUser(user);
            status.setText(message);
        } catch (Exception e) {
            e.printStackTrace();
            alert("Save failed.");
        }
    }

    private void alert(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }

    private boolean confirm(String message) {
        return new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL)
                .showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}