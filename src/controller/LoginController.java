package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import app.Photos;
import model.Session;

/**
 * Controller for the login screen.
 * <p>
 * Handles reading the username entered by the user and determining if it should
 * start an admin session or a regular user session. Navigation is performed
 * through {@link Photos}.
 * </p>
 *
 * <p>
 * With no passwords required, simply typing a username and pressing Enter (or
 * clicking login) is sufficient to get started.
 * </p>
 *
 * @author Craig Hymowitz
 */
public class LoginController {

	@FXML
	private TextField usernameField;

	/**
	 * Initializes the login view by allowing the user to press Enter inside the
	 * text field to trigger the login action.
	 */
	@FXML
	private void initialize() {
		usernameField.setOnAction(e -> onLogin());
	}

	/**
	 * Handles logging in.
	 * <p>
	 * Reads the username, trims and sets it to lowercase, stores it in
	 * {@link Session}, and navigates to either the admin view or the albums view
	 * depending on whether the username is "admin".
	 * </p>
	 */
	@FXML
	private void onLogin() {
		String username = usernameField.getText().toLowerCase().trim();
		if (username.isEmpty())
			return;

		Session.setCurrentUser(username);

		if (Session.isAdmin()) {
			Photos.go("Admin.fxml");
		} else {
			Photos.go("Albums.fxml");
		}
	}
}