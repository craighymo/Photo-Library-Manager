package controller;

import javafx.fxml.FXML;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the photo viewer popup.
 * <p>
 * This displays a single photo at a time along with its caption, date/time, and
 * tags. Users can:
 * </p>
 * <ul>
 * <li>Manually slideshow the previous/next photos in the album</li>
 * <li>Add tags to the current photo</li>
 * <li>Delete existing tags</li>
 * <li>Close the viewer window</li>
 * </ul>
 *
 * <p>
 * The current photo, album, and user are loaded from {@link Session} and
 * updated back into the model. Changes are persisted using {@link UserStorage}.
 * </p>
 *
 * @author Craig Hymowitz
 */
public class PhotoView {

	@FXML
	private ImageView imageView;
	@FXML
	private Label captionLabel;
	@FXML
	private Label dateTimeLabel;
	@FXML
	private Label status;
	@FXML
	private ListView<Tag> tagList;
	@FXML
	private Button addTagButton;
	@FXML
	private Button deleteTagButton;
	@FXML
	private Button prevButton;
	@FXML
	private Button nextButton;

	private Photo photo;
	private User user;
	private Album album;
	private int photoIndex;

	@FXML
	private void initialize() {
		String username = Session.getUsername();
		String albumName = Session.getCurrentAlbumName();
		photo = Session.getCurrentPhoto();
		user = UserStorage.getOrCreateUser(username);
		album = user.getAlbum(albumName);

		List<Photo> photos = album.getPhotos();

		int index = photos.indexOf(photo);
		if (index >= 0) {
			photoIndex = index;
		} else {
			photoIndex = 0;
			photo = photos.get(0);
			Session.setCurrentPhoto(photo);
		}

		showPhoto();
		updateNavButtons();

		tagList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			boolean hasSelection = newVal != null;
			deleteTagButton.setDisable(!hasSelection);
		});
	}

	/**
	 * Shows the current photo, its caption, date/time, and tags in the PhotoView.
	 * If the photo file cannot be found, shows a status message.
	 */
	private void showPhoto() {
		if (photo == null) {
			status.setText("No photo selected.");
			imageView.setImage(null);
			captionLabel.setText("");
			dateTimeLabel.setText("");
			tagList.getItems().clear();
			return;
		}

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
			status.setText("Viewing photo " + (photoIndex + 1));
		} else {
			imageView.setImage(null);
			status.setText("File not found: " + photo.getFilePath());
		}

		tagList.getItems().setAll(photo.getTags());
	}

	/**
	 * Moves to the previous photo in the album, if it's available.
	 */
	@FXML
	private void onPrevious() {
		List<Photo> photos = album.getPhotos();
		if (photoIndex > 0) {
			photoIndex = photoIndex - 1;
			photo = photos.get(photoIndex);
			Session.setCurrentPhoto(photo);
			showPhoto();
			updateNavButtons();
		}
	}

	/**
	 * Moves to the next photo in the album, if it's available.
	 */
	@FXML
	private void onNext() {
		List<Photo> photos = album.getPhotos();
		if (photoIndex < photos.size() - 1) {
			photoIndex = photoIndex + 1;
			photo = photos.get(photoIndex);
			Session.setCurrentPhoto(photo);
			showPhoto();
			updateNavButtons();
		}
	}

	/**
	 * Updates the state of the navigation buttons (enabled/disabled) based on the
	 * current photo index.
	 */
	private void updateNavButtons() {
		List<Photo> photos = album.getPhotos();
		prevButton.setDisable(photoIndex <= 0);
		nextButton.setDisable(photoIndex >= photos.size() - 1);
	}

	/**
	 * Handles adding a tag to the current photo.
	 * <p>
	 * Shows a dialog for the user to choose a tag type and value. Makes sure that
	 * only a single location can be tagged (can replace an existing location tag if
	 * needed), and prevents duplicates.
	 * </p>
	 */
	@FXML
	private void onAddTag() {
		if (photo == null) {
			return;
		}

		Tag newTag = showAddTagDialog();
		if (newTag == null) {
			return;
		}

		if (newTag.getName().equals("location")) {
			Tag location = getLocationTag();
			if (location != null) {
				if (!confirm("Replaces location: " + location.getValue() + " with \"" + newTag.getValue() + "\"")) {
					return;
				}
				photo.removeTag(location);
				tagList.getItems().remove(location);
			}
		}

		if (photo.getTags().contains(newTag)) {
			alert("This tag already exists for this photo.");
			return;
		}

		photo.addTag(newTag);
		tagList.getItems().add(newTag);
		persist("Added tag: " + newTag);

	}

	/**
	 * Deletes the selected tag from the current photo after checking for user
	 * confirmation.
	 */
	@FXML
	private void onDeleteTag() {
		if (photo == null) {
			return;
		}

		Tag selected = tagList.getSelectionModel().getSelectedItem();
		if (selected == null) {
			return;
		}

		if (!confirm("Delete tag '" + selected + "'?")) {
			return;
		}

		photo.removeTag(selected);
		tagList.getItems().remove(selected);
		persist("Deleted tag: " + selected);
	}

	/**
	 * Displays the dialog for adding a tag and returns the created tag, or
	 * {@code null} if the user cancels or inputs are invalid.
	 *
	 * @return the new {@link Tag}, or {@code null} if cancelled or not valid
	 */
	private Tag showAddTagDialog() {
		Dialog<Tag> dialog = new Dialog<>();
		dialog.setTitle("Add Tag");
		dialog.setHeaderText("Choose a tag type and enter a value");

		ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

		ComboBox<String> typeBox = new ComboBox<>();
		typeBox.getItems().addAll(user.getKnownTagTypes());
		typeBox.setEditable(true);

		TextField valueField = new TextField();

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));

		grid.add(new Label("Type:"), 0, 0);
		grid.add(typeBox, 1, 0);
		grid.add(new Label("Value:"), 0, 1);
		grid.add(valueField, 1, 1);

		dialog.getDialogPane().setContent(grid);

		Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
		addButton.setDisable(true);

		typeBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
			boolean typeEmpty = newVal == null || newVal.trim().isEmpty();
			boolean valueEmpty = valueField.getText() == null || valueField.getText().trim().isEmpty();
			addButton.setDisable(typeEmpty || valueEmpty);
		});

		valueField.textProperty().addListener((obs, oldVal, newVal) -> {
			String typeText = typeBox.getEditor().getText();
			boolean typeEmpty = typeText == null || typeText.trim().isEmpty();
			boolean valueEmpty = newVal == null || newVal.trim().isEmpty();
			addButton.setDisable(typeEmpty || valueEmpty);
		});

		dialog.setResultConverter(button -> {
			if (button == addButtonType) {
				String typeText = typeBox.getEditor().getText();
				String valueText = valueField.getText();
				if (typeText != null) {
					typeText = typeText.trim();
				}
				if (valueText != null) {
					valueText = valueText.trim();
				}

				if (typeText == null || typeText.isEmpty()) {
					return null;
				}
				if (valueText == null || valueText.isEmpty()) {
					return null;
				}

				return new Tag(typeText, valueText);
			}
			return null;
		});

		Optional<Tag> result = dialog.showAndWait();
		if (result.isPresent()) {
			return result.get();
		}
		return null;
	}

	/**
	 * Returns the current location tag for the photo, if there is one. Helps with
	 * regulating a singular location per photo tag.
	 *
	 * @return the location tag, or {@code null} if none exists
	 */
	private Tag getLocationTag() {
		if (photo == null) {
			return null;
		}
		for (Tag tag : photo.getTags()) {
			if (tag.getName().equals("location")) {
				return tag;
			}
		}
		return null;
	}

	/**
	 * Closes the photo view popup window.
	 */
	@FXML
	private void onClose() {
		Stage stage = (Stage) imageView.getScene().getWindow();
		stage.close();
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