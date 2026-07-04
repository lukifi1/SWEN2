package at.fhtw.tourplanner.server.business.importexport;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GpxImportExportStrategyTest {

    private final GpxImportExportStrategy strategy = new GpxImportExportStrategy();

    @Test
    void parseExternalGpxAddsFallbacksForRequiredTourFields() {
        String gpx = """
                <?xml version="1.0" encoding="UTF-8"?>
                <gpx version="1.1" creator="komoot" xmlns="http://www.topografix.com/GPX/1/1">
                  <metadata>
                    <name>External tour</name>
                  </metadata>
                  <trk>
                    <name>Labyrinthsteig</name>
                    <type>hike</type>
                    <trkseg>
                      <trkpt lat="46.422015" lon="11.586574">
                        <time>2025-07-15T20:28:08Z</time>
                      </trkpt>
                      <trkpt lat="46.423015" lon="11.586574">
                        <time>2025-07-15T20:58:08Z</time>
                      </trkpt>
                    </trkseg>
                  </trk>
                </gpx>
                """;

        TourExportFile parsed = strategy.parse(gpx.getBytes(StandardCharsets.UTF_8));

        assertThat(parsed.tours()).hasSize(1);
        TourExportFile.TourExport tour = parsed.tours().getFirst();
        assertThat(tour.name()).isEqualTo("Labyrinthsteig");
        assertThat(tour.fromLocation()).isEqualTo("46.422015, 11.586574");
        assertThat(tour.toLocation()).isEqualTo("46.423015, 11.586574");
        assertThat(tour.transportType()).isEqualTo("hike");
        assertThat(tour.distance()).isCloseTo(0.11, within(0.01));
        assertThat(tour.estimatedTime()).isEqualTo(0.5);
        assertThat(tour.routeGeometry()).isEqualTo("[[46.422015,11.586574],[46.423015,11.586574]]");
    }
}
