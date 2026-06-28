package at.fhtw.tourplanner.server.dto;

import java.util.List;
import java.util.Map;

/**
 * Aggregated statistics over a user's tours and logs, consumed by the
 * statistics dashboard (the application's unique feature).
 */
public record StatsDto(
        long totalTours,
        long totalLogs,
        double totalTourDistance,
        double totalLoggedDistance,
        double totalLoggedTime,
        double averageTourDistance,
        double averageRating,
        Map<Integer, Long> difficultyDistribution,
        Map<Integer, Long> ratingDistribution,
        Map<String, Long> transportTypeBreakdown,
        List<TourRank> popularityRanking,
        List<MonthlyDistance> distanceOverTime
) {
    public record TourRank(String name, int popularity, double childFriendliness) {
    }

    public record MonthlyDistance(String month, double distance) {
    }
}
