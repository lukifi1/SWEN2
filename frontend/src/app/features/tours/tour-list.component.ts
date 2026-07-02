import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { Tour } from '../../core/models/tour.model';
import { DataApiService } from '../../core/api/data-api.service';
import { childFriendlinessLabel, formatDistance } from '../../core/tour-format';

@Component({
  selector: 'app-tour-list',
  standalone: true,
  templateUrl: './tour-list.component.html',
  styleUrl: './tour-list.component.css',
})
export class TourListComponent {
  @Input() tours: Tour[] = [];
  @Input() selectedId: number | null = null;
  @Input() loading = false;
  @Output() tourSelected = new EventEmitter<Tour>();

  private dataApi = inject(DataApiService);

  protected readonly formatDistance = formatDistance;
  protected readonly childFriendlinessLabel = childFriendlinessLabel;

  imageUrl(tour: Tour): string | null {
    return tour.imagePath ? this.dataApi.imageUrl(tour.imagePath) : null;
  }
}
