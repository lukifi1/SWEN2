import { Injectable, computed, inject, signal } from '@angular/core';
import { TourLogApiService } from '../../core/api/tour-log-api.service';
import { extractMessage } from '../../core/api/http-error';
import { TourLog, TourLogCreate } from '../../core/models/tour.model';
import { ToursViewModel } from './tours.viewmodel';

/**
 * View-model for the tour-logs panel. After every mutation it reloads the logs
 * and asks the tours view-model to refresh the selected tour, so the computed
 * popularity / child-friendliness stay in sync.
 */
@Injectable()
export class TourLogsViewModel {
  private api = inject(TourLogApiService);
  private toursVm = inject(ToursViewModel);

  readonly logs = signal<TourLog[]>([]);
  readonly error = signal<string | null>(null);
  readonly hasLogs = computed(() => this.logs().length > 0);
  readonly hasError = computed(() => this.error() !== null);
  private tourId = 0;

  load(tourId: number): void {
    this.tourId = tourId;
    this.error.set(null);
    this.api.list(tourId).subscribe({
      next: (logs) => this.logs.set(logs),
      error: (err) => this.error.set(extractMessage(err, 'Failed to load logs.')),
    });
  }

  create(dto: TourLogCreate): void {
    this.api.create(this.tourId, dto).subscribe({
      next: () => this.afterChange(),
      error: (err) => this.error.set(extractMessage(err, 'Failed to add log.')),
    });
  }

  update(logId: number, dto: TourLogCreate): void {
    this.api.update(this.tourId, logId, dto).subscribe({
      next: () => this.afterChange(),
      error: (err) => this.error.set(extractMessage(err, 'Failed to update log.')),
    });
  }

  remove(logId: number): void {
    this.api.remove(this.tourId, logId).subscribe({
      next: () => this.afterChange(),
      error: (err) => this.error.set(extractMessage(err, 'Failed to delete log.')),
    });
  }

  private afterChange(): void {
    this.load(this.tourId);
    this.toursVm.refreshSelected();
  }
}
