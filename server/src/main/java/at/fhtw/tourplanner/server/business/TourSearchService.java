package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.business.computation.ComputedAttributeLabels;
import at.fhtw.tourplanner.server.business.mapper.TourMapper;
import at.fhtw.tourplanner.server.dal.TourRepository;
import at.fhtw.tourplanner.server.dto.TourResponseDto;
import at.fhtw.tourplanner.server.model.Tour;
import at.fhtw.tourplanner.server.model.TourLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Full-text search over the authenticated user's tours. Each tour is reduced to
 * a single searchable text built from its own fields, the fields of all its
 * logs, and textual labels for the computed attributes (popularity and
 * child-friendliness). A tour matches when that text contains the query.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TourSearchService {

    private final TourRepository tourRepository;

    @Transactional(readOnly = true)
    public List<TourResponseDto> search(String query, String username) {
        List<Tour> tours = tourRepository.findByUser_UsernameOrderByIdDesc(username);

        if (query == null || query.isBlank()) {
            return tours.stream().map(TourMapper::toResponse).toList();
        }

        String needle = query.toLowerCase(Locale.ROOT).trim();
        log.debug("Searching {} tours of '{}' for '{}'", tours.size(), username, needle);

        return tours.stream()
                .filter(tour -> searchableText(tour).contains(needle))
                .map(TourMapper::toResponse)
                .toList();
    }

    /** Builds the combined, lower-cased searchable text for one tour. */
    private String searchableText(Tour tour) {
        StringBuilder sb = new StringBuilder();
        append(sb, tour.getName());
        append(sb, tour.getDescription());
        append(sb, tour.getFromLocation());
        append(sb, tour.getToLocation());
        append(sb, tour.getTransportType());
        append(sb, "distance " + tour.getDistance());
        append(sb, "time " + tour.getEstimatedTime());
        // computed values + their labels
        append(sb, "popularity " + tour.getPopularity());
        append(sb, ComputedAttributeLabels.popularityLabel(tour.getPopularity()));
        append(sb, "childfriendliness " + tour.getChildFriendliness());
        append(sb, ComputedAttributeLabels.childFriendlinessLabel(tour.getChildFriendliness()));
        // tour logs
        for (TourLog logEntry : tour.getLogs()) {
            append(sb, logEntry.getComment());
            append(sb, "difficulty " + logEntry.getDifficulty());
            append(sb, "rating " + logEntry.getRating());
            append(sb, "distance " + logEntry.getTotalDistance());
            append(sb, "time " + logEntry.getTotalTime());
            if (logEntry.getDateTime() != null) {
                append(sb, logEntry.getDateTime().toString());
            }
        }
        return sb.toString().toLowerCase(Locale.ROOT);
    }

    private void append(StringBuilder sb, Object value) {
        if (value != null) {
            sb.append(' ').append(value).append(' ');
        }
    }
}
