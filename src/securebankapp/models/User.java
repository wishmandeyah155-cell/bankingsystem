package securebankapp.models;

public class User {
    private final String username;
    private final String passwordHash;
    private final String salt;
    private final int iterations;

    public User(String username, String passwordHash, String salt, int iterations) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.iterations = iterations;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public int getIterations() {
        return iterations;
    }
}
