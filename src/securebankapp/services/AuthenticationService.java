package securebankapp.services;

import securebankapp.models.User;
import securebankapp.security.PasswordSecurity;
import securebankapp.storage.FileStorageService;
import securebankapp.utils.InputValidator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AuthenticationService {
    private final FileStorageService storageService;
    private final Path userFile;
    private final List<User> users;

    public AuthenticationService(FileStorageService storageService, Path userFile) {
        this.storageService = storageService;
        this.userFile = userFile;
        this.users = new ArrayList<>();
    }

    public User register(String username, String password) throws IOException {
        String cleanUsername = InputValidator.sanitizeText(username);
        String cleanPassword = InputValidator.sanitizeText(password);

        if (!InputValidator.isValidUsername(cleanUsername)) {
            throw new IllegalArgumentException("Username must be 3 to 20 letters, numbers, underscores, or hyphens.");
        }
        if (!InputValidator.isValidPassword(cleanPassword)) {
            throw new IllegalArgumentException("Password must be 8 to 128 characters long.");
        }
        if (findUserByUsername(cleanUsername) != null) {
            throw new IllegalArgumentException("Username is already registered.");
        }

        String salt = PasswordSecurity.generateSalt();
        int iterations = PasswordSecurity.DEFAULT_ITERATIONS;
        String passwordHash = PasswordSecurity.hashPassword(cleanPassword, salt, iterations);
        User user = new User(cleanUsername, passwordHash, salt, iterations);
        users.add(user);
        saveUsers();
        return user;
    }

    public User login(String username, String password) {
        String cleanUsername = InputValidator.sanitizeText(username);
        String cleanPassword = InputValidator.sanitizeText(password);

        User user = findUserByUsername(cleanUsername);
        if (user == null) {
            return null;
        }
        if (PasswordSecurity.verifyPassword(cleanPassword, user.getSalt(), user.getIterations(), user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    public void loadUsers() throws IOException {
        users.clear();
        List<String> lines = storageService.readLines(userFile);
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            String[] fields = line.split("\\|", -1);
            if (fields.length != 4) {
                continue;
            }

            String username = fields[0];
            String hash = fields[1];
            String salt = fields[2];
            int iterations;
            try {
                iterations = Integer.parseInt(fields[3]);
            } catch (NumberFormatException e) {
                continue;
            }

            if (!InputValidator.isValidUsername(username) || hash == null || hash.isBlank() || salt == null || salt.isBlank() || iterations <= 0) {
                continue;
            }

            users.add(new User(username, hash, salt, iterations));
        }
    }

    public void saveUsers() throws IOException {
        storageService.ensureDataDirectoryExists();
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(user.getUsername() + "|" + user.getPasswordHash() + "|" + user.getSalt() + "|" + user.getIterations());
        }
        storageService.writeLines(userFile, lines);
    }

    private User findUserByUsername(String username) {
        for (User user : users) {
            if (Objects.equals(user.getUsername(), username)) {
                return user;
            }
        }
        return null;
    }
}
