package at.fhtw.tourplanner.server.dal;

import at.fhtw.tourplanner.server.model.Tour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Data access for {@link Tour} entities. All finders are scoped by the owning
 * user so a user can never see another user's tours. Spring Data generates
 * parameterized queries, which protects against SQL injection.
 */
public interface TourRepository extends JpaRepository<Tour, Long> {

    List<Tour> findByUser_UsernameOrderByIdDesc(String username);

    Optional<Tour> findByIdAndUser_Username(Long id, String username);
}
