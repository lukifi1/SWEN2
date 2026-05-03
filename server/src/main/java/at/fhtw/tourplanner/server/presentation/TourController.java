package at.fhtw.tourplanner.server.presentation;

import at.fhtw.tourplanner.server.business.TourService;
import at.fhtw.tourplanner.server.dto.TourCreateDto;
import at.fhtw.tourplanner.server.dto.TourResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TourController {

    private final TourService tourService;

    @GetMapping
    public List<TourResponseDto> getAllTours() {
        return tourService.getAllTours();
    }

    @GetMapping("/{id}")
    public TourResponseDto getTourById(@PathVariable Long id) {
        return tourService.getTourById(id);
    }

    @PostMapping
    public TourResponseDto createTour(@Valid @RequestBody TourCreateDto dto) {
        return tourService.createTour(dto);
    }

    @PutMapping("/{id}")
    public TourResponseDto updateTour(
            @PathVariable Long id,
            @Valid @RequestBody TourCreateDto dto
    ) {
        return tourService.updateTour(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteTour(@PathVariable Long id) {
        tourService.deleteTour(id);
    }

    @GetMapping("/search")
    public List<TourResponseDto> searchTours(@RequestParam String query) {
        return tourService.searchTours(query);
    }
}
