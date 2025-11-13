package model;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class UserStorage {
    private static final String DATA_FILE = "data/users.dat";
    private static Map<String, User> users; 

    @SuppressWarnings("unchecked")
	private static void ensureLoaded() {
    	
        if (users != null) return;
        
        Path p = Path.of(DATA_FILE);
        if (!Files.exists(p)) { 
        	users = new HashMap<>(); return; 
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

    private static void saveAll() throws IOException {
        Files.createDirectories(Path.of("data"));
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(users);
        }
    }

    public static User getOrCreateUser(String username) {
        ensureLoaded();
        return users.computeIfAbsent(username, User::new);
    }

    public static synchronized User getUser(String username) {
        ensureLoaded();
        return users.get(username);
    }

    public static void putUser(User user) throws IOException {
        ensureLoaded();
        users.put(user.getUsername(), user);
        saveAll();
    }

    public static void save() throws IOException {
        ensureLoaded();
        saveAll();
    }
}