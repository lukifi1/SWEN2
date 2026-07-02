import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges,
  ViewChild,
  inject,
} from '@angular/core';
import * as L from 'leaflet';
import { Tour } from '../../core/models/tour.model';
import { DataApiService } from '../../core/api/data-api.service';
import {
  childFriendlinessLabel,
  formatDistance,
  formatTime,
  popularityLabel,
} from '../../core/tour-format';
import { ActionButtonComponent } from '../../shared/action-button/action-button.component';
import { TourLogsComponent } from './tour-logs.component';

@Component({
  selector: 'app-tour-detail',
  standalone: true,
  imports: [ActionButtonComponent, TourLogsComponent],
  templateUrl: './tour-detail.component.html',
  styleUrl: './tour-detail.component.css',
})
export class TourDetailComponent implements AfterViewInit, OnChanges, OnDestroy {
  @Input({ required: true }) tour!: Tour;
  @Output() editTour = new EventEmitter<void>();
  @Output() deleteTour = new EventEmitter<void>();

  @ViewChild('mapContainer') private mapEl!: ElementRef<HTMLElement>;

  private dataApi = inject(DataApiService);
  private map?: L.Map;
  private routeLayer?: L.LayerGroup;

  protected readonly formatDistance = formatDistance;
  protected readonly formatTime = formatTime;
  protected readonly popularityLabel = popularityLabel;
  protected readonly childFriendlinessLabel = childFriendlinessLabel;

  /** Derived from the input so it stays stable during change detection. */
  get hasRoute(): boolean {
    return this.parseGeometry(this.tour.routeGeometry).length > 0;
  }

  ngAfterViewInit(): void {
    this.initMap();
    this.drawRoute();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['tour'] && !changes['tour'].firstChange) {
      this.drawRoute();
    }
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }

  imageUrl(): string | null {
    return this.tour.imagePath ? this.dataApi.imageUrl(this.tour.imagePath) : null;
  }

  private initMap(): void {
    this.map = L.map(this.mapEl.nativeElement, { center: [48.2, 16.37], zoom: 7 });
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors',
    }).addTo(this.map);
    setTimeout(() => this.map?.invalidateSize(), 60);
  }

  private drawRoute(): void {
    if (!this.map) {
      return;
    }
    this.routeLayer?.remove();
    const coordinates = this.parseGeometry(this.tour.routeGeometry);
    if (coordinates.length === 0) {
      this.map.setView([48.2, 16.37], 6);
      return;
    }

    const polyline = L.polyline(coordinates, { color: '#2563eb', weight: 4 });
    const start = L.circleMarker(coordinates[0], {
      radius: 7, color: '#16a34a', fillColor: '#16a34a', fillOpacity: 1,
    });
    const end = L.circleMarker(coordinates[coordinates.length - 1], {
      radius: 7, color: '#dc2626', fillColor: '#dc2626', fillOpacity: 1,
    });

    this.routeLayer = L.layerGroup([polyline, start, end]).addTo(this.map);
    this.map.fitBounds(polyline.getBounds(), { padding: [25, 25] });
    setTimeout(() => this.map?.invalidateSize(), 60);
  }

  private parseGeometry(json: string | null): L.LatLngTuple[] {
    if (!json) {
      return [];
    }
    try {
      const parsed = JSON.parse(json);
      return Array.isArray(parsed) ? (parsed as L.LatLngTuple[]) : [];
    } catch {
      return [];
    }
  }
}
