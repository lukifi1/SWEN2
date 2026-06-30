package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.business.exceptions.ImageStorageException;
import at.fhtw.tourplanner.server.config.TourPlannerProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Protects the security and correctness constraints in {@link ImageStorageService}.
 * Critically: only allowed image extensions are accepted, path-traversal filenames
 * must be rejected, and looking up a non-existent image returns empty rather than
 * throwing.
 */
class ImageStorageServiceTest {

    private ImageStorageService serviceWithTempDir(Path dir) {
        TourPlannerProperties props = new TourPlannerProperties();
        props.getImage().setBasePath(dir.toString());
        return new ImageStorageService(props);
    }

    @Test
    void storeRejectsUnsupportedExtension(@TempDir Path dir) {
        ImageStorageService service = serviceWithTempDir(dir);
        MockMultipartFile file = new MockMultipartFile("file", "photo.bmp", "image/bmp", new byte[]{1});

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(ImageStorageException.class)
                .hasMessageContaining("Unsupported image type");
    }

    @Test
    void storeRejectsEmptyFile(@TempDir Path dir) {
        ImageStorageService service = serviceWithTempDir(dir);
        MockMultipartFile empty = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> service.store(empty))
                .isInstanceOf(ImageStorageException.class);
    }

    @Test
    void storeValidImageReturnsFilename(@TempDir Path dir) {
        ImageStorageService service = serviceWithTempDir(dir);
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});

        String filename = service.store(file);

        assertThat(filename).endsWith(".jpg");
        assertThat(dir.resolve(filename)).exists();
    }

    @Test
    void findReturnsEmptyForMissingFile(@TempDir Path dir) {
        ImageStorageService service = serviceWithTempDir(dir);

        Optional<byte[]> result = service.find("nonexistent.jpg");

        assertThat(result).isEmpty();
    }

    @Test
    void findRejectsPathTraversalFilename(@TempDir Path dir) {
        ImageStorageService service = serviceWithTempDir(dir);

        assertThatThrownBy(() -> service.find("../../etc/passwd"))
                .isInstanceOf(ImageStorageException.class);
    }
}
