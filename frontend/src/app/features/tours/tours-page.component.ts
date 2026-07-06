import { Component, OnInit, inject } from '@angular/core';
import { ToursViewModel } from './tours.viewmodel';
import { TourLogsViewModel } from './tour-logs.viewmodel';
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
    this.vm.exportSelected((blob, filename) => this.downloadBlob(blob, filename));
  }

  onImport(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    this.vm.importTours(file);
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
}
