import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Tour, TourLog } from '../shared/models/tour.model';
import { ActionButtonComponent } from '../shared/action-button/action-button.component';
import { TourLogsComponent } from '../tour-logs/tour-logs.component';

@Component({
  selector: 'app-tour-detail',
  standalone: true,
  imports: [CommonModule, ActionButtonComponent, TourLogsComponent],
  templateUrl: 'tour-detail.component.html',
  styles: [`
    h2, h3, p { margin: 0 0 12px 0; }
    .details-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px; }
    .details-card { border: 1px solid #e5e7eb; border-radius: 14px; padding: 16px; background: #ffffff; }
    .detail-image { width: 100%; max-height: 240px; object-fit: cover; border-radius: 12px; margin-bottom: 12px; }
    .detail-list { padding-left: 18px; margin: 0; line-height: 1.6; }
    .map-placeholder { height: 280px; border: 2px dashed #94a3b8; border-radius: 12px; display: flex; align-items: center; justify-content: center; background: #f8fafc; color: #475569; }
    @media (max-width: 900px) { .details-grid { grid-template-columns: 1fr; } }
  `]
})
export class TourDetailComponent {
  @Input({ required: true }) tour!: Tour;

  @Output() editTour = new EventEmitter<void>();
  @Output() deleteTour = new EventEmitter<void>();
  @Output() tourUpdated = new EventEmitter<Tour>();

  updateLogs(updatedLogs: TourLog[]) {
    this.tourUpdated.emit({ ...this.tour, logs: updatedLogs });
  }
}
