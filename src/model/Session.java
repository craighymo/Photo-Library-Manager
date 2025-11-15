package model;

public class Session {
    private static String username;
    private static boolean admin;
    public static String currentAlbum;
    public static Photo currentPhoto;

    public static void setCurrentUser(String user) {
        username = user;
        admin = "admin".equalsIgnoreCase(user);
    }
    
    public static String getUsername() { return username; }
    
    public static boolean isAdmin() { return admin; }
    
    public static void setCurrentAlbumName(String albumName) {
        currentAlbum = albumName;
    }
    
    public static String getCurrentAlbumName() {
        return currentAlbum;
    }
    
    public static void setCurrentPhoto(Photo p) {
        currentPhoto = p;
    }

    public static Photo getCurrentPhoto() {
        return currentPhoto;
    }
    
    public static void clear() { 
    	username = null; 
    	admin = false; 
    	currentAlbum = null;
        currentPhoto = null;
    }
}