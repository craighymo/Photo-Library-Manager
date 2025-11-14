package view;

import java.util.Optional;

import app.Photos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.*;

// reminder set all FXML to java FX 21

public class Albums {

    @FXML private ListView<Album> albumList;  
    @FXML private Button renameButton, deleteButton, openButton;
    @FXML private Label status;

    private User user;
    private final ObservableList<Album> albums = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
    	String username = Session.getUsername();
    	if (username == null) {
            status.setText("Not logged in");
            return;
        }
        user = UserStorage.getOrCreateUser(username);
   
        // albums.setAll("stuff", "other stuff", "sssstuff"); // stub data
        albums.setAll(user.getAlbums());
        albumList.setItems(albums);
        status.setText("Logged in as " + username);
        
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
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String name = result.get().trim();
        if (name.isEmpty()) {
            alert("Name cannot be empty.");
            return;
        }
        if (user.getAlbum(name) != null) {  
            alert("An album with that name already exists.");
            return;
        }
        user.addAlbum(name);
        albums.setAll(user.getAlbums());
        persist("Created: " + name);
        
        Album created = user.getAlbum(name);
        if (created != null) {
        	albumList.getSelectionModel().select(created);
        }
    }
 

    @FXML
    private void onRename() {
        Album selection = albumList.getSelectionModel().getSelectedItem();
        if (selection == null) return;
        String oldName = selection.getName();
        TextInputDialog dialog = new TextInputDialog(oldName);
        dialog.setHeaderText("Rename album");
        dialog.setContentText("New name:");
        dialog.setTitle("Rename Album");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String newName = result.get().trim();
        if (newName.isEmpty()) {
            alert("Name is empty.");
            return;
        }
        if (!oldName.equals(newName) && user.getAlbum(newName) != null) {
            alert("An album with that name already exists.");
            return;
        }
        selection.setName(newName);
        albumList.refresh();
        persist("Renamed to: " + newName);
    }

    @FXML
    private void onDelete() {
    	Album selection = albumList.getSelectionModel().getSelectedItem();
        if (selection == null) return;
        if (!confirm("Delete album '" + selection + "'?")) { return; }
        String name = selection.getName();
        user.deleteAlbum(name);
        albums.remove(selection);
        persist("Deleted: " + name);
        
    }

    @FXML
    private void onOpen() {
        Album selection = albumList.getSelectionModel().getSelectedItem();
        if (selection == null) return;

        Photos.go("AlbumView.fxml");  
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
    
    private void persist(String statusMsg) {
        try {
            UserStorage.putUser(user);   
            status.setText(statusMsg);
        } catch (Exception e) {
            e.printStackTrace();
            alert("Save failed. See console for details.");
        }
    }
}