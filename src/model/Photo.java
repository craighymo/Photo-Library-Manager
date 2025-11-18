package model;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

/**
 * Represents a single photo in the Photos app. A photo stores its file
 * location, caption, date-time taken (determined from the file's last modified
 * timestamp), and a list of associated tags.
 *
 * <p>
 * This class is serializable so that photo data can be saved and restored
 * across sessions. The image file itself is not stored, only its path.
 * </p>
 *
 * @author Craig Hymowitz
 */
public class Photo implements Serializable {
	/** Serialization identifier to maintain version compatibility. */
	private static final long serialVersionUID = 1L;

	/** The path to the user's local photo file. */
	private String filePath;

	/** The caption associated with the photo. */
	private String caption;

	/**
	 * The date and time the photo was taken (based on last time the file was
	 * modified).
	 */
	private LocalDateTime date;

	/** List of this photo's tags. */
	private ArrayList<Tag> tags;

	/**
	 * Creates a new Photo object from the given file path. The date is the file's
	 * last modified timestamp.
	 *
	 * @param path file path for the image
	 * @throws IOException if the file cannot be accessed
	 */
	public Photo(String path) throws IOException {
		this.filePath = path;
		this.caption = "";
		this.tags = new ArrayList<>();
		this.date = LocalDateTime.ofInstant(Files.getLastModifiedTime(Path.of(path)).toInstant(),
				ZoneId.systemDefault());
	}

	/**
	 * Returns the file path of the photo.
	 *
	 * @return photo file path
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * Returns the caption associated with the photo.
	 *
	 * @return the caption text
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * Updates the caption of the photo.
	 *
	 * @param caption new caption
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**
	 * Returns the date and time the photo was taken.
	 *
	 * @return the photo date/time
	 */
	public LocalDateTime getDate() {
		return date;
	}

	/**
	 * Returns the list of tags associated with the photo.
	 *
	 * @return list of tags
	 */
	public List<Tag> getTags() {
		return tags;
	}

	/**
	 * Adds a tag to the photo, if it does not already exist.
	 *
	 * @param tag the tag to add
	 */
	public void addTag(Tag tag) {
		if (!tags.contains(tag)) {
			tags.add(tag);
		}
	}

	/**
	 * Removes the specified tag from the photo, if it exists.
	 *
	 * @param tag the tag to add
	 */
	public void removeTag(Tag tag) {
		tags.remove(tag);
	}

	/**
	 * Returns a string to represent the photo. If a caption exists, it is returned.
	 * If not, the file name is used.
	 *
	 * @return the caption or file name
	 */
	@Override
	public String toString() {
		return caption.isEmpty() ? Path.of(filePath).getFileName().toString() : caption;
	}
}