import { Injectable, computed, inject, signal } from '@angular/core';
import { TourApiService } from '../../core/api/tour-api.service';
import { DataApiService } from '../../core/api/data-api.service';
import { extractMessage } from '../../core/api/http-error';
import { Tour, TourCreate } from '../../core/models/tour.model';

export type TourViewMode = 'view' | 'create' | 'edit';
export type TourExportHandler = (blob: Blob, filename: string) => void;

/**
 * View-model for the tours page. Holds all tour-related UI state as signals and
 * exposes commands that talk to the API service. The views only bind to this.
 */
@Injectable()
export class ToursViewModel {
  private api = inject(TourApiService);
  private dataApi = inject(DataApiService);

  readonly tours = signal<Tour[]>([]);
  readonly selectedTour = signal<Tour | null>(null);
  readonly searchTerm = signal('');
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly importMessage = signal<string | null>(null);
  readonly mode = signal<TourViewMode>('view');

  readonly hasTours = computed(() => this.tours().length > 0);
  readonly hasSelectedTour = computed(() => this.selectedTour() !== null);

  load(): void {
    this.runList(this.api.list(), 'Failed to load tours.');
  }

  search(term: string): void {
    this.searchTerm.set(term);
    this.runList(this.api.search(term), 'Search failed.');
  }

  select(tour: Tour): void {
    this.selectedTour.set(tour);
    this.mode.set('view');
  }

  startCreate(): void {
    this.mode.set('create');
  }

  startEdit(): void {
    if (this.selectedTour()) {
      this.mode.set('edit');
    }
  }

  cancelForm(): void {
    this.mode.set('view');
    if (!this.selectedTour() && this.tours().length > 0) {
      this.selectedTour.set(this.tours()[0]);
    }
  }

  save(dto: TourCreate): void {
    const editing = this.mode() === 'edit' && this.selectedTour();
    const request = editing
      ? this.api.update(this.selectedTour()!.id, dto)
      : this.api.create(dto);

    this.loading.set(true);
    this.error.set(null);
    request.subscribe({
      next: (tour) => {
        this.upsert(tour);
        this.selectedTour.set(tour);
        this.mode.set('view');
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(extractMessage(err, 'Failed to save tour.'));
        this.loading.set(false);
      },
    });
  }

  delete(): void {
    const selected = this.selectedTour();
    if (!selected) {
      return;
    }
    this.api.remove(selected.id).subscribe({
      next: () => {
        this.tours.update((list) => list.filter((t) => t.id !== selected.id));
        this.selectedTour.set(this.tours()[0] ?? null);
        this.mode.set('view');
      },
      error: (err) => this.error.set(extractMessage(err, 'Failed to delete tour.')),
    });
  }

  exportSelected(onExportReady: TourExportHandler): void {
    const selected = this.selectedTour();
    if (!selected) {
      this.error.set('Select a tour before exporting.');
      return;
    }

    this.error.set(null);
    this.dataApi.exportTour(selected.id).subscribe({
      next: (blob) => onExportReady(blob, this.exportFilename(selected.name)),
      error: (err) => this.error.set(extractMessage(err, 'Export failed.')),
    });
  }

  importTours(file: File): void {
    this.importMessage.set(null);
    this.error.set(null);
    this.dataApi.importTours(file).subscribe({
      next: (res) => {
        this.importMessage.set(`Imported ${res.imported} tour(s).`);
        this.load();
      },
      error: (err) => this.error.set(extractMessage(err, 'Import failed.')),
    });
  }

  /** Reloads the selected tour so recomputed attributes (popularity, ...) show up. */
  refreshSelected(): void {
    const selected = this.selectedTour();
    if (!selected) {
      return;
    }
    this.api.get(selected.id).subscribe({
      next: (tour) => {
        this.upsert(tour);
        this.selectedTour.set(tour);
      },
    });
  }

  clearError(): void {
    this.error.set(null);
  }

  private runList(request: ReturnType<TourApiService['list']>, fallback: string): void {
    this.loading.set(true);
    this.error.set(null);
    request.subscribe({
      next: (tours) => {
        this.tours.set(tours);
        this.syncSelection();
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(extractMessage(err, fallback));
        this.loading.set(false);
      },
    });
  }

  private upsert(tour: Tour): void {
    this.tours.update((list) => {
      const index = list.findIndex((t) => t.id === tour.id);
      if (index === -1) {
        return [tour, ...list];
      }
      const copy = [...list];
      copy[index] = tour;
      return copy;
    });
  }

  private exportFilename(name: string): string {
    const safeName = name.trim().toLowerCase()
      .replace(/[^a-z0-9-]+/g, '-')
      .replace(/^-+|-+$/g, '');
    return `${safeName || 'tour'}.gpx`;
  }

  private syncSelection(): void {
    const current = this.selectedTour();
    const list = this.tours();
    if (current) {
      const match = list.find((t) => t.id === current.id);
      this.selectedTour.set(match ?? list[0] ?? null);
    } else {
      this.selectedTour.set(list[0] ?? null);
    }
  }
}
