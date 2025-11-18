package model;

/**
 * A class used to stores information about the current user session in the
 * Photos app.
 *
 * <p>
 * This class tracks:
 * </p>
 * <ul>
 * <li>The username of the logged in user</li>
 * <li>Whether the user is the admin account</li>
 * <li>The name of the current opened album</li>
 * <li>The current selected photo</li>
 * </ul>
 *
 * <p>
 * All of the fields and methods are static, since there is only one active
 * session at a time. When the user logs out, {@link #clear()} resets the
 * session state.
 * </p>
 *
 * @author Craig Hymowitz
 */

public class Session {

	/** Username of the current logged in user. */
	private static String username;

	/** Represents if the current user is the admin account. */
	private static boolean admin;

	/** Name of the currently opened album. */
	private static String currentAlbum;

	/** The currently selected photo in the open album. */
	private static Photo currentPhoto;

	/**
	 * Sets the current user for the session and determines if the user is the
	 * admin.
	 *
	 * @param user the username of the logging in user
	 */
	public static void setCurrentUser(String user) {
		username = user;
		admin = "admin".equalsIgnoreCase(user);
	}

	/**
	 * Returns the username of the currently logged in user.
	 *
	 * @return the username
	 */
	public static String getUsername() {
		return username;
	}

	/**
	 * Returns whether or not the current session belongs to the admin user.
	 *
	 * @return {@code true} if current user is admin, otherwise {@code false}
	 */
	public static boolean isAdmin() {
		return admin;
	}

	/**
	 * Sets the name of the album currently being viewed.
	 *
	 * @param albumName the current album name
	 */
	public static void setCurrentAlbumName(String albumName) {
		currentAlbum = albumName;
	}

	/**
	 * Returns the name of the currently opened album.
	 *
	 * @return the album name, or {@code null} if none is open
	 */
	public static String getCurrentAlbumName() {
		return currentAlbum;
	}

	/**
	 * Sets the currently selected photo.
	 *
	 * @param p the current photo
	 */
	public static void setCurrentPhoto(Photo p) {
		currentPhoto = p;
	}

	/**
	 * Returns the currently selected photo.
	 *
	 * @return the current photo, or {@code null} if none selected
	 */
	public static Photo getCurrentPhoto() {
		return currentPhoto;
	}

	/**
	 * Clears all session data. Called on logout.
	 */
	public static void clear() {
		username = null;
		admin = false;
		currentAlbum = null;
		currentPhoto = null;
	}
}