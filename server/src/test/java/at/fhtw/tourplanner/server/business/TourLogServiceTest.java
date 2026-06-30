package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.business.exceptions.TourLogNotFoundException;
import at.fhtw.tourplanner.server.business.exceptions.TourNotFoundException;
import at.fhtw.tourplanner.server.dal.TourLogRepository;
import at.fhtw.tourplanner.server.dal.TourRepository;
import at.fhtw.tourplanner.server.dto.TourLogCreateDto;
import at.fhtw.tourplanner.server.dto.TourLogResponseDto;
import at.fhtw.tourplanner.server.model.Tour;
import at.fhtw.tourplanner.server.model.TourLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Protects the tour-log rules in {@link TourLogService}. Critically: every log
 * operation must first verify the parent tour belongs to the caller (otherwise
 * {@link TourNotFoundException}/{@link TourLogNotFoundException}), and any change to
 * the set of logs must trigger {@link ComputedAttributesService#recompute} so the
 * derived popularity/child-friendliness on the tour stay consistent.
 */
@ExtendWith(MockitoExtension.class)
class TourLogServiceTest {

    @Mock
    private TourLogRepository tourLogRepository;
    @Mock
    private TourRepository tourRepository;
    @Mock
    private ComputedAttributesService computedAttributesService;

    @InjectMocks
    private TourLogService tourLogService;

    private static TourLogCreateDto dto() {
        return new TourLogCreateDto(LocalDateTime.of(2026, 3, 1, 10, 0), "nice", 3, 12.0, 1.5, 4);
    }

    @Test
    void createLogSavesLogAndRecomputesTour() {
        Tour tour = Tour.builder().id(1L).name("Trip").build();
        when(tourRepository.findByIdAndUser_Username(1L, "alice")).thenReturn(Optional.of(tour));
        when(tourLogRepository.save(any(TourLog.class))).thenAnswer(inv -> {
            TourLog l = inv.getArgument(0);
            l.setId(7L);
            return l;
        });

        TourLogResponseDto response = tourLogService.createLog(1L, dto(), "alice");

        ArgumentCaptor<TourLog> saved = ArgumentCaptor.forClass(TourLog.class);
        verify(tourLogRepository).save(saved.capture());
        assertThat(saved.getValue().getTour()).isSameAs(tour);
        assertThat(saved.getValue().getComment()).isEqualTo("nice");
        verify(computedAttributesService).recompute(tour);
        assertThat(response.id()).isEqualTo(7L);
    }

    @Test
    void createLogOnNotOwnedTourThrowsAndDoesNotSave() {
        when(tourRepository.findByIdAndUser_Username(1L, "alice")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourLogService.createLog(1L, dto(), "alice"))
                .isInstanceOf(TourNotFoundException.class);

        verify(tourLogRepository, never()).save(any());
        verify(computedAttributesService, never()).recompute(any());
    }

    @Test
    void updateLogOnMissingLogThrows() {
        Tour tour = Tour.builder().id(1L).name("Trip").build();
        when(tourRepository.findByIdAndUser_Username(1L, "alice")).thenReturn(Optional.of(tour));
        when(tourLogRepository.findByIdAndTour_User_Username(5L, "alice")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourLogService.updateLog(1L, 5L, dto(), "alice"))
                .isInstanceOf(TourLogNotFoundException.class);

        verify(tourLogRepository, never()).save(any());
        verify(computedAttributesService, never()).recompute(any());
    }

    @Test
    void deleteLogRemovesLogAndRecomputesTour() {
        Tour tour = Tour.builder().id(1L).name("Trip").build();
        TourLog log = TourLog.builder().id(5L).tour(tour).build();
        when(tourRepository.findByIdAndUser_Username(1L, "alice")).thenReturn(Optional.of(tour));
        when(tourLogRepository.findByIdAndTour_User_Username(5L, "alice")).thenReturn(Optional.of(log));

        tourLogService.deleteLog(1L, 5L, "alice");

        verify(tourLogRepository).delete(log);
        verify(computedAttributesService).recompute(tour);
    }
}
