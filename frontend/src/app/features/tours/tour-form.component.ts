import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  inject,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Tour, TourCreate } from '../../core/models/tour.model';
import { DataApiService } from '../../core/api/data-api.service';
import { extractMessage } from '../../core/api/http-error';
import { ActionButtonComponent } from '../../shared/action-button/action-button.component';

@Component({
  selector: 'app-tour-form',
  standalone: true,
  imports: [ReactiveFormsModule, ActionButtonComponent],
  templateUrl: './tour-form.component.html',
  styleUrl: './tour-form.component.css',
})
export class TourFormComponent implements OnChanges {
  @Input() tour: Tour | null = null;
  @Input() saving = false;
  @Output() save = new EventEmitter<TourCreate>();
  @Output() cancel = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private dataApi = inject(DataApiService);

  readonly transportTypes = ['Hiking', 'Running', 'Bike', 'Car', 'Vacation'];
  readonly imagePath = signal<string | null>(null);
  readonly uploadingImage = signal(false);
  readonly uploadError = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', Validators.maxLength(3000)],
    fromLocation: ['', Validators.required],
    toLocation: ['', Validators.required],
    transportType: ['Hiking', Validators.required],
  });

  ngOnChanges(): void {
    if (this.tour) {
      this.form.patchValue({
        name: this.tour.name,
        description: this.tour.description ?? '',
        fromLocation: this.tour.fromLocation,
        toLocation: this.tour.toLocation,
        transportType: this.tour.transportType,
      });
      this.imagePath.set(this.tour.imagePath);
    } else {
      this.form.reset({ transportType: 'Hiking', name: '', description: '', fromLocation: '', toLocation: '' });
      this.imagePath.set(null);
    }
    this.uploadError.set(null);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    this.uploadingImage.set(true);
    this.uploadError.set(null);
    this.dataApi.uploadImage(file).subscribe({
      next: (res) => {
        this.imagePath.set(res.filename);
        this.uploadingImage.set(false);
      },
      error: (err) => {
        this.uploadError.set(extractMessage(err, 'Image upload failed.'));
        this.uploadingImage.set(false);
      },
    });
  }

  imageUrl(): string | null {
    const path = this.imagePath();
    return path ? this.dataApi.imageUrl(path) : null;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.save.emit({ ...this.form.getRawValue(), imagePath: this.imagePath() });
  }

  get isEdit(): boolean {
    return this.tour !== null;
  }
}
