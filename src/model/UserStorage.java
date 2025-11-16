package model;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;


public class UserStorage {

    private static final String DATA_FILE = "data/users.dat";

    private static Map<String, User> users = null;

    private static void ensureLoaded() {
        if (users != null) return;

        Path p = Path.of(DATA_FILE);

        if (!Files.exists(p)) {
            users = new HashMap<>();
            return;
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(DATA_FILE))) {

            Object obj = ois.readObject();
            if (obj instanceof Map<?, ?> map) {
                users = (Map<String, User>) map;
            } else {
                users = new HashMap<>();
            }

        } catch (Exception e) {
            e.printStackTrace();
            users = new HashMap<>();
        }
    }

    private static void saveAll() throws IOException {
        Files.createDirectories(Path.of("data"));

        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
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

    public static Map<String, User> getAllUsers() {
        ensureLoaded();
        return users;
    }

    public static void ensureAdminAndStock() throws IOException {
        ensureLoaded();

        if (!users.containsKey("admin")) {
            users.put("admin", new User("admin"));
        }

        if (!users.containsKey("stock")) {
            User stock = new User("stock");
            stock.addAlbum("stock");
            users.put("stock", stock);
        }

        saveAll();
    }
}
