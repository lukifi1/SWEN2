package at.fhtw.tourplanner.server.presentation;

import at.fhtw.tourplanner.server.business.TourLogService;
import at.fhtw.tourplanner.server.dto.TourLogCreateDto;
import at.fhtw.tourplanner.server.dto.TourLogResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tours/{tourId}/logs")
@RequiredArgsConstructor
public class TourLogController {

    private final TourLogService tourLogService;

    @GetMapping
    public List<TourLogResponseDto> getLogs(@PathVariable Long tourId, Authentication auth) {
        return tourLogService.getLogsForTour(tourId, auth.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TourLogResponseDto createLog(@PathVariable Long tourId,
                                        @Valid @RequestBody TourLogCreateDto dto,
                                        Authentication auth) {
        return tourLogService.createLog(tourId, dto, auth.getName());
    }

    @PutMapping("/{logId}")
    public TourLogResponseDto updateLog(@PathVariable Long tourId,
                                        @PathVariable Long logId,
                                        @Valid @RequestBody TourLogCreateDto dto,
                                        Authentication auth) {
        return tourLogService.updateLog(tourId, logId, dto, auth.getName());
    }

    @DeleteMapping("/{logId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLog(@PathVariable Long tourId,
                          @PathVariable Long logId,
                          Authentication auth) {
        tourLogService.deleteLog(tourId, logId, auth.getName());
    }
}
