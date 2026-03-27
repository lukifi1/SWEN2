import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Tour } from '../models/tour';
import {ActionButtonComponent} from '../shared/action-button/action-button.component';

@Component({
  selector: 'app-tours',
  standalone: true,
  imports: [
    ActionButtonComponent
  ],
  templateUrl: './tours.component.html'
})
export class ToursComponent {
  @Input() tours: Tour[] = [];
  @Input() selectedTour: Tour | null = null;

  @Output() select = new EventEmitter<Tour>();
  @Output() create = new EventEmitter<Tour>();
}
