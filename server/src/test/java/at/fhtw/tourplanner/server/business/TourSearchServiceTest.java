package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.dal.TourRepository;
import at.fhtw.tourplanner.server.dto.TourResponseDto;
import at.fhtw.tourplanner.server.model.Tour;
import at.fhtw.tourplanner.server.model.TourLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Protects the full-text search in {@link TourSearchService}. A tour is reduced to a
 * single searchable string built from its own fields, all its logs, AND the textual
 * labels for the computed attributes - so this verifies the less obvious cases: a
 * blank query returns everything, a log comment is searchable, and a computed label
 * ("child friendly") matches a tour whose child-friendliness score is high enough.
 */
@ExtendWith(MockitoExtension.class)
class TourSearchServiceTest {

    @Mock
    private TourRepository tourRepository;

    @InjectMocks
    private TourSearchService tourSearchService;

    private static Tour tour(long id, String name, double childFriendliness, int popularity, TourLog... logs) {
        return Tour.builder()
                .id(id)
                .name(name)
                .description("desc")
                .fromLocation("A")
                .toLocation("B")
                .transportType("Car")
                .childFriendliness(childFriendliness)
                .popularity(popularity)
                .logs(List.of(logs))
                .build();
    }

    @Test
    void blankQueryReturnsAllTours() {
        when(tourRepository.findByUser_UsernameOrderByIdDesc("alice"))
                .thenReturn(List.of(tour(1L, "Alps", 10.0, 0), tour(2L, "Coast", 90.0, 3)));

        List<TourResponseDto> result = tourSearchService.search("   ", "alice");

        assertThat(result).hasSize(2);
    }

    @Test
    void matchesByTourName() {
        when(tourRepository.findByUser_UsernameOrderByIdDesc("alice"))
                .thenReturn(List.of(tour(1L, "Alps hike", 10.0, 0), tour(2L, "Coast", 90.0, 3)));

        List<TourResponseDto> result = tourSearchService.search("alps", "alice");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Alps hike");
    }

    @Test
    void matchesByLogComment() {
        TourLog log = TourLog.builder().comment("stunning waterfall view").difficulty(2).rating(5).build();
        when(tourRepository.findByUser_UsernameOrderByIdDesc("alice"))
                .thenReturn(List.of(tour(1L, "Alps", 10.0, 1, log), tour(2L, "Coast", 90.0, 3)));

        List<TourResponseDto> result = tourSearchService.search("waterfall", "alice");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Alps");
    }

    @Test
    void matchesByComputedChildFriendlinessLabel() {
        // childFriendliness >= 66 -> "child friendly kid friendly family friendly ...";
        // a low score -> "not child friendly ...". "family friendly" is unique to the
        // high bucket, so it must match only the high-scoring tour.
        when(tourRepository.findByUser_UsernameOrderByIdDesc("alice"))
                .thenReturn(List.of(tour(1L, "Hard climb", 10.0, 1), tour(2L, "Easy stroll", 90.0, 2)));

        List<TourResponseDto> result = tourSearchService.search("family friendly", "alice");

        assertThat(result).extracting(TourResponseDto::name).containsExactly("Easy stroll");
    }

    @Test
    void nonMatchingQueryReturnsEmpty() {
        when(tourRepository.findByUser_UsernameOrderByIdDesc("alice"))
                .thenReturn(List.of(tour(1L, "Alps", 10.0, 0), tour(2L, "Coast", 90.0, 3)));

        List<TourResponseDto> result = tourSearchService.search("nonexistentterm", "alice");

        assertThat(result).isEmpty();
    }
}
