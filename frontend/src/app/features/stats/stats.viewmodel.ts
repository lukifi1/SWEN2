import { Injectable, inject, signal } from '@angular/core';
import { StatsApiService } from '../../core/api/stats-api.service';
import { extractMessage } from '../../core/api/http-error';
import { Stats } from '../../core/models/stats.model';

@Injectable()
export class StatsViewModel {
  private api = inject(StatsApiService);

  readonly stats = signal<Stats | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.get().subscribe({
      next: (stats) => {
        this.stats.set(stats);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(extractMessage(err, 'Failed to load statistics.'));
        this.loading.set(false);
      },
    });
  }
}
