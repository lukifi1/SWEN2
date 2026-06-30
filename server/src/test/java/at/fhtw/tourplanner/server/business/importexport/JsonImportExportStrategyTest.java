package at.fhtw.tourplanner.server.business.importexport;

import at.fhtw.tourplanner.server.business.exceptions.ImportExportException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Protects the JSON (de)serialization used by the import/export feature
 * ({@link JsonImportExportStrategy}). An export must round-trip losslessly through a
 * parse - including a log's {@link LocalDateTime} - so that exported files re-import
 * faithfully. Malformed input must surface as a domain {@link ImportExportException}
 * rather than a raw Jackson error. Uses the Spring-managed Jackson 3 {@code ObjectMapper}
 * bean so dates serialize exactly like the rest of the API.
 */
@SpringBootTest
class JsonImportExportStrategyTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void exportThenParseRoundTripsTourWithLog() {
        JsonImportExportStrategy strategy = new JsonImportExportStrategy(objectMapper);

        LocalDateTime when = LocalDateTime.of(2026, 3, 15, 14, 30);
        TourExportFile.TourLogExport logExport =
                new TourExportFile.TourLogExport(when, "great trip", 3, 12.5, 1.5, 4);
        TourExportFile.TourExport tourExport = new TourExportFile.TourExport(
                "Alps", "scenic", "Vienna", "Graz", "Hiking",
                42.0, 1.2, "[[1.0,2.0]]", "img.png", List.of(logExport));
        TourExportFile original = new TourExportFile("1.0", List.of(tourExport));

        byte[] bytes = strategy.export(original);
        TourExportFile parsed = strategy.parse(bytes);

        assertThat(parsed.version()).isEqualTo("1.0");
        assertThat(parsed.tours()).hasSize(1);
        TourExportFile.TourExport parsedTour = parsed.tours().getFirst();
        assertThat(parsedTour.name()).isEqualTo("Alps");
        assertThat(parsedTour.distance()).isEqualTo(42.0);
        assertThat(parsedTour.logs()).hasSize(1);
        TourExportFile.TourLogExport parsedLog = parsedTour.logs().getFirst();
        assertThat(parsedLog.dateTime()).isEqualTo(when);
        assertThat(parsedLog.comment()).isEqualTo("great trip");
        assertThat(parsedLog.rating()).isEqualTo(4);
    }

    @Test
    void parsingInvalidBytesThrowsImportExportException() {
        JsonImportExportStrategy strategy = new JsonImportExportStrategy(objectMapper);

        assertThatThrownBy(() -> strategy.parse("not json".getBytes()))
                .isInstanceOf(ImportExportException.class);
    }
}
