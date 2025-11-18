package controller;

import app.Photos;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import model.User;
import model.UserStorage;

import java.io.IOException;

/**
 * Controller for the Admin screen.
 *
 * This screen allows the administrator to:
 * <ul>
 * <li>View all registered users</li>
 * <li>Add new users</li>
 * <li>Delete existing users (except admin and stock)</li>
 * <li>Logout back to the login screen</li>
 * </ul>
 *
 * It interacts with {@link UserStorage} to manage persistent user data.
 * 
 * @author Joseph Cabrera
 */
public class AdminController {

	@FXML
	private ListView<String> userList;

	@FXML
	private TextField newUserField;

	@FXML
	private Button addUserButton;

	@FXML
	private Button deleteUserButton;

	@FXML
	private Button logoutButton;

	/**
	 * Initializes the Admin screen by populating the user list.
	 */
	@FXML
	private void initialize() {
		refreshList();
	}

	/**
	 * Reloads the list of usernames displayed in the UI. Fetches all users from
	 * storage and updates the ListView.
	 */
	private void refreshList() {
		userList.getItems().clear();
		for (String name : UserStorage.getAllUsers().keySet()) {
			userList.getItems().add(name);
		}
	}

	/**
	 * Adds a new user using the text entered in the input field. Validates input,
	 * checks for duplicates, and updates storage.
	 */
	@FXML
	private void addUser() {
		String name = newUserField.getText().trim();

		if (name.isEmpty()) {
			showError("Username cannot be empty.");
			return;
		}

		if (UserStorage.getUser(name) != null) {
			showError("User already exists.");
			return;
		}

		try {
			User newUser = new User(name);
			UserStorage.putUser(newUser);
			refreshList();
			newUserField.clear();
		} catch (IOException e) {
			e.printStackTrace();
			showError("Error saving user.");
		}
	}

	/**
	 * Deletes the selected user from the system. Prevents deletion of reserved
	 * users such as "admin" and "stock".
	 */
	@FXML
	private void deleteUser() {
		String selected = userList.getSelectionModel().getSelectedItem();

		if (selected == null) {
			showError("Select a user first.");
			return;
		}

		if (selected.equalsIgnoreCase("admin") || selected.equalsIgnoreCase("stock")) {
			showError("Cannot delete admin or stock user.");
			return;
		}

		try {
			UserStorage.getAllUsers().remove(selected);
			UserStorage.save();
			refreshList();
		} catch (IOException e) {
			e.printStackTrace();
			showError("Error deleting user.");
		}
	}

	/**
	 * Logs out from the Admin screen and returns to the login view.
	 */
	@FXML
	private void logout() {
		Photos.go("Login.fxml");
	}

	/**
	 * Shows an error popup with the provided message.
	 *
	 * @param msg the error message to display
	 */
	private void showError(String msg) {
		Alert a = new Alert(Alert.AlertType.ERROR);
		a.setHeaderText(null);
		a.setContentText(msg);
		a.showAndWait();
	}
}
