import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Tour } from './shared/models/tour.model';
import { TourListComponent } from './tours/tours.component';
import { TourFormComponent } from './tour-form/tour-form.component';
import { TourDetailComponent } from './tour-detail/tour-detail.component';
import {ActionButtonComponent} from './shared/action-button/action-button.component';


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, TourListComponent, TourFormComponent, TourDetailComponent, ActionButtonComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  tours: Tour[] = [
    {
      id: 1,
      name: 'Vienna Woods Hike',
      description: 'A relaxing hiking tour through the Vienna Woods.',
      from: 'Vienna',
      to: 'Klosterneuburg',
      transportType: 'Hiking',
      distance: 12,
      estimatedTime: 4,
      image: 'https://vcdn.bergfex.at/images/resized/10/85bcadb24995b410_711033be33617f95.jpg',
      logs: [{ id: 1, dateTime: '2026-03-20T09:30', comment: 'Very nice weather and easy trail.', difficulty: 2, totalDistance: 12, totalTime: 4, rating: 5 }]
    }
  ];

  selectedTour: Tour | null = this.tours[0];
  showTourForm = signal(false);
  editMode = signal(false);

  selectTour(tour: Tour) {
    this.selectedTour = tour;
    this.showTourForm.set(false);
  }

  openCreateTourForm() {
    this.editMode.set(false);
    this.selectedTour = null;
    this.showTourForm.set(true);
  }

  openEditTourForm() {
    this.editMode.set(true);
    this.showTourForm.set(true);
  }

  saveTour(tour: Tour) {
    if (this.editMode()) {
      const index = this.tours.findIndex(t => t.id === tour.id);
      if (index !== -1) this.tours[index] = tour;
    } else {
      this.tours.push(tour);
    }
    this.updateTourState(tour);
    this.showTourForm.set(false);
  }

  deleteTour() {
    if (!this.selectedTour) return;
    this.tours = this.tours.filter(t => t.id !== this.selectedTour!.id);
    this.selectedTour = this.tours.length > 0 ? this.tours[0] : null;
    this.showTourForm.set(false);
  }

  updateTourState(updatedTour: Tour) {
    this.selectedTour = updatedTour;
    const index = this.tours.findIndex(t => t.id === updatedTour.id);
    if (index !== -1) this.tours[index] = updatedTour;
  }

  cancelTourForm() {
    this.showTourForm.set(false);
    if (!this.selectedTour && this.tours.length > 0) {
      this.selectedTour = this.tours[0];
    }
  }

  onLogin() {
    console.log('Navigate to Login');
    // Implement login logic or routing here
  }

  onRegister() {
    console.log('Navigate to Register');
    // Implement registration logic or routing here
  }
}
