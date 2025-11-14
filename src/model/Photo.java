package model;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

public class Photo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String filePath;
    private String caption;
    private LocalDateTime date;
    private ArrayList<Tag> tags;

    public Photo(String path) throws IOException {
        this.filePath = path;
        this.caption = "";
        this.tags = new ArrayList<>();
        this.date = LocalDateTime.ofInstant(
            Files.getLastModifiedTime(Path.of(path)).toInstant(),
            ZoneId.systemDefault()
        );
    }

    public String getFilePath() { return filePath; }
    
    public String getCaption() { return caption; }
    
    public void setCaption(String caption) { this.caption = caption; }
    
    public LocalDateTime getDate() { return date; }
    
    public List<Tag> getTags() { return tags; }

    public void addTag(Tag tag) {
        if (!tags.contains(tag)) {
        	tags.add(tag);
        }
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    @Override
    public String toString() {
        return caption.isEmpty() ? Path.of(filePath).getFileName().toString() : caption;
    }
}