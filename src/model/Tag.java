package model;

import java.io.Serializable;

public class Tag implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String value;
	
	public Tag(String name, String value) {
		this.name = name.trim().toLowerCase();
		this.value = value.trim().toLowerCase();
	}
	
	public String getValue() { return value; }
	public String getName() { return name; }
	
	@Override
    public boolean equals(Object o) {
        if (!(o instanceof Tag)) return false;
        Tag t = (Tag) o;
        return name.equals(t.name) && value.equals(t.value);
    }
	
	@Override
	public int hashCode() { 
		return (name + value).hashCode(); 
	}

	@Override
	public String toString() { 
		return name + "=" + value; 
	}
}
