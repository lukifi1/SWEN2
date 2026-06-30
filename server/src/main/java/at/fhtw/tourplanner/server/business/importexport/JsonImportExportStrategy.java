package at.fhtw.tourplanner.server.business.importexport;

import at.fhtw.tourplanner.server.business.exceptions.ImportExportException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * JSON implementation of {@link ImportExportStrategy}. Uses the Spring-managed
 * Jackson mapper so dates are serialized exactly like the rest of the API.
 */
@Component
public class JsonImportExportStrategy implements ImportExportStrategy {

    private final ObjectMapper objectMapper;

    public JsonImportExportStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String formatName() {
        return "json";
    }

    @Override
    public String fileExtension() {
        return "json";
    }

    @Override
    public String contentType() {
        return MediaType.APPLICATION_JSON_VALUE;
    }

    @Override
    public byte[] export(TourExportFile data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (RuntimeException ex) {
            throw new ImportExportException("Failed to serialize tour data to JSON.", ex);
        }
    }

    @Override
    public TourExportFile parse(byte[] data) {
        try {
            return objectMapper.readValue(data, TourExportFile.class);
        } catch (RuntimeException ex) {
            throw new ImportExportException("The uploaded file is not valid TourPlanner JSON.", ex);
        }
    }
}
