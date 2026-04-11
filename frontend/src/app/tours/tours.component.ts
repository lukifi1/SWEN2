import { Component, EventEmitter, Input, Output } from '@angular/core';

import { Tour } from '../shared/models/tour.model';
import { ActionButtonComponent } from '../shared/action-button/action-button.component';

@Component({
  selector: 'app-tour-list',
  standalone: true,
  imports: [ActionButtonComponent],
  templateUrl: './tours.component.html',
  styles: [`
    h2, h3, p { margin: 0; }
    h3 { margin-bottom: 4px; }
    p { color: #4b5563; font-size: 14px; margin-bottom: 4px; }
    small { color: #6b7280; font-size: 12px; }
    .tour-list { display: flex; flex-direction: column; gap: 12px; }
    .tour-card { display: flex; gap: 12px; padding: 12px; border: 1px solid #dbe3ea; border-radius: 12px; cursor: pointer; transition: 0.2s ease; }
    .tour-card:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06); }
    .tour-card.active { border: 2px solid #2563eb; }
    .tour-thumb { width: 80px; height: 80px; object-fit: cover; border-radius: 10px; }
  `]
})
export class TourListComponent {
  @Input() tours: Tour[] = [];
  @Input() selectedTourId?: number;
  @Output() tourSelected = new EventEmitter<Tour>();
  @Output() createTourClicked = new EventEmitter<void>();
}
