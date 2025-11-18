package controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import model.*;

import app.Photos;

/**
 * The Search controller handles all searching operations in the application. It
 * supports searching by date ranges, by tags, combining tag criteria using
 * AND/OR logic, displaying search results, and creating albums from results.
 * This class also manages navigation back to the Albums screen or logout.
 * 
 * @author Joseph Cabrera
 */
public class Search {

	@FXML
	private Label statusLabel;
	@FXML
	private DatePicker startDatePicker;
	@FXML
	private DatePicker endDatePicker;

	@FXML
	private TextField tagType1Field;
	@FXML
	private TextField tagValue1Field;
	@FXML
	private TextField tagType2Field;
	@FXML
	private TextField tagValue2Field;
	@FXML
	private TextField nameField;

	@FXML
	private RadioButton andRadio;
	@FXML
	private RadioButton orRadio;

	@FXML
	private ListView<Photo> resultsList;

	private List<Photo> results = new ArrayList<>();

	/**
	 * Runs the search procedure. Determines whether to perform a date range search,
	 * a tag search, or both. Fills the results lists.
	 */
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

		boolean dateSearchUsed = startDatePicker.getValue() != null || endDatePicker.getValue() != null;

		boolean tagSearchUsed = !tagType1Field.getText().isEmpty() || !tagValue1Field.getText().isEmpty()
				|| !tagType2Field.getText().isEmpty() || !tagValue2Field.getText().isEmpty();

		String nameSearch = nameField.getText().trim();
		boolean nameSearchUsed = !nameSearch.isEmpty();

		if (nameSearchUsed && dateSearchUsed || tagSearchUsed && dateSearchUsed || nameSearchUsed && tagSearchUsed) {
			showError("Choose EITHER name search, date search OR tag search.");
			return;
		}

		if (!dateSearchUsed && !tagSearchUsed && !nameSearchUsed) {
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

		if (nameSearchUsed) {
			String query = nameField.getText().toLowerCase();

			for (Album a : albums) {
				for (Photo p : a.getPhotos()) {

					boolean found = false;

					String caption = p.getCaption();
					if (caption != null) {
						caption = caption.toLowerCase();
						if (caption.contains(query)) {
							found = true;
						}
					}

					if (!found) {
						String filePath = p.getFilePath();
						if (filePath != null) {
							String fileName = new java.io.File(filePath).getName();
							fileName = fileName.toLowerCase();
							if (fileName.contains(query)) {
								found = true;
							}
						}
					}
					if (found && !results.contains(p)) {
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
					if (t.getName().equalsIgnoreCase(type1) && t.getValue().equalsIgnoreCase(value1)) {
						match1 = true;
						break;
					}
				}

				if (usingSecondTag) {
					for (Tag t : p.getTags()) {
						if (t.getName().equalsIgnoreCase(type2) && t.getValue().equalsIgnoreCase(value2)) {
							match2 = true;
							break;
						}
					}
				}

				boolean add = false;

				if (!usingSecondTag) {
					if (match1)
						add = true;
				} else {
					if (useAND && match1 && match2)
						add = true;
					if (useOR && (match1 || match2))
						add = true;
				}

				if (add && !results.contains(p)) {
					results.add(p);
				}
			}
		}

		resultsList.getItems().setAll(results);
		statusLabel.setText(results.size() + " photos found.");
	}

	/**
	 * Opens the double-clicked photo in a popup viewer.
	 *
	 * @param e Mouse event associated with the click.
	 */
	@FXML
	private void onResultDoubleClick(MouseEvent e) {
		if (e.getClickCount() == 2) {
			Photo selected = resultsList.getSelectionModel().getSelectedItem();
			if (selected == null)
				return;

			Session.setCurrentPhoto(selected);
			Photos.popup("PhotoView.fxml");
		}
	}

	/**
	 * Creates a new album using all photos from the current search results. Prompts
	 * the user for an album name and saves it if valid.
	 */
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
			for (Photo p : results)
				newAlbum.addPhoto(p);

			user.getAlbums().add(newAlbum);

			try {
				UserStorage.save();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			statusLabel.setText("Album \"" + name + "\" created.");
		});
	}

	/**
	 * Navigates back to the Albums screen.
	 */

	@FXML
	private void onBack() {
		Photos.go("Albums.fxml");
	}

	/**
	 * Logs the current user out and returns to the login screen.
	 */

	@FXML
	private void onLogout() {
		Session.clear();
		Photos.go("Login.fxml");
	}

	/**
	 * Resets all search input fields and clears results.
	 */
	@FXML
	private void onReset() {

		startDatePicker.setValue(null);
		endDatePicker.setValue(null);

		tagType1Field.clear();
		tagValue1Field.clear();
		tagType2Field.clear();
		tagValue2Field.clear();

		andRadio.setSelected(true);
		orRadio.setSelected(false);

		if (nameField != null) {
			nameField.clear();
		}

		results.clear();
		resultsList.getItems().clear();
		statusLabel.setText("");
	}

	/**
	 * Shows an error dialog with the given message.
	 *
	 * @param msg the text to display
	 */
	private void showError(String msg) {
		Alert a = new Alert(Alert.AlertType.ERROR);
		a.setHeaderText(null);
		a.setContentText(msg);
		a.showAndWait();
	}
}