package at.fhtw.tourplanner.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 3000)
    private String description;

    @Column(nullable = false)
    private String fromLocation;

    @Column(nullable = false)
    private String toLocation;

    @Column(nullable = false)
    private String transportType;

    /** Distance in kilometers, computed from OpenRouteService. */
    private Double distance;

    /** Estimated travel time in hours, computed from OpenRouteService. */
    private Double estimatedTime;

    /** Route geometry as a JSON array of [lat, lng] pairs (from ORS), used by Leaflet. */
    @Column(columnDefinition = "TEXT")
    private String routeGeometry;

    private String imagePath;

    private Integer popularity;

    private Double childFriendliness;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TourLog> logs = new ArrayList<>();
}