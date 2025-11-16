package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.*;
import java.io.File;
import java.time.format.DateTimeFormatter;

public class PhotoView {

    @FXML private ImageView imageView;
    @FXML private Label captionLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label status;
    @FXML private ListView<Tag> tagList; 
    @FXML private Button addTagButton;
    @FXML private Button deleteTagButton;
    
    private Photo photo;
    private User user;

    @FXML
    private void initialize() {
    	String username = Session.getUsername();
    	photo = Session.getCurrentPhoto();
        if (username == null || photo == null) {
            status.setText("No photo selected.");
            return;
        }
        
        user = UserStorage.getOrCreateUser(username);

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
        
        tagList.getItems().setAll(photo.getTags());
        
        tagList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            deleteTagButton.setDisable(!hasSelection);
        });
    }
    
    @FXML
    private void onAddTag() {
        if (photo == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Tag");
        dialog.setHeaderText("Add a tag in the form name=value");
        dialog.setContentText("Tag:");

        dialog.showAndWait().ifPresent(input -> {
            String trimmed = input.trim();
            if (trimmed.isEmpty()) {
                alert("Tag cannot be empty.");
                return;
            }

            int eqIndex = trimmed.indexOf('=');
            if (eqIndex <= 0 || eqIndex == trimmed.length() - 1) {
                alert("Tag must be in the form name=value.");
                return;
            }

            String name = trimmed.substring(0, eqIndex).trim();
            String value = trimmed.substring(eqIndex + 1).trim();
            if (name.isEmpty() || value.isEmpty()) {
                alert("Tag name and value cannot be empty.");
                return;
            }

            Tag newTag = new Tag(name, value);

            if (photo.getTags().contains(newTag)) {
                alert("This tag already exists for this photo.");
                return;
            }

            photo.addTag(newTag);
            tagList.getItems().add(newTag);
            persist("Added tag: " + newTag.toString());
        });
    }
    
    @FXML
    private void onDeleteTag() {
        if (photo == null) {
            return;
        }

        Tag selected = tagList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        if (!confirm("Delete tag '" + selected.toString() + "'?")) {
            return;
        }

        photo.removeTag(selected);
        tagList.getItems().remove(selected);
        persist("Deleted tag: " + selected.toString());
    }
    
    

    @FXML
    private void onClose() {
        Stage stage = (Stage) imageView.getScene().getWindow();
        stage.close();
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