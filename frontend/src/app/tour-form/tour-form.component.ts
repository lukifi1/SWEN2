import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Tour } from '../shared/models/tour.model';
import { ActionButtonComponent } from '../shared/action-button/action-button.component';

@Component({
  selector: 'app-tour-form',
  standalone: true,
  imports: [CommonModule, FormsModule, ActionButtonComponent],
  templateUrl: './tour-form.component.html',
})
export class TourFormComponent implements OnChanges {
  @Input() tour: Tour | null = null;
  @Input() editMode = false;

  @Output() save = new EventEmitter<Tour>();
  @Output() cancel = new EventEmitter<void>();

  tourForm: Tour = this.createEmptyTour();

  ngOnChanges() {
    this.tourForm = this.tour && this.editMode ? { ...this.tour, logs: [...this.tour.logs] } : this.createEmptyTour();
  }

  submitForm() {
    this.save.emit({ ...this.tourForm, id: this.editMode ? this.tourForm.id : Date.now() });
  }

  createEmptyTour(): Tour {
    return { id: 0, name: '', description: '', from: '', to: '', transportType: '', distance: 0, estimatedTime: 0, image: '', logs: [], longitude: 0, latitude: 0};
  }
}
