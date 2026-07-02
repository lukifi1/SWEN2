import { DatePipe } from '@angular/common';
import { Component, Input, OnChanges, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TourLog } from '../../core/models/tour.model';
import { TourLogsViewModel } from './tour-logs.viewmodel';
import { ActionButtonComponent } from '../../shared/action-button/action-button.component';

@Component({
  selector: 'app-tour-logs',
  standalone: true,
  imports: [ReactiveFormsModule, ActionButtonComponent, DatePipe],
  templateUrl: './tour-logs.component.html',
  styleUrl: './tour-logs.component.css',
})
export class TourLogsComponent implements OnChanges {
  @Input({ required: true }) tourId!: number;

  protected readonly vm = inject(TourLogsViewModel);
  private fb = inject(FormBuilder);

  readonly editingId = signal<number | null>(null);

  readonly form = this.fb.nonNullable.group({
    dateTime: ['', Validators.required],
    comment: ['', Validators.maxLength(3000)],
    difficulty: [3, [Validators.required, Validators.min(1), Validators.max(5)]],
    totalDistance: [0, [Validators.required, Validators.min(0)]],
    totalTime: [0, [Validators.required, Validators.min(0)]],
    rating: [3, [Validators.required, Validators.min(1), Validators.max(5)]],
  });

  ngOnChanges(): void {
    this.vm.load(this.tourId);
    this.resetForm();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const dto = this.form.getRawValue();
    const id = this.editingId();
    if (id !== null) {
      this.vm.update(id, dto);
    } else {
      this.vm.create(dto);
    }
    this.resetForm();
  }

  edit(log: TourLog): void {
    this.editingId.set(log.id);
    this.form.setValue({
      dateTime: (log.dateTime ?? '').slice(0, 16),
      comment: log.comment ?? '',
      difficulty: log.difficulty,
      totalDistance: log.totalDistance,
      totalTime: log.totalTime,
      rating: log.rating,
    });
  }

  remove(id: number): void {
    this.vm.remove(id);
    if (this.editingId() === id) {
      this.resetForm();
    }
  }

  resetForm(): void {
    this.editingId.set(null);
    this.form.reset({
      dateTime: '',
      comment: '',
      difficulty: 3,
      totalDistance: 0,
      totalTime: 0,
      rating: 3,
    });
  }

  stars(value: number): string {
    return '★'.repeat(value) + '☆'.repeat(Math.max(0, 5 - value));
  }
}
