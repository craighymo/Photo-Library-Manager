package model;
import java.io.*;
import java.util.*;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private ArrayList<Album> albums;

    public User(String username) {
        this.username = username;
        this.albums = new ArrayList<>();
    }

    public String getUsername() { return username; }
    
    public List<Album> getAlbums() { return albums; }

    public void addAlbum(String name) {
        if (getAlbum(name) == null) {
        	albums.add(new Album(name));
        }
    }

    public void deleteAlbum(String name) {
        albums.removeIf(a -> a.getName().equals(name));
    }

    public Album getAlbum(String name) {
        return albums.stream()
                     .filter(a -> a.getName().equals(name))
                     .findFirst().orElse(null);
    }
    
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
