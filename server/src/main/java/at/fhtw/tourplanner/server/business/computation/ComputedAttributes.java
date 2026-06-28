package at.fhtw.tourplanner.server.business.computation;

import at.fhtw.tourplanner.server.model.TourLog;

import java.util.List;
import java.util.Objects;

/**
 * Pure functions that derive a tour's computed attributes from its logs.
 * <ul>
 *   <li><b>popularity</b> - number of recorded logs.</li>
 *   <li><b>child-friendliness</b> - a 0..100 score that grows when the recorded
 *       difficulty, time and distance are low (so easy, short, quick tours score high).</li>
 * </ul>
 * Kept free of framework dependencies so the logic is trivially unit-testable.
 */
public final class ComputedAttributes {

    private ComputedAttributes() {
    }

    public static int popularity(List<TourLog> logs) {
        return logs == null ? 0 : logs.size();
    }

    public static double childFriendliness(List<TourLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return 0.0;
        }

        double avgDifficulty = logs.stream()
                .map(TourLog::getDifficulty).filter(Objects::nonNull)
                .mapToInt(Integer::intValue).average().orElse(0.0); // expected 1..5
        double avgTime = logs.stream()
                .map(TourLog::getTotalTime).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).average().orElse(0.0); // hours
        double avgDistance = logs.stream()
                .map(TourLog::getTotalDistance).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).average().orElse(0.0); // km

        // Normalize each factor to 0..1 where 1 = most child-friendly.
        double difficultyScore = clamp01(1.0 - (avgDifficulty - 1.0) / 4.0); // diff 1 -> 1.0, diff 5 -> 0.0
        double timeScore = clamp01(1.0 - avgTime / 8.0);                     // 0h -> 1.0, >=8h -> 0.0
        double distanceScore = clamp01(1.0 - avgDistance / 30.0);            // 0km -> 1.0, >=30km -> 0.0

        // Difficulty weighs most, then time, then distance.
        double score = 0.5 * difficultyScore + 0.3 * timeScore + 0.2 * distanceScore;
        return Math.round(score * 1000.0) / 10.0; // one decimal place, 0..100
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
