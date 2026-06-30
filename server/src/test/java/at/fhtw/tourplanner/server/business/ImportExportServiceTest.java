package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.business.importexport.ImportExportStrategy;
import at.fhtw.tourplanner.server.business.importexport.TourExportFile;
import at.fhtw.tourplanner.server.dal.TourRepository;
import at.fhtw.tourplanner.server.model.Tour;
import at.fhtw.tourplanner.server.model.TourLog;
import at.fhtw.tourplanner.server.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Protects the orchestration in {@link ImportExportService}, which delegates the
 * concrete format to a pluggable {@link ImportExportStrategy} (Strategy pattern).
 * Verifies that export maps the user's tours (and their logs) into the format-agnostic
 * export model handed to the strategy, and that import parses, persists each tour, and
 * recomputes its derived attributes - returning the imported count (0 for an empty file).
 */
@ExtendWith(MockitoExtension.class)
class ImportExportServiceTest {

    @Mock
    private TourRepository tourRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private ComputedAttributesService computedAttributesService;
    @Mock
    private ImportExportStrategy strategy;

    @InjectMocks
    private ImportExportService importExportService;

    @Test
    void exportMapsUsersToursIntoExportModelAndDelegatesToStrategy() {
        Tour tour = Tour.builder()
                .id(1L).name("Alps").description("scenic")
                .fromLocation("Vienna").toLocation("Graz").transportType("Hiking")
                .distance(42.0).estimatedTime(1.2).routeGeometry("[[1.0,2.0]]").imagePath("img.png")
                .logs(List.of(TourLog.builder()
                        .dateTime(LocalDateTime.of(2026, 3, 1, 9, 0))
                        .comment("nice").difficulty(2).totalDistance(10.0).totalTime(1.0).rating(5)
                        .build()))
                .build();
        when(tourRepository.findByUser_UsernameOrderByIdDesc("alice")).thenReturn(List.of(tour));
        when(strategy.export(any(TourExportFile.class))).thenReturn("BYTES".getBytes());

        byte[] result = importExportService.export("alice");

        assertThat(result).isEqualTo("BYTES".getBytes());
        ArgumentCaptor<TourExportFile> captor = ArgumentCaptor.forClass(TourExportFile.class);
        verify(strategy).export(captor.capture());
        TourExportFile file = captor.getValue();
        assertThat(file.tours()).hasSize(1);
        TourExportFile.TourExport exported = file.tours().getFirst();
        assertThat(exported.name()).isEqualTo("Alps");
        assertThat(exported.transportType()).isEqualTo("Hiking");
        assertThat(exported.logs()).hasSize(1);
        assertThat(exported.logs().getFirst().comment()).isEqualTo("nice");
    }

    @Test
    void importParsesSavesEachTourAndRecomputes() {
        User user = User.builder().username("alice").build();
        when(currentUserService.require("alice")).thenReturn(user);

        TourExportFile.TourExport t1 = new TourExportFile.TourExport(
                "Alps", "d", "A", "B", "Car", 1.0, 1.0, null, null,
                List.of(new TourExportFile.TourLogExport(LocalDateTime.of(2026, 1, 1, 8, 0), "c", 1, 1.0, 1.0, 1)));
        TourExportFile.TourExport t2 = new TourExportFile.TourExport(
                "Coast", "d", "A", "B", "Bike", 2.0, 2.0, null, null, List.of());
        when(strategy.parse(any())).thenReturn(new TourExportFile("1.0", List.of(t1, t2)));
        when(tourRepository.save(any(Tour.class))).thenAnswer(inv -> inv.getArgument(0));

        int imported = importExportService.importData("BYTES".getBytes(), "alice");

        assertThat(imported).isEqualTo(2);
        verify(tourRepository, times(2)).save(any(Tour.class));
        verify(computedAttributesService, times(2)).recompute(any(Tour.class));
    }

    @Test
    void importEmptyFileReturnsZero() {
        User user = User.builder().username("alice").build();
        when(currentUserService.require("alice")).thenReturn(user);
        when(strategy.parse(any())).thenReturn(new TourExportFile("1.0", List.of()));

        int imported = importExportService.importData(new byte[0], "alice");

        assertThat(imported).isZero();
        verify(tourRepository, never()).save(any());
        verify(computedAttributesService, never()).recompute(any());
    }
}
