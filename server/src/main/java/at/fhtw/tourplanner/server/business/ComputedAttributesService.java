package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.business.computation.ComputedAttributes;
import at.fhtw.tourplanner.server.dal.TourLogRepository;
import at.fhtw.tourplanner.server.dal.TourRepository;
import at.fhtw.tourplanner.server.model.Tour;
import at.fhtw.tourplanner.server.model.TourLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Recomputes and persists a tour's derived attributes (popularity and
 * child-friendliness) from its current set of logs. Invoked whenever logs change.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ComputedAttributesService {

    private final TourLogRepository tourLogRepository;
    private final TourRepository tourRepository;

    @Transactional
    public void recompute(Tour tour) {
        List<TourLog> logs = tourLogRepository.findByTour_IdOrderByDateTimeDesc(tour.getId());
        tour.setPopularity(ComputedAttributes.popularity(logs));
        tour.setChildFriendliness(ComputedAttributes.childFriendliness(logs));
        tourRepository.save(tour);
        log.debug("Recomputed tour {}: popularity={}, childFriendliness={}",
                tour.getId(), tour.getPopularity(), tour.getChildFriendliness());
    }
}
