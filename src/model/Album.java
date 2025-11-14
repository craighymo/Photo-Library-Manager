package model;

import java.io.*;
import java.util.*;

public class Album implements Serializable {
    private static final long serialVersionUID = 1L; // keeps saved files compatible

    private String name;
    private ArrayList<Photo> photos;

    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>();
    }

    public String getName() { return name; }
    
    public void setName(String name) { this.name = name; }

    public List<Photo> getPhotos() { return photos; }
    
    
    public void addPhoto(Photo p) {
        if (!photos.contains(p)) {
        	photos.add(p);
        }
    }

    public void removePhoto(Photo p) {
        photos.remove(p);
    }

    public int getPhotoCount() {
        return photos.size();
    }

    @Override
    public String toString() {
    	return name;
        //return name + " (" + photos.size() + " photos)";
    }
}