package at.fhtw.tourplanner.server.presentation;

import at.fhtw.tourplanner.server.business.ImageStorageService;
import at.fhtw.tourplanner.server.dto.ImageUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageStorageService imageStorageService;

    /** Uploads a tour image and returns its generated filename. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageUploadResponse upload(@RequestParam("file") MultipartFile file) {
        return new ImageUploadResponse(imageStorageService.store(file));
    }

    /** Serves a stored image (public so it can be used directly in &lt;img src&gt;). */
    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> serve(@PathVariable String filename) {
        return imageStorageService.find(filename)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaTypeFactory.getMediaType(filename)
                                .orElse(MediaType.APPLICATION_OCTET_STREAM))
                        .body(bytes))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
