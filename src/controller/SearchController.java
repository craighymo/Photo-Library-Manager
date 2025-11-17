package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

import model.Album;
import model.Photo;
import model.Tag;
import model.Session;
import model.User;
import model.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import app.Photos;

public class SearchController {

    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;

    @FXML private TextField tagType;
    @FXML private TextField tagValue;

    @FXML private ListView<Photo> resultsList;

    private List<Photo> results = new ArrayList<>();

    @FXML
    private void initialize() {

        startDate.setConverter(new StringConverter<LocalDate>() {
            @Override public String toString(LocalDate d) { return d == null ? "" : d.toString(); }
            @Override public LocalDate fromString(String s) { return s == null || s.isEmpty()? null : LocalDate.parse(s); }
        });

        endDate.setConverter(new StringConverter<LocalDate>() {
            @Override public String toString(LocalDate d) { return d == null ? "" : d.toString(); }
            @Override public LocalDate fromString(String s) { return s == null || s.isEmpty()? null : LocalDate.parse(s); }
        });

        resultsList.setCellFactory(list -> new ListCell<Photo>() {
            @Override
            protected void updateItem(Photo p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText("");
                } else {
                    String caption = p.getCaption();
                    if (caption == null || caption.isEmpty()) {
                        caption = new java.io.File(p.getFilePath()).getName();
                    }
                    setText(caption + "   (" + p.getDate().toLocalDate() + ")");
                }
            }
        });

        resultsList.setOnMouseClicked(this::openSelectedPhoto);
    }

    
    @FXML
    private void doSearch() {

        results.clear();

        LocalDate start = startDate.getValue();
        LocalDate end   = endDate.getValue();

        String type  = tagType.getText().trim().toLowerCase();
        String value = tagValue.getText().trim().toLowerCase();

        boolean searchingByDate = (start != null || end != null);
        boolean searchingByTag  = (!type.isEmpty() && !value.isEmpty());

        String username = Session.getUsername();
        User user = UserStorage.getOrCreateUser(username);

        for (Album album : user.getAlbums()) {
            for (Photo p : album.getPhotos()) {

                if (searchingByDate) {
                    LocalDate photoDate = p.getDate().toLocalDate();

                    if (start != null && photoDate.isBefore(start)) continue;
                    if (end != null   && photoDate.isAfter(end)) continue;

                    if (!results.contains(p)) results.add(p);
                    continue;
                }

                if (searchingByTag) {
                    for (Tag t : p.getTags()) {
                        if (t.getName().equalsIgnoreCase(type) &&
                            t.getValue().equalsIgnoreCase(value)) {

                            if (!results.contains(p)) results.add(p);
                        }
                    }
                }
            }
        }

        resultsList.getItems().setAll(results);
    }

  
    private void openSelectedPhoto(MouseEvent e) {
        if (e.getClickCount() == 2) {
            Photo selected = resultsList.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            Session.setCurrentPhoto(selected);
            Photos.go("PhotoView.fxml");
        }
    }

   
    @FXML
    private void createAlbumFromResults() {

        if (results.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "No search results to save as an album.").showAndWait();
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Album From Search");
        dialog.setHeaderText("Enter new album name:");
        String newName = dialog.showAndWait().orElse("").trim();

        if (newName.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Album name cannot be empty.").showAndWait();
            return;
        }

        User user = UserStorage.getOrCreateUser(Session.getUsername());

        if (user.getAlbum(newName) != null) {
            new Alert(Alert.AlertType.ERROR, "Album already exists.").showAndWait();
            return;
        }

        Album resultAlbum = new Album(newName);
        for (Photo p : results) {
            resultAlbum.addPhoto(p);
        }

        user.getAlbums().add(resultAlbum);

        try {
            UserStorage.putUser(user);
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Failed to save album.").showAndWait();
        }

        new Alert(Alert.AlertType.INFORMATION, "Album created successfully.").showAndWait();
    }

    @FXML private void goBack() { Photos.go("Albums.fxml"); }

    @FXML
    private void logout() {
        Session.clear();
        Photos.go("Login.fxml");
    }
}



