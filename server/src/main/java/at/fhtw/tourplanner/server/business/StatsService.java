package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.dal.TourRepository;
import at.fhtw.tourplanner.server.dto.StatsDto;
import at.fhtw.tourplanner.server.model.Tour;
import at.fhtw.tourplanner.server.model.TourLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Computes aggregated statistics for the statistics dashboard (unique feature).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final TourRepository tourRepository;

    @Transactional(readOnly = true)
    public StatsDto computeStats(String username) {
        List<Tour> tours = tourRepository.findByUser_UsernameOrderByIdDesc(username);
        List<TourLog> logs = tours.stream()
                .flatMap(tour -> tour.getLogs().stream())
                .toList();
        log.debug("Computing stats for '{}': {} tours, {} logs", username, tours.size(), logs.size());

        return new StatsDto(
                tours.size(),
                logs.size(),
                sum(tours, Tour::getDistance),
                sum(logs, TourLog::getTotalDistance),
                sum(logs, TourLog::getTotalTime),
                average(tours, Tour::getDistance),
                averageInt(logs, TourLog::getRating),
                countByBucket(logs, TourLog::getDifficulty),
                countByBucket(logs, TourLog::getRating),
                transportBreakdown(tours),
                popularityRanking(tours),
                distanceOverTime(logs));
    }

    private <T> double sum(List<T> items, java.util.function.Function<T, Double> extractor) {
        return items.stream().map(extractor).filter(Objects::nonNull).mapToDouble(Double::doubleValue).sum();
    }

    private <T> double average(List<T> items, java.util.function.Function<T, Double> extractor) {
        return round(items.stream().map(extractor).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).average().orElse(0.0));
    }

    private <T> double averageInt(List<T> items, java.util.function.Function<T, Integer> extractor) {
        return round(items.stream().map(extractor).filter(Objects::nonNull)
                .mapToInt(Integer::intValue).average().orElse(0.0));
    }

    /** Counts logs per bucket value 1..5 (difficulty or rating), keeping all 5 buckets. */
    private <T> Map<Integer, Long> countByBucket(List<T> items, java.util.function.Function<T, Integer> extractor) {
        Map<Integer, Long> result = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            result.put(i, 0L);
        }
        for (T item : items) {
            Integer value = extractor.apply(item);
            if (value != null && value >= 1 && value <= 5) {
                result.merge(value, 1L, Long::sum);
            }
        }
        return result;
    }

    private Map<String, Long> transportBreakdown(List<Tour> tours) {
        return tours.stream()
                .map(Tour::getTransportType)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(type -> type, LinkedHashMap::new, Collectors.counting()));
    }

    private List<StatsDto.TourRank> popularityRanking(List<Tour> tours) {
        return tours.stream()
                .sorted(Comparator.comparingInt((Tour t) -> t.getPopularity() == null ? 0 : t.getPopularity())
                        .reversed())
                .limit(5)
                .map(t -> new StatsDto.TourRank(
                        t.getName(),
                        t.getPopularity() == null ? 0 : t.getPopularity(),
                        t.getChildFriendliness() == null ? 0.0 : t.getChildFriendliness()))
                .toList();
    }

    private List<StatsDto.MonthlyDistance> distanceOverTime(List<TourLog> logs) {
        Map<String, Double> byMonth = new TreeMap<>();
        for (TourLog log : logs) {
            if (log.getDateTime() == null) {
                continue;
            }
            String month = YearMonth.from(log.getDateTime()).toString(); // e.g. 2026-03
            double distance = log.getTotalDistance() == null ? 0.0 : log.getTotalDistance();
            byMonth.merge(month, distance, Double::sum);
        }
        return byMonth.entrySet().stream()
                .map(e -> new StatsDto.MonthlyDistance(e.getKey(), round(e.getValue())))
                .toList();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
