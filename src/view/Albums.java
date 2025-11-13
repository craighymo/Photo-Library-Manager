package view;

import app.Photos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Session;
// import model.User;
// import model.UserStorage;
// import model.Album;

public class Albums {

    @FXML private ListView<String> albumList;  
    @FXML private Button renameButton, deleteButton, openButton;
    @FXML private Label status;

    // private String username;
    private final ObservableList<String> albums = FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        // storage is gonna go here
        
        albums.setAll("stuff", "other stuff", "sssstuff"); // stub data

        albumList.setItems(albums);

        // Enable/disable buttons when selectionection changes
        albumList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean selected = newVal != null;
            renameButton.setDisable(!selected);
            deleteButton.setDisable(!selected);
            openButton.setDisable(!selected);
        });
    }

    @FXML
    private void onNew() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Create new album");
        dialog.setContentText("Album name:");
        dialog.setTitle("New Album");
        dialog.showAndWait().ifPresent(name -> {
            if (name.isBlank()) {
                alert("Name cannot be empty.");
                return;
            }
            if (albums.contains(name)) {
                alert("An album with that name already exists.");
                return;
            }
            albums.add(name);
            status.setText("Created: " + name);
            // need to persist in UserStorage for rename
        });
    }

    @FXML
    private void onRename() {
        String selection = albumList.getSelectionModel().getSelectedItem();
        if (selection == null) return;
        TextInputDialog dialog = new TextInputDialog(selection);
        dialog.setHeaderText("Rename album");
        dialog.setContentText("New name:");
        dialog.setTitle("Rename Album");
        dialog.showAndWait().ifPresent(name -> {
            if (name.isBlank()) { 
            	alert("Name is empty."); 
            	return; 
            }
            if (!selection.equals(name) && albums.contains(name)) {
                alert("An album with that name already exists."); 
                return;
            }
            int index = albums.indexOf(selection);
            albums.set(index, name);
            status.setText("Renamed to: " + name);
        });
    }

    @FXML
    private void onDelete() {
        String selection = albumList.getSelectionModel().getSelectedItem();
        if (selection == null) return;
        if (confirm("Delete album '" + selection + "'?")) {
            albums.remove(selection);
            status.setText("Deleted: " + selection);
        }
    }

    @FXML
    private void onOpen() {
        String selection = albumList.getSelectionModel().getSelectedItem();
        if (selection == null) return;

        // gonna pass an album object here
  
        // TEST: Current.setAlbumName(selection);

        Photos.go("AlbumView.fxml");  // create a basic placeholder fxml next
    }

    @FXML
    private void onLogout() {
        Session.clear();
        Photos.go("Login.fxml");
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    private boolean confirm(String msg) {
        return new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL)
                .showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}