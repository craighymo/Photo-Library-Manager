package model;

import java.io.*;

import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A static class that handles loading, storing, and managing user data for the
 * Photos app.
 *
 * <p>
 * The class maintains a static map of usernames to {@link User} objects. User
 * data is persisted using Java serialization and stored in
 * <code>data/users.dat</code>. The methods ensure the user map is loaded before
 * access.
 * </p>
 *
 * <p>
 * The storage format contains the entire map of users, including their albums
 * and photos, so data can be fully restored across many sessions.
 * </p>
 *
 * @author Craig Hymowitz
 */
public class UserStorage {

	/** File where serialized user data is stored. */
	private static final String DATA_FILE = "data/users.dat";

	/** Map of all users. Key is username. */
	private static Map<String, User> users;

	/**
	 * Makes sure the user map is loaded. If it is, the method returns immediately.
	 * If no stored data exists, a new empty map is created.
	 *
	 * <p>
	 * If the stored file exists but fails to deserialize, a new empty map is
	 * created and the error is logged.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	private static void ensureLoaded() {

		if (users != null)
			return;

		Path path = Path.of(DATA_FILE);
		if (!Files.exists(path)) {
			users = new HashMap<>();
			return;
		}
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
			Object obj = ois.readObject();
			if (obj instanceof Map) {
				users = (Map<String, User>) obj;
			} else {
				users = new HashMap<>();
			}
		} catch (Exception e) {
			users = new HashMap<>();
			e.printStackTrace();
		}
	}

	/**
	 * Saves all user data in serialized form.
	 *
	 * @throws IOException if the data cannot be written
	 */
	private static void saveAll() throws IOException {
		Files.createDirectories(Path.of("data"));
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
			oos.writeObject(users);
		}
	}

	/**
	 * Retrieves the user with the given username. Creates a new user if none
	 * exists, then automatically adds the new user to storage.
	 *
	 * @param username the username to find or create
	 * @return the existing or newly created user
	 */
	public static User getOrCreateUser(String username) {
		ensureLoaded();
		return users.computeIfAbsent(username, User::new);
	}

	/**
	 * Returns a map of all users. Can be modified by admin.
	 *
	 * @return a map of all users
	 */
	public static Map<String, User> getAllUsers() {
		ensureLoaded();
		return users;
	}

	/**
	 * Adds or replaces a user in storage and saves the updated user map.
	 *
	 * @param user the user to store
	 * @throws IOException if saving fails
	 */
	public static void putUser(User user) throws IOException {
		ensureLoaded();
		users.put(user.getUsername(), user);
		saveAll();
	}

	/**
	 * Returns the user with the given username, or {@code null} if no such user
	 * exists.
	 *
	 * @param username the username to look up
	 * @return the user, or {@code null} if not found
	 */
	public static synchronized User getUser(String username) {
		ensureLoaded();
		return users.get(username);
	}

	/**
	 * Saves all user data to disk.
	 *
	 * @throws IOException if the save operation fails
	 */
	public static void save() throws IOException {
		ensureLoaded();
		saveAll();
	}
}