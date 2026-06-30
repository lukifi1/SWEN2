package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.dal.TourRepository;
import at.fhtw.tourplanner.server.dto.StatsDto;
import at.fhtw.tourplanner.server.model.Tour;
import at.fhtw.tourplanner.server.model.TourLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Protects the dashboard aggregation in {@link StatsService} (the application's unique
 * feature). Verifies the non-trivial reductions: difficulty distribution always carries
 * all five buckets, the transport-type breakdown counts tours per type, the popularity
 * ranking is sorted descending, and distance-over-time aggregates log distances per
 * calendar month.
 */
@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private TourRepository tourRepository;

    @InjectMocks
    private StatsService statsService;

    private static TourLog log(int difficulty, int rating, double distance, double time, LocalDateTime when) {
        return TourLog.builder()
                .difficulty(difficulty)
                .rating(rating)
                .totalDistance(distance)
                .totalTime(time)
                .dateTime(when)
                .build();
    }

    private void stubTwoTours() {
        Tour alps = Tour.builder()
                .id(1L).name("Alps").transportType("Hiking")
                .popularity(2).childFriendliness(80.0).distance(15.0)
                .logs(List.of(
                        log(3, 4, 10.0, 2.0, LocalDateTime.of(2026, 3, 5, 9, 0)),
                        log(3, 5, 5.0, 1.0, LocalDateTime.of(2026, 3, 20, 9, 0))))
                .build();
        Tour coast = Tour.builder()
                .id(2L).name("Coast").transportType("Car")
                .popularity(1).childFriendliness(50.0).distance(20.0)
                .logs(List.of(log(1, 2, 20.0, 4.0, LocalDateTime.of(2026, 4, 10, 9, 0))))
                .build();
        when(tourRepository.findByUser_UsernameOrderByIdDesc("alice"))
                .thenReturn(List.of(alps, coast));
    }

    @Test
    void totalsCountToursAndLogs() {
        stubTwoTours();

        StatsDto stats = statsService.computeStats("alice");

        assertThat(stats.totalTours()).isEqualTo(2);
        assertThat(stats.totalLogs()).isEqualTo(3);
    }

    @Test
    void difficultyDistributionKeepsAllFiveBuckets() {
        stubTwoTours();

        StatsDto stats = statsService.computeStats("alice");

        assertThat(stats.difficultyDistribution()).containsOnlyKeys(1, 2, 3, 4, 5);
        assertThat(stats.difficultyDistribution().get(1)).isEqualTo(1L);
        assertThat(stats.difficultyDistribution().get(3)).isEqualTo(2L);
        assertThat(stats.difficultyDistribution().get(2)).isEqualTo(0L);
    }

    @Test
    void transportTypeBreakdownCountsToursPerType() {
        stubTwoTours();

        StatsDto stats = statsService.computeStats("alice");

        assertThat(stats.transportTypeBreakdown())
                .containsEntry("Hiking", 1L)
                .containsEntry("Car", 1L);
    }

    @Test
    void popularityRankingIsSortedDescending() {
        stubTwoTours();

        StatsDto stats = statsService.computeStats("alice");

        assertThat(stats.popularityRanking())
                .extracting(StatsDto.TourRank::name)
                .containsExactly("Alps", "Coast");
        assertThat(stats.popularityRanking().getFirst().popularity()).isEqualTo(2);
    }

    @Test
    void distanceOverTimeAggregatesPerMonth() {
        stubTwoTours();

        StatsDto stats = statsService.computeStats("alice");

        assertThat(stats.distanceOverTime())
                .extracting(StatsDto.MonthlyDistance::month)
                .containsExactly("2026-03", "2026-04");
        assertThat(stats.distanceOverTime().getFirst().distance()).isEqualTo(15.0); // 10 + 5
        assertThat(stats.distanceOverTime().get(1).distance()).isEqualTo(20.0);
    }
}
