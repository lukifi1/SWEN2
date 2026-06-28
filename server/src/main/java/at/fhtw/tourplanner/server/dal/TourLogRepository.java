package at.fhtw.tourplanner.server.dal;

import at.fhtw.tourplanner.server.model.TourLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TourLogRepository extends JpaRepository<TourLog, Long> {

    List<TourLog> findByTour_IdOrderByDateTimeDesc(Long tourId);

    Optional<TourLog> findByIdAndTour_User_Username(Long id, String username);
}
