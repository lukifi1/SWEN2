package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.business.exceptions.TourNotFoundException;
import at.fhtw.tourplanner.server.business.ors.OpenRouteServiceClient;
import at.fhtw.tourplanner.server.business.ors.RouteResult;
import at.fhtw.tourplanner.server.dal.TourRepository;
import at.fhtw.tourplanner.server.dto.TourCreateDto;
import at.fhtw.tourplanner.server.dto.TourResponseDto;
import at.fhtw.tourplanner.server.model.Tour;
import at.fhtw.tourplanner.server.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Protects the tour CRUD rules in {@link TourService}. Critically: every read/write is
 * scoped to the owning user (a missing/foreign tour yields {@link TourNotFoundException}),
 * create always derives distance/time/geometry from ORS and starts with popularity 0,
 * and ORS is re-queried on update ONLY when a route-relevant field (from/to/transport)
 * actually changes - a name/description-only edit must NOT trigger an expensive ORS call.
 */
@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    @Mock
    private TourRepository tourRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private OpenRouteServiceClient openRouteServiceClient;

    @InjectMocks
    private TourService tourService;

    private static Tour existingTour() {
        return Tour.builder()
                .id(1L)
                .name("Old name")
                .description("Old description")
                .fromLocation("Vienna")
                .toLocation("Graz")
                .transportType("Car")
                .distance(200.0)
                .estimatedTime(2.0)
                .routeGeometry("[[1.0,2.0]]")
                .build();
    }

    @Test
    void getAllToursMapsEntitiesToDtos() {
        when(tourRepository.findByUser_UsernameOrderByIdDesc("alice"))
                .thenReturn(List.of(existingTour()));

        List<TourResponseDto> result = tourService.getAllTours("alice");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(1L);
        assertThat(result.getFirst().name()).isEqualTo("Old name");
    }

    @Test
    void getTourByIdThrowsWhenMissing() {
        when(tourRepository.findByIdAndUser_Username(99L, "alice")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourService.getTourById(99L, "alice"))
                .isInstanceOf(TourNotFoundException.class);
    }

    @Test
    void createTourCallsOrsAndSavesComputedRouteWithZeroPopularity() {
        User user = User.builder().username("alice").build();
        when(currentUserService.require("alice")).thenReturn(user);
        when(openRouteServiceClient.calculateRoute("Vienna", "Graz", "Car"))
                .thenReturn(new RouteResult(200.5, 2.25, "[[48.2,16.3],[47.0,15.4]]"));
        when(tourRepository.save(any(Tour.class))).thenAnswer(inv -> inv.getArgument(0));

        TourCreateDto dto = new TourCreateDto("Trip", "desc", "Vienna", "Graz", "Car", "img.png");
        TourResponseDto response = tourService.createTour(dto, "alice");

        ArgumentCaptor<Tour> saved = ArgumentCaptor.forClass(Tour.class);
        verify(tourRepository).save(saved.capture());
        Tour persisted = saved.getValue();
        assertThat(persisted.getDistance()).isEqualTo(200.5);
        assertThat(persisted.getEstimatedTime()).isEqualTo(2.25);
        assertThat(persisted.getRouteGeometry()).isEqualTo("[[48.2,16.3],[47.0,15.4]]");
        assertThat(persisted.getPopularity()).isZero();
        assertThat(persisted.getChildFriendliness()).isEqualTo(0.0);
        assertThat(persisted.getUser()).isSameAs(user);
        assertThat(response.distance()).isEqualTo(200.5);
    }

    @Test
    void updateTourRecalculatesRouteWhenLocationChanges() {
        when(tourRepository.findByIdAndUser_Username(1L, "alice"))
                .thenReturn(Optional.of(existingTour()));
        when(openRouteServiceClient.calculateRoute("Vienna", "Salzburg", "Car"))
                .thenReturn(new RouteResult(300.0, 3.0, "[[1.0,1.0]]"));
        when(tourRepository.save(any(Tour.class))).thenAnswer(inv -> inv.getArgument(0));

        // toLocation changed Graz -> Salzburg
        TourCreateDto dto = new TourCreateDto("Old name", "Old description", "Vienna", "Salzburg", "Car", null);
        TourResponseDto response = tourService.updateTour(1L, dto, "alice");

        verify(openRouteServiceClient).calculateRoute("Vienna", "Salzburg", "Car");
        assertThat(response.distance()).isEqualTo(300.0);
        assertThat(response.estimatedTime()).isEqualTo(3.0);
    }

    @Test
    void updateTourDoesNotCallOrsWhenOnlyNameOrDescriptionChange() {
        when(tourRepository.findByIdAndUser_Username(1L, "alice"))
                .thenReturn(Optional.of(existingTour()));
        when(tourRepository.save(any(Tour.class))).thenAnswer(inv -> inv.getArgument(0));

        // from/to/transport unchanged; only name + description differ
        TourCreateDto dto = new TourCreateDto("New name", "New description", "Vienna", "Graz", "Car", null);
        TourResponseDto response = tourService.updateTour(1L, dto, "alice");

        verify(openRouteServiceClient, never()).calculateRoute(anyString(), anyString(), anyString());
        // original computed route is preserved
        assertThat(response.distance()).isEqualTo(200.0);
        assertThat(response.name()).isEqualTo("New name");
    }

    @Test
    void deleteTourThrowsWhenMissing() {
        when(tourRepository.findByIdAndUser_Username(42L, "alice")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourService.deleteTour(42L, "alice"))
                .isInstanceOf(TourNotFoundException.class);

        verify(tourRepository, never()).delete(any());
    }

    @Test
    void getTourByIdReturnsDtoWhenFound() {
        Tour tour = existingTour();
        when(tourRepository.findByIdAndUser_Username(1L, "alice")).thenReturn(Optional.of(tour));

        TourResponseDto result = tourService.getTourById(1L, "alice");

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Old name");
        assertThat(result.fromLocation()).isEqualTo("Vienna");
    }

    @Test
    void deleteTourDeletesWhenOwned() {
        Tour tour = existingTour();
        when(tourRepository.findByIdAndUser_Username(1L, "alice")).thenReturn(Optional.of(tour));

        tourService.deleteTour(1L, "alice");

        verify(tourRepository).delete(tour);
    }
}
