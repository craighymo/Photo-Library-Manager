package model;

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.util.Comparator;

/**
 * Represents an album belonging to a user. An album stores a collection of
 * photos and provides methods for adding, removing, and getting information
 * from photos like retrieving the earliest and latest dates of the photos it
 * contains.
 *
 * <p>
 * This class is serializable so that album data may be saved and restored
 * across application sessions.
 * </p>
 *
 * @author Craig Hymowitz
 */
public class Album implements Serializable {

	/** Serialization identifier to ensure version compatibility. */
	private static final long serialVersionUID = 1L;

	/** The name of the album (unique per user) */
	private String name;

	/** The list of photos in this album */
	private ArrayList<Photo> photos;

	/**
	 * Creates a new album with the specified name
	 *
	 * @param name the name of the album (not null)
	 */
	public Album(String name) {
		this.name = name;
		this.photos = new ArrayList<>();
	}

	/**
	 * Returns the name of this album
	 *
	 * @return the album name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Renames this album
	 *
	 * @param name the new album name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the list of photos in this album
	 *
	 * @return list of photos
	 */
	public List<Photo> getPhotos() {
		return photos;
	}

	/**
	 * Adds the photo to the album, if it is not already there
	 *
	 * @param photo the photo to add
	 */
	public void addPhoto(Photo photo) {
		if (!photos.contains(photo)) {
			photos.add(photo);
		}
	}

	/**
	 * Removes the given photo from the album, if it exists
	 *
	 * @param photo the photo to remove
	 */
	public void removePhoto(Photo photo) {
		photos.remove(photo);
	}

	/**
	 * Returns the number of photos in this album
	 *
	 * @return the number of photos
	 */
	public int getPhotoCount() {
		return photos.size();
	}

	/**
	 * Returns the earliest photo date within this album Returns {@code null} if no
	 * photos or photos do not have dates
	 *
	 * @return the earliest photo date, or {@code null} if none
	 */
	public LocalDateTime getEarliestDate() {
		if (photos.isEmpty())
			return null;
		return photos.stream().map(Photo::getDate).filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
	}

	/**
	 * Returns the latest photo date within this album Returns {@code null} if no
	 * photos or photos do not have dates
	 *
	 * @return the earliest photo date, or {@code null} if none
	 */
	public LocalDateTime getLatestDate() {
		if (photos.isEmpty())
			return null;
		return photos.stream().map(Photo::getDate).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
	}

	/**
	 * Returns the album name as its string representation
	 *
	 * @return the album name
	 */
	@Override
	public String toString() {
		return name;
	}
}