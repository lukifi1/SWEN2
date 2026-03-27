import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Tour } from '../models/tour';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-tour-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tour-form.component.html'
})
export class TourFormComponent {
  @Input() tour!: Tour;
  @Input() editMode = false;

  @Output() save = new EventEmitter<Tour>();
  @Output() cancel = new EventEmitter<void>();
}
