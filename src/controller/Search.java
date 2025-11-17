package controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import model.*;

import app.Photos;

public class Search {

    @FXML private Label statusLabel;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private TextField tagType1Field;
    @FXML private TextField tagValue1Field;
    @FXML private TextField tagType2Field;
    @FXML private TextField tagValue2Field;

    @FXML private RadioButton andRadio;
    @FXML private RadioButton orRadio;

    @FXML private ListView<Photo> resultsList;

    private List<Photo> results = new ArrayList<>();

   
    @FXML
    private void onSearch() {

        results.clear();
        resultsList.getItems().clear();
        statusLabel.setText("");

        User user = UserStorage.getUser(Session.getUsername());
        if (user == null) {
            statusLabel.setText("No user loaded");
            return;
        }

        List<Album> albums = user.getAlbums();
        if (albums == null || albums.isEmpty()) {
            statusLabel.setText("No albums found.");
            return;
        }

        boolean dateSearchUsed =
                startDatePicker.getValue() != null ||
                endDatePicker.getValue() != null;

        boolean tagSearchUsed =
                !tagType1Field.getText().isEmpty() ||
                !tagValue1Field.getText().isEmpty() ||
                !tagType2Field.getText().isEmpty() ||
                !tagValue2Field.getText().isEmpty();

        if (dateSearchUsed && tagSearchUsed) {
            showError("Choose EITHER date search OR tag search.");
            return;
        }

        if (!dateSearchUsed && !tagSearchUsed) {
            showError("Enter search criteria.");
            return;
        }

        if (dateSearchUsed) {
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();

            for (Album a : albums) {
                for (Photo p : a.getPhotos()) {

                    LocalDate pd = p.getDate().toLocalDate();

                    boolean afterStart = (start == null) || (!pd.isBefore(start));
                    boolean beforeEnd = (end == null) || (!pd.isAfter(end));

                    if (afterStart && beforeEnd && !results.contains(p)) {
                        results.add(p);
                    }
                }
            }

            resultsList.getItems().setAll(results);
            statusLabel.setText(results.size() + " photos found.");
            return;
        }

        String type1 = tagType1Field.getText();
        String value1 = tagValue1Field.getText();
        String type2 = tagType2Field.getText();
        String value2 = tagValue2Field.getText();

        boolean useAND = andRadio.isSelected();
        boolean useOR = orRadio.isSelected();

        boolean usingSecondTag = !type2.isEmpty() && !value2.isEmpty();

        for (Album a : albums) {
            for (Photo p : a.getPhotos()) {

                boolean match1 = false;
                boolean match2 = false;

                for (Tag t : p.getTags()) {
                    if (t.getName().equalsIgnoreCase(type1) &&
                        t.getValue().equalsIgnoreCase(value1)) {
                        match1 = true;
                        break;
                    }
                }

                if (usingSecondTag) {
                    for (Tag t : p.getTags()) {
                        if (t.getName().equalsIgnoreCase(type2) &&
                            t.getValue().equalsIgnoreCase(value2)) {
                            match2 = true;
                            break;
                        }
                    }
                }

                boolean add = false;

                if (!usingSecondTag) {
                    if (match1) add = true;
                } else {
                    if (useAND && match1 && match2) add = true;
                    if (useOR && (match1 || match2)) add = true;
                }

                if (add && !results.contains(p)) {
                    results.add(p);
                }
            }
        }

        resultsList.getItems().setAll(results);
        statusLabel.setText(results.size() + " photos found.");
    }

   
    @FXML
    private void onResultDoubleClick(MouseEvent e) {
        if (e.getClickCount() == 2) {
            Photo selected = resultsList.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            Session.setCurrentPhoto(selected);
            Photos.popup("PhotoView.fxml");
        }
    }

    
    @FXML
    private void onCreateAlbumFromResults() {

        if (results.isEmpty()) {
            showError("No search results to save.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Album From Results");
        dialog.setHeaderText("Enter a name for the new album:");
        dialog.setContentText("Album name:");

        dialog.showAndWait().ifPresent(name -> {

            if (name.isEmpty()) {
                showError("Album name cannot be empty.");
                return;
            }

            User user = UserStorage.getUser(Session.getUsername());
            if (user == null) {
                showError("No current user loaded.");
                return;
            }

            if (user.getAlbum(name) != null) {
                showError("You already have an album named \"" + name + "\".");
                return;
            }

            Album newAlbum = new Album(name);
            for (Photo p : results) newAlbum.addPhoto(p);

            user.getAlbums().add(newAlbum);

            try {
                UserStorage.save();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            statusLabel.setText("Album \"" + name + "\" created.");
        });
    }

    
    @FXML
    private void onBack() {
        Photos.go("Albums.fxml");
    }

    @FXML
    private void onLogout() {
        Session.clear();
        Photos.go("Login.fxml");
    }

    
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
