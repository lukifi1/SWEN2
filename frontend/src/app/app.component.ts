import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {TourFormComponent} from './tour-form/tour-form.component';
import {TourDetailComponent} from './tour-detail/tour-detail.component';
import {ToursComponent} from './tours/tours.component';
import {Tour, TourLog} from './models/tour';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, TourFormComponent, TourDetailComponent, ToursComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
class AppComponent {
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
      image: 'https://via.placeholder.com/400x220?text=Tour+Image',
      logs: [
        {
          id: 1,
          dateTime: '2026-03-20T09:30',
          comment: 'Very nice weather and easy trail.',
          difficulty: 2,
          totalDistance: 12,
          totalTime: 4,
          rating: 5
        }
      ]
    }
  ];

  selectedTour: Tour | null = this.tours[0];

  showTourForm = false;
  editMode = false;

  tourForm: Tour = this.createEmptyTour();

  logForm: TourLog = this.createEmptyLog();
  editingLogId: number | null = null;

  createEmptyTour(): Tour {
    return {
      id: 0,
      name: '',
      description: '',
      from: '',
      to: '',
      transportType: '',
      distance: 0,
      estimatedTime: 0,
      image: '',
      logs: []
    };
  }

  createEmptyLog(): TourLog {
    return {
      id: 0,
      dateTime: '',
      comment: '',
      difficulty: 1,
      totalDistance: 0,
      totalTime: 0,
      rating: 1
    };
  }

  selectTour(tour: Tour): void {
    this.selectedTour = tour;
    this.resetLogForm();
  }

  openCreateTourForm(): void {
    this.editMode = false;
    this.tourForm = this.createEmptyTour();
    this.showTourForm = true;
  }

  openEditTourForm(): void {
    if (!this.selectedTour) return;

    this.editMode = true;
    this.tourForm = {
      ...this.selectedTour,
      logs: [...this.selectedTour.logs]
    };
    this.showTourForm = true;
  }

  saveTour(tour: Tour): void {
    this.tourForm = tour;
    if (
      !this.tourForm.name.trim() ||
      !this.tourForm.description.trim() ||
      !this.tourForm.from.trim() ||
      !this.tourForm.to.trim() ||
      !this.tourForm.transportType.trim() ||
      this.tourForm.distance <= 0 ||
      this.tourForm.estimatedTime <= 0 ||
      !this.tourForm.image.trim()
    ) {
      return;
    }

    if (this.editMode) {
      const index = this.tours.findIndex(t => t.id === this.tourForm.id);
      if (index !== -1) {
        this.tours[index] = {
          ...this.tourForm,
          logs: this.tours[index].logs
        };
        this.selectedTour = this.tours[index];
      }
    } else {
      const newTour: Tour = {
        ...this.tourForm,
        id: Date.now(),
        logs: []
      };
      this.tours.push(newTour);
      this.selectedTour = newTour;
    }

    this.cancelTourForm();
  }

  deleteTour(): void {
    if (!this.selectedTour) return;

    const id = this.selectedTour.id;
    this.tours = this.tours.filter(t => t.id !== id);
    this.selectedTour = this.tours.length > 0 ? this.tours[0] : null;
    this.showTourForm = false;
    this.resetLogForm();
  }

  cancelTourForm(): void {
    this.showTourForm = false;
    this.tourForm = this.createEmptyTour();
  }

  saveLog(): void {
    if (!this.selectedTour) return;

    if (
      !this.logForm.dateTime ||
      !this.logForm.comment.trim() ||
      this.logForm.difficulty < 1 ||
      this.logForm.difficulty > 5 ||
      this.logForm.totalDistance <= 0 ||
      this.logForm.totalTime <= 0 ||
      this.logForm.rating < 1 ||
      this.logForm.rating > 5
    ) {
      return;
    }

    if (this.editingLogId) {
      const index = this.selectedTour.logs.findIndex(log => log.id === this.editingLogId);
      if (index !== -1) {
        this.selectedTour.logs[index] = {
          ...this.logForm,
          id: this.editingLogId
        };
      }
    } else {
      this.selectedTour.logs.push({
        ...this.logForm,
        id: Date.now()
      });
    }

    this.resetLogForm();
  }

  editLog(log: TourLog): void {
    this.logForm = { ...log };
    this.editingLogId = log.id;
  }

  deleteLog(logId: number): void {
    if (!this.selectedTour) return;

    this.selectedTour.logs = this.selectedTour.logs.filter(log => log.id !== logId);

    if (this.editingLogId === logId) {
      this.resetLogForm();
    }
  }

  resetLogForm(): void {
    this.logForm = this.createEmptyLog();
    this.editingLogId = null;
  }
}

export default AppComponent
