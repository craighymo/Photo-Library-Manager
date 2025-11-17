package controller;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import app.Photos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.*;

public class Albums {

    @FXML private ListView<Album> albumList;  
    @FXML private Button renameButton, deleteButton, openButton, searchButton;
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
        
        if ("stock".equalsIgnoreCase(username)) {
            initializeStockAlbum();
        }
        
        albums.setAll(user.getAlbums());
        albumList.setItems(albums);
        status.setText("Logged in as " + username);
        
        albumList.setCellFactory(list -> new ListCell<Album>() {

            private final Label titleLabel = new Label();
            private final Label datesLabel = new Label();
            private final VBox box = new VBox(2, titleLabel, datesLabel);
            private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            protected void updateItem(Album album, boolean empty) {
                super.updateItem(album, empty);

                if (empty || album == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                int count = album.getPhotoCount();

                String title = album.getName() + " (" + count + " photos)";
                titleLabel.setText(title);

                if (count == 0) {
                    datesLabel.setText("No photos yet");
                } else {
                    LocalDateTime earliest = album.getEarliestDate();
                    LocalDateTime latest   = album.getLatestDate();

                    if (earliest != null && latest != null) {
                        String start = earliest.toLocalDate().format(dateFormat);
                        String end   = latest.toLocalDate().format(dateFormat);

                        String range;
                        if (start.equals(end)) {
                            range = "Date: " + start;
                        } else {
                            range = "Dates: " + start + " to " + end;
                        }
                        datesLabel.setText(range);
                    } else {
                        datesLabel.setText("No date info");
                    }
                }

                setText(null); 
                setGraphic(box);
            }
        });
        
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
        
        Session.setCurrentAlbumName(selection.getName());
        Photos.go("AlbumView.fxml");  
    }

    @FXML
    private void onLogout() {
        Session.clear();
        Photos.go("Login.fxml");
    }
    
    @FXML
    private void onSearch() {
    	Photos.go("Search.fxml");
    };
 
    private void initializeStockAlbum() {
        Album stockAlbum = user.getAlbum("stock");
        
        if (!stockAlbum.getPhotos().isEmpty()) {
            return;
        }
        
        File folder = new File("data/stock");
        
        File[] files = folder.listFiles();
        
        if (files == null) {
            return;
        }
       
        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }
            try {
                Photo photo = new Photo(file.getAbsolutePath());
                stockAlbum.addPhoto(photo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        albums.setAll(user.getAlbums());
        persist("Initialized stock album.");
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
