package at.fhtw.tourplanner.server.presentation;

import at.fhtw.tourplanner.server.business.TourSearchService;
import at.fhtw.tourplanner.server.business.TourService;
import at.fhtw.tourplanner.server.dto.TourCreateDto;
import at.fhtw.tourplanner.server.dto.TourResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;
    private final TourSearchService tourSearchService;

    @GetMapping
    public List<TourResponseDto> getAllTours(Authentication auth) {
        return tourService.getAllTours(auth.getName());
    }

    @GetMapping("/search")
    public List<TourResponseDto> search(@RequestParam(name = "q", defaultValue = "") String query,
                                        Authentication auth) {
        return tourSearchService.search(query, auth.getName());
    }

    @GetMapping("/{id}")
    public TourResponseDto getTourById(@PathVariable Long id, Authentication auth) {
        return tourService.getTourById(id, auth.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TourResponseDto createTour(@Valid @RequestBody TourCreateDto dto, Authentication auth) {
        return tourService.createTour(dto, auth.getName());
    }

    @PutMapping("/{id}")
    public TourResponseDto updateTour(@PathVariable Long id,
                                      @Valid @RequestBody TourCreateDto dto,
                                      Authentication auth) {
        return tourService.updateTour(id, dto, auth.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTour(@PathVariable Long id, Authentication auth) {
        tourService.deleteTour(id, auth.getName());
    }
}
