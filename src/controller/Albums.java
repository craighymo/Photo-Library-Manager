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

/**
 * The controller for the Albums screen. It displays all albums of the logged in
 * user, with their photo counts and date ranges. Users can create, rename,
 * delete, and open albums.
 *
 * <p>
 * The Albums controller also handles logout navigation and redirects to the
 * search page. If the logged in user is the "stock" user, the controller
 * initializes the stock album.
 * </p>
 *
 * <p>
 * All album changes are persisted using {@link UserStorage}.
 * </p>
 *
 * @author Craig Hymowitz
 */
public class Albums {

	@FXML
	private ListView<Album> albumList;
	@FXML
	private Button renameButton, deleteButton, openButton, searchButton;
	@FXML
	private Label status;

	private User user;
	private final ObservableList<Album> albums = FXCollections.observableArrayList();

	/**
	 * Initializes the albums view. Loads the current user, the album list, handles
	 * the album details, and sets up UI selection behavior.
	 */
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
					LocalDateTime latest = album.getLatestDate();

					if (earliest != null && latest != null) {
						String start = earliest.toLocalDate().format(dateFormat);
						String end = latest.toLocalDate().format(dateFormat);

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

		albumList.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				onOpen();
			}
		});
	}

	/**
	 * Handles the creation of a new album with a text input dialog. Checks that the
	 * name is valid and updates storage.
	 */
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

	/**
	 * Renames the selected album.
	 */
	@FXML
	private void onRename() {
		Album selection = albumList.getSelectionModel().getSelectedItem();

		if (selection == null)
			return;

		String oldName = selection.getName();

		TextInputDialog dialog = new TextInputDialog(oldName);
		dialog.setHeaderText("Rename album");
		dialog.setContentText("New name:");
		dialog.setTitle("Rename Album");

		Optional<String> result = dialog.showAndWait();
		if (result.isEmpty())
			return;

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

	/**
	 * Deletes the selected album.
	 */
	@FXML
	private void onDelete() {
		Album selection = albumList.getSelectionModel().getSelectedItem();

		if (selection == null)
			return;

		if (!confirm("Delete album '" + selection + "'?")) {
			return;
		}

		String name = selection.getName();

		user.deleteAlbum(name);
		albums.remove(selection);
		persist("Deleted: " + name);
	}

	/**
	 * Opens the selected album and switches screens to AlbumView.
	 */
	@FXML
	private void onOpen() {
		Album selection = albumList.getSelectionModel().getSelectedItem();
		if (selection == null)
			return;

		Session.setCurrentAlbumName(selection.getName());
		Photos.go("AlbumView.fxml");
	}

	/**
	 * Logs the current user out, clears the session, and returns to login screen.
	 */
	@FXML
	private void onLogout() {
		Session.clear();
		Photos.go("Login.fxml");
	}

	/**
	 * Switches to the Search screen.
	 */
	@FXML
	private void onSearch() {
		Photos.go("Search.fxml");
	};

	/**
	 * Loads stock photos into the stock album if it is empty. Only executed for the
	 * if the "stock" user is logged in.
	 */
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

	/**
	 * Saves the current user's data and updates the status.
	 *
	 * @param message status to display
	 */
	private void persist(String message) {
		try {
			UserStorage.putUser(user);
			status.setText(message);
		} catch (Exception e) {
			e.printStackTrace();
			alert("Save failed.");
		}
	}

	/**
	 * Shows an alert dialog with info.
	 *
	 * @param message the message to display
	 */
	private void alert(String message) {
		new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
	}

	/**
	 * Shows a confirmation dialog and returns the user's response.
	 *
	 * @param message confirmation
	 * @return {@code true} if OK was pressed, otherwise {@code false}
	 */
	private boolean confirm(String message) {
		return new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL).showAndWait()
				.orElse(ButtonType.CANCEL) == ButtonType.OK;
	}
}
