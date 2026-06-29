package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.business.exceptions.ImageStorageException;
import at.fhtw.tourplanner.server.config.TourPlannerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Stores tour images on the filesystem (outside the database, as required) in
 * the configured base directory. Filenames are randomized to avoid collisions
 * and path traversal.
 */
@Service
@Slf4j
public class ImageStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path baseDirectory;

    public ImageStorageService(TourPlannerProperties properties) {
        this.baseDirectory = Paths.get(properties.getImage().getBasePath()).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImageStorageException("No image file was provided.");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ImageStorageException("Unsupported image type: " + extension);
        }

        String filename = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(baseDirectory);
            Path target = resolveSafe(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored image {}", filename);
            return filename;
        } catch (IOException ex) {
            throw new ImageStorageException("Failed to store image.", ex);
        }
    }

    public Optional<byte[]> find(String filename) {
        Path target = resolveSafe(filename);
        if (!Files.exists(target)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readAllBytes(target));
        } catch (IOException ex) {
            throw new ImageStorageException("Failed to read image: " + filename, ex);
        }
    }

    /** Resolves a filename inside the base directory, rejecting path traversal. */
    private Path resolveSafe(String filename) {
        if (!StringUtils.hasText(filename)) {
            throw new ImageStorageException("Invalid image filename.");
        }
        Path target = baseDirectory.resolve(filename).normalize();
        if (!target.startsWith(baseDirectory)) {
            throw new ImageStorageException("Invalid image path.");
        }
        return target;
    }

    private String extensionOf(String originalFilename) {
        String ext = StringUtils.getFilenameExtension(originalFilename);
        return ext == null ? "" : ext.toLowerCase(Locale.ROOT);
    }
}
