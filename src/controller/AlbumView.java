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

/** Controller for the AlbumView screen.
* <p>
* This view shows all photos in a single album, along with their thumbnails,
* captions, and dates. Users can:
* </p>
* <ul>
*     <li>Add photos to the current album</li>
*     <li>Remove photos</li>
*     <li>Edit captions</li>
*     <li>Open a photo in a separate viewer (PhotoView)</li>
*     <li>Copy or move photos to another album</li>
* </ul>
*
* <p>The controller loads the active user and album from {@link Session} and
* persists changes using {@link UserStorage}.</p>
*
* @author Craig Hymowitz
*/
public class AlbumView {

	@FXML
	private Label albumNameLabel;
	@FXML
	private Label photoCountLabel;
	@FXML
	private Label status;

	@FXML
	private ListView<Photo> photoList;

	@FXML
	private Button removeButton;
	@FXML
	private Button editCaptionButton;
	@FXML
	private Button openPhotoButton;
	@FXML
	private Button copyButton;
	@FXML
	private Button moveButton;

	private User user;
	private Album album;

	/**
	 * Initializes the album view.
	 * <p>
	 * Loads the current user and album from the {@link Session}, the photo list
	 * with thumbnails and date/time information, handles the selection behavior,
	 * and sets up double-click to open a photo.
	 * </p>
	 */
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
			private final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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

	/**
	 * Returns to Albums list view.
	 */
	@FXML
	private void onBack() {
		Photos.go("Albums.fxml");
	}

	/**
	 * Adds a new photo to the current album using a file chooser. Checks it's the
	 * appropriate photo type and prevents duplicates.
	 */
	@FXML
	private void onAddPhoto() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Select Photo");
		fc.getExtensionFilters()
				.add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"));

		File selected = fc.showOpenDialog(null);
		if (selected == null)
			return;

		try {
			Photo photo = new Photo(selected.getAbsolutePath());

			boolean exists = album.getPhotos().stream().anyMatch(p -> p.getFilePath().equals(photo.getFilePath()));
			if (exists) {
				alert("This photo is already in the album.");
				return;
			}

			album.addPhoto(photo);
			photoList.getItems().add(photo);
			updatePhotoCount();
			persist("Added photo: " + photo.toString());
		} catch (Exception e) {
			e.printStackTrace();
			alert("Unable to load photo.");
		}
	}

	/**
	 * Confirms then removes the selected photo from the album.
	 */
	@FXML
	private void onRemovePhoto() {
		Photo select = photoList.getSelectionModel().getSelectedItem();
		if (select == null)
			return;

		if (!confirm("Remove photo from the album?"))
			return;

		album.removePhoto(select);
		photoList.getItems().remove(select);
		updatePhotoCount();
		persist("Removed photo.");
	}

	/**
	 * Edits the caption (file name by default) of the selected photo with text
	 * input dialog.
	 */
	@FXML
	private void onEditCaption() {
		Photo select = photoList.getSelectionModel().getSelectedItem();
		if (select == null)
			return;

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

	/**
	 * Opens the selected photo in a popup PhotoView.
	 */
	@FXML
	private void onOpenPhoto() {
		Photo select = photoList.getSelectionModel().getSelectedItem();
		if (select == null)
			return;

		Session.setCurrentPhoto(select);
		Photos.popup("PhotoView.fxml");
	}

	/**
	 * Asks the user for a target album name and returns the matching album. Makes
	 * sure that the album exists and is not the current album.
	 *
	 * @param title the dialog title/header
	 * @return the target album, or {@code null} if cancelled or not valid
	 */
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

	/**
	 * Copies the selected photo to another album.
	 */
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

	/**
	 * Moves the selected photo to another album. Same as copy but also deletes.
	 */
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

	/**
	 * Disables the main UI controls when there's no valid album loaded.
	 */
	private void disableAll() {
		if (removeButton != null)
			removeButton.setDisable(true);
		if (editCaptionButton != null)
			editCaptionButton.setDisable(true);
		if (photoList != null)
			photoList.setDisable(true);
	}

	/**
	 * Updates the label that shows the number of photos in the album.
	 */
	private void updatePhotoCount() {
		if (photoCountLabel != null && album != null) {
			photoCountLabel.setText(Integer.toString(album.getPhotoCount()));
		}
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
