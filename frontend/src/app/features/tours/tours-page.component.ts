import { Component, OnInit, inject, signal } from '@angular/core';
import { ToursViewModel } from './tours.viewmodel';
import { TourLogsViewModel } from './tour-logs.viewmodel';
import { DataApiService } from '../../core/api/data-api.service';
import { extractMessage } from '../../core/api/http-error';
import { TourListComponent } from './tour-list.component';
import { TourFormComponent } from './tour-form.component';
import { TourDetailComponent } from './tour-detail.component';
import { ActionButtonComponent } from '../../shared/action-button/action-button.component';

@Component({
  selector: 'app-tours-page',
  standalone: true,
  providers: [ToursViewModel, TourLogsViewModel],
  imports: [TourListComponent, TourFormComponent, TourDetailComponent, ActionButtonComponent],
  templateUrl: './tours-page.component.html',
  styleUrl: './tours-page.component.css',
})
export class ToursPageComponent implements OnInit {
  protected readonly vm = inject(ToursViewModel);
  private readonly dataApi = inject(DataApiService);

  readonly importMessage = signal<string | null>(null);
  private searchTimer: ReturnType<typeof setTimeout> | undefined;

  ngOnInit(): void {
    this.vm.load();
  }

  onSearch(event: Event): void {
    const term = (event.target as HTMLInputElement).value;
    clearTimeout(this.searchTimer);
    this.searchTimer = setTimeout(() => this.vm.search(term), 300);
  }

  onExport(): void {
    const selected = this.vm.selectedTour();
    if (!selected) {
      this.vm.error.set('Select a tour before exporting.');
      return;
    }

    this.dataApi.exportTour(selected.id).subscribe({
      next: (blob) => this.downloadBlob(blob, this.exportFilename(selected.name)),
      error: (err) => this.vm.error.set(extractMessage(err, 'Export failed.')),
    });
  }

  onImport(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    this.importMessage.set(null);
    this.dataApi.importTours(file).subscribe({
      next: (res) => {
        this.importMessage.set(`Imported ${res.imported} tour(s).`);
        this.vm.load();
      },
      error: (err) => this.vm.error.set(extractMessage(err, 'Import failed.')),
    });
    input.value = '';
  }

  private downloadBlob(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = filename;
    anchor.click();
    URL.revokeObjectURL(url);
  }

  private exportFilename(name: string): string {
    const safeName = name.trim().toLowerCase()
      .replace(/[^a-z0-9-]+/g, '-')
      .replace(/^-+|-+$/g, '');
    return `${safeName || 'tour'}.gpx`;
  }
}
