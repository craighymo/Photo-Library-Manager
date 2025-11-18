package model;

import java.io.*;
import java.util.*;

/**
 * Represents the user of the Photos app. Each user has a unique username and a
 * collection of albums.
 *
 * <p>
 * This class is serializable so that all user data (albums, photos, tags) can
 * be saved and restored across sessions.
 * </p>
 *
 * @author Craig Hymowitz
 */
public class User implements Serializable {

	/** Serialization identifier to ensure version compatibility. */
	private static final long serialVersionUID = 1L;

	/** Unique username for this user */
	private String username;

	/** All albums that belong to this user */
	private ArrayList<Album> albums;

	/**
	 * The constuctor creates a new user with the given username.
	 *
	 * @param username is the username for this user (not null)
	 */
	public User(String username) {
		this.username = username;
		this.albums = new ArrayList<>();
	}

	/**
	 * Returns the username for this user.
	 *
	 * @return username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Returns list of albums belonging to this user.
	 *
	 * @return albums list
	 */
	public List<Album> getAlbums() {
		return albums;
	}

	/**
	 * Creates and adds a new album with the name given, if an album with that name
	 * doesn't already exist.
	 * 
	 * @param name the album name
	 */
	public void addAlbum(String name) {
		if (getAlbum(name) == null) {
			albums.add(new Album(name));
		}
	}

	/**
	 * Deletes an album with the name provided if it exists
	 *
	 * @param name the name of the album to delete
	 */
	public void deleteAlbum(String name) {
		albums.removeIf(a -> a.getName().equals(name));
	}

	/**
	 * Retrieves an album with the given name
	 *
	 * @param name the name of the album to search for
	 * @return the album, or {@code null} if it wasn't found
	 */
	public Album getAlbum(String name) {
		return albums.stream().filter(a -> a.getName().equals(name)).findFirst().orElse(null);
	}

	/**
	 * Returns a list of all tag types known to this user
	 * <p>
	 * Includes the default tag types:
	 * <ul>
	 * <li>person</li>
	 * <li>location</li>
	 * </ul>
	 * Other tag types are discovered by checking through all photos and collecting
	 * the tag names used.
	 *
	 * @return a list of distinct tag types
	 */
	public List<String> getKnownTagTypes() {
		List<String> list = new ArrayList<>();

		list.add("person");
		list.add("location");

		for (Album album : albums) {
			for (Photo photo : album.getPhotos()) {
				for (Tag tag : photo.getTags()) {
					String name = tag.getName();
					if (name != null && !name.isBlank() && !list.contains(name)) {
						list.add(name);
					}
				}
			}
		}
		return list;
	}
}
