import { Component, EventEmitter, Input, Output, AfterViewInit, OnDestroy, OnChanges, SimpleChanges } from '@angular/core';

import { Tour, TourLog } from '../shared/models/tour.model';
import { ActionButtonComponent } from '../shared/action-button/action-button.component';
import { TourLogsComponent } from '../tour-logs/tour-logs.component';
import * as L from 'leaflet';

@Component({
  selector: 'app-tour-detail',
  standalone: true,
  imports: [ActionButtonComponent, TourLogsComponent],
  templateUrl: './tour-detail.component.html',
  styleUrls: ['./tour-detail.component.css']
})
export class TourDetailComponent implements AfterViewInit, OnDestroy, OnChanges {
  @Input({ required: true }) tour!: Tour;

  @Output() editTour = new EventEmitter<void>();
  @Output() deleteTour = new EventEmitter<void>();
  @Output() tourUpdated = new EventEmitter<Tour>();

  private map!: L.Map;

  // Lifecycle: After HTML is ready
  ngAfterViewInit(): void {
    this.initMap();
  }

  // Lifecycle: When switching between different tours
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['tour'] && !changes['tour'].firstChange && this.map) {
      // Logic to reset map view or update markers when tour changes
      this.map.setView([this.tour.longitude, this.tour.latitude], 12);
    }
    if (this.map) {
      setTimeout(() => {
        this.map.invalidateSize();
      }, 100);
    }
  }

  private initMap(): void {
    // 1. Initialize map
    this.map = L.map('map', {
      center: [this.tour.longitude, this.tour.latitude],
      zoom: 12
    });

    // 2. Add OpenStreetMap tiles
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    // Leaflet sometimes misses container size on init
    setTimeout(() => {
      this.map.invalidateSize();
    }, 100);
  }

  updateLogs(updatedLogs: TourLog[]) {
    this.tourUpdated.emit({ ...this.tour, logs: updatedLogs });
  }

  // Lifecycle: Clean up to prevent memory leaks
  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }
}
