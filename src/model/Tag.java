package model;

import java.io.Serializable;

/**
 * Represents a tag applied to a photo. A tag consists of a name–value pair,
 * like <code>person=craig</code> or <code>location=new jersey</code>.
 *
 * <p>
 * Tag names and values are stored in lowercase to prevent case sensitivity
 * issues when comparing. Two tags are considered equal if both their names and
 * values match.
 * </p>
 *
 * <p>
 * This class is serializable so that tag information can be saved as part of a
 * photo's metadata.
 * </p>
 *
 * @author Craig Hymowitz
 */
public class Tag implements Serializable {

	/** Serialization identifier to ensure version compatibility. */
	private static final long serialVersionUID = 1L;

	/** The tag's name (e.g., "person", "location"). */
	private String name;

	/** The tag's value (e.g., "susan", "prague"). */
	private String value;

	/**
	 * Creates a new tag with the given name and value. Spaces (leading/trailing)
	 * are trimmed and the pair is set to lowercase.
	 *
	 * @param name  the tag type
	 * @param value the tag value
	 */
	public Tag(String name, String value) {
		this.name = name.trim().toLowerCase();
		this.value = value.trim().toLowerCase();
	}

	/**
	 * Returns the tag's value.
	 *
	 * @return the tag value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns the tag's name.
	 *
	 * @return the tag name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Compares this tag with another object for equality. Two tags are equal if
	 * both name and value match.
	 *
	 * @param o the object to compare
	 * @return {@code true} if the tags match, otherwise {@code false}
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Tag))
			return false;
		Tag tag = (Tag) o;
		return name.equals(tag.name) && value.equals(tag.value);
	}

	/**
	 * Returns a hash code based on both name and value.
	 *
	 * @return the hash code
	 */
	@Override
	public int hashCode() {
		return (name + value).hashCode();
	}

	/**
	 * Returns a string representation of the tag in <code>name=value</code> format.
	 *
	 * @return the formatted tag string
	 */
	@Override
	public String toString() {
		return name + "=" + value;
	}
}
