package at.fhtw.tourplanner.server.business.computation;

import at.fhtw.tourplanner.server.model.TourLog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Protects the pure derivation rules in {@link ComputedAttributes}: popularity is
 * simply the number of logs (0 for null/empty), and child-friendliness is a 0..100
 * score that is highest for easy/short/quick tours and lowest for hard/long/slow ones.
 * These values feed the UI badges and the search index, so the exact boundary
 * behaviour (0.0 / 100.0 / a monotone middle) must stay stable.
 */
class ComputedAttributesTest {

    private static TourLog log(int difficulty, double totalTime, double totalDistance) {
        return TourLog.builder()
                .difficulty(difficulty)
                .totalTime(totalTime)
                .totalDistance(totalDistance)
                .build();
    }

    @Test
    void popularityCountsTheNumberOfLogs() {
        List<TourLog> logs = List.of(log(1, 0, 0), log(2, 1, 1), log(3, 2, 2));

        assertThat(ComputedAttributes.popularity(logs)).isEqualTo(3);
    }

    @Test
    void popularityIsZeroForNullList() {
        assertThat(ComputedAttributes.popularity(null)).isZero();
    }

    @Test
    void popularityIsZeroForEmptyList() {
        assertThat(ComputedAttributes.popularity(List.of())).isZero();
    }

    @Test
    void childFriendlinessIsZeroForEmptyLogs() {
        assertThat(ComputedAttributes.childFriendliness(List.of())).isEqualTo(0.0);
    }

    @Test
    void childFriendlinessIsZeroForNullLogs() {
        assertThat(ComputedAttributes.childFriendliness(null)).isEqualTo(0.0);
    }

    @Test
    void easiestPossibleTourScoresHundred() {
        // difficulty 1, no time, no distance => every normalized factor is 1.0
        double score = ComputedAttributes.childFriendliness(List.of(log(1, 0.0, 0.0)));

        assertThat(score).isEqualTo(100.0);
    }

    @Test
    void hardestPossibleTourScoresZero() {
        // difficulty 5, 8h, 30km => every normalized factor clamps to 0.0
        double score = ComputedAttributes.childFriendliness(List.of(log(5, 8.0, 30.0)));

        assertThat(score).isEqualTo(0.0);
    }

    @Test
    void mediumTourScoresStrictlyBetweenZeroAndHundred() {
        double score = ComputedAttributes.childFriendliness(List.of(log(3, 4.0, 15.0)));

        assertThat(score).isStrictlyBetween(0.0, 100.0);
        // 0.5*0.5 + 0.3*0.5 + 0.2*0.5 = 0.5 -> 50.0
        assertThat(score).isEqualTo(50.0);
    }
}
