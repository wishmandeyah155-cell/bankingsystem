package securebankapp.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileStorageService {
    private final Path dataDirectory;

    public FileStorageService(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public void ensureDataDirectoryExists() throws IOException {
        if (dataDirectory != null) {
            Files.createDirectories(dataDirectory);
        }
    }

    public void writeLines(Path filePath, List<String> lines) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null.");
        }

        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path tempFile = filePath.resolveSibling(filePath.getFileName() + ".tmp");
        Files.write(
                tempFile,
                lines,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );

        try {
            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicMoveFailed) {
            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public List<String> readLines(Path filePath) throws IOException {
        if (filePath == null) {
            return new ArrayList<>();
        }
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        return Files.readAllLines(filePath, StandardCharsets.UTF_8);
    }
}
