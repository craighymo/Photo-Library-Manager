package controller;

import app.Photos;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class Search {

    @FXML
    private DatePicker fromDatePicker, toDatePicker;

    @FXML
    private TextField tagName1, tagValue1, tagName2, tagValue2;

    @FXML
    private RadioButton andRadio, orRadio;

    @FXML
    private Button searchButton, backButton, createAlbumButton;

    @FXML
    private ListView<Photo> resultList;

    @FXML
    private Label statusLabel;

    private final List<Photo> searchResults = new ArrayList<>();

    @FXML
    private void initialize() {
        resultList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Photo photo, boolean empty) {
                super.updateItem(photo, empty);
                if (empty || photo == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                String caption = (photo.getCaption() == null || photo.getCaption().isEmpty())
                        ? "(no caption)" : photo.getCaption();
                setText(caption + "  —  " + photo.getFilePath());
            }
        });
    }

    @FXML
    private void onSearch() {
        User user = getCurrentUser();
        if (user == null) {
            showError("No logged-in user.");
            return;
        }

        Set<Photo> allPhotos = new LinkedHashSet<>();
        for (Album a : user.getAlbums()) {
            allPhotos.addAll(a.getPhotos());
        }

        if (allPhotos.isEmpty()) {
            showInfo("User has no photos.");
            resultList.getItems().clear();
            searchResults.clear();
            createAlbumButton.setDisable(true);
            return;
        }

        boolean hasDateRange = fromDatePicker.getValue() != null || toDatePicker.getValue() != null;
        boolean hasTag1 = !isBlank(tagName1.getText()) && !isBlank(tagValue1.getText());
        boolean hasTag2 = !isBlank(tagName2.getText()) && !isBlank(tagValue2.getText());

        if (hasDateRange && (hasTag1 || hasTag2)) {
            showError("Search must be EITHER by date range OR by tag(s), not both.");
            return;
        }

        List<Photo> matches;

        if (hasDateRange) {
            matches = searchByDate(allPhotos);
        } else if (hasTag1) {
            matches = searchByTags(allPhotos, hasTag2);
        } else {
            showError("Please specify a date range or at least Tag 1.");
            return;
        }

        searchResults.clear();
        searchResults.addAll(matches);
        resultList.getItems().setAll(matches);
        createAlbumButton.setDisable(matches.isEmpty());

        statusLabel.setText(matches.size() + " photo(s) found.");
    }

    private List<Photo> searchByDate(Set<Photo> allPhotos) {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to   = toDatePicker.getValue();

        if (from == null && to == null) {
            showError("Please select at least one end of the date range.");
            return Collections.emptyList();
        }

        LocalDateTime fromDT = (from == null) ? LocalDateTime.MIN : from.atStartOfDay();
        LocalDateTime toDT   = (to == null)   ? LocalDateTime.MAX : to.atTime(LocalTime.MAX);

        return allPhotos.stream()
                .filter(p -> {
                    LocalDateTime t = p.getDateTime();
                    return !t.isBefore(fromDT) && !t.isAfter(toDT);
                })
                .collect(Collectors.toList());
    }

    private List<Photo> searchByTags(Set<Photo> allPhotos, boolean hasTag2) {
        String n1 = tagName1.getText().trim().toLowerCase();
        String v1 = tagValue1.getText().trim().toLowerCase();

        String n2 = hasTag2 ? tagName2.getText().trim().toLowerCase() : null;
        String v2 = hasTag2 ? tagValue2.getText().trim().toLowerCase() : null;

        return allPhotos.stream()
                .filter(p -> matchesTagCriteria(p, n1, v1, n2, v2, hasTag2))
                .collect(Collectors.toList());
    }

    private boolean matchesTagCriteria(Photo p,
                                       String n1, String v1,
                                       String n2, String v2,
                                       boolean hasTag2) {

        boolean has1 = hasTag(p, n1, v1);

        if (!hasTag2) {
            return has1;
        }

        boolean has2 = hasTag(p, n2, v2);

        if (andRadio.isSelected()) {
            return has1 && has2;
        }
        if (orRadio.isSelected()) {
            return has1 || has2;
        }

        return has1 || has2;
    }

    private boolean hasTag(Photo p, String name, String value) {
        for (Tag t : p.getTags()) {
            if (t.getName().equalsIgnoreCase(name)
                    && t.getValue().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    @FXML
    private void onCreateAlbum() {
        if (searchResults.isEmpty()) {
            showError("No search results to create an album from.");
            return;
        }

        User user = getCurrentUser();
        if (user == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Album from Results");
        dialog.setHeaderText(null);
        dialog.setContentText("New album name:");

        dialog.showAndWait().ifPresent(name -> {
            name = name.trim();
            if (name.isEmpty()) {
                showError("Album name cannot be empty.");
                return;
            }
            if (user.getAlbum(name) != null) {
                showError("An album with that name already exists.");
                return;
            }

            Album newAlbum = new Album(name);
            for (Photo p : searchResults) {
                newAlbum.addPhoto(p);  
            }
            user.getAlbums().add(newAlbum);

            try {
                UserStorage.putUser(user);
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error saving album.");
                return;
            }

            showInfo("Album '" + name + "' created with " + searchResults.size() + " photo(s).");
        });
    }

    @FXML
    private void onBack() {
        Photos.go("Albums.fxml");
    }


    private User getCurrentUser() {
        String username = Session.getUser();
        if (username == null) return null;
        return UserStorage.getUser(username);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
