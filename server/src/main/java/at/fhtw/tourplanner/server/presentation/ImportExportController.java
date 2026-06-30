package at.fhtw.tourplanner.server.presentation;

import at.fhtw.tourplanner.server.business.ImportExportService;
import at.fhtw.tourplanner.server.business.exceptions.ImportExportException;
import at.fhtw.tourplanner.server.dto.ImportResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class ImportExportController {

    private final ImportExportService importExportService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(Authentication auth) {
        byte[] data = importExportService.export(auth.getName());
        String filename = "tours." + importExportService.exportFileExtension();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(importExportService.exportContentType()))
                .body(data);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResult importTours(@RequestParam("file") MultipartFile file, Authentication auth) {
        try {
            int imported = importExportService.importData(file.getBytes(), auth.getName());
            return new ImportResult(imported);
        } catch (IOException ex) {
            throw new ImportExportException("Could not read the uploaded file.", ex);
        }
    }
}
