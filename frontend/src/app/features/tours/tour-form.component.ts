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
import { TourFormViewModel } from './tour-form.viewmodel';

@Component({
  selector: 'app-tour-form',
  standalone: true,
  providers: [TourFormViewModel],
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
  protected readonly vm = inject(TourFormViewModel);

  readonly transportTypes = ['Hiking', 'Running', 'Bike', 'Car'];
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

  constructor() {
    this.vm.bindLocationAutocomplete('fromLocation', this.form.controls.fromLocation);
    this.vm.bindLocationAutocomplete('toLocation', this.form.controls.toLocation);
  }

  ngOnChanges(): void {
    if (this.tour) {
      this.form.patchValue({
        name: this.tour.name,
        description: this.tour.description ?? '',
        fromLocation: this.tour.fromLocation,
        toLocation: this.tour.toLocation,
        transportType: this.tour.transportType,
      }, { emitEvent: false });
      this.imagePath.set(this.tour.imagePath);
    } else {
      this.form.reset(
        { transportType: 'Hiking', name: '', description: '', fromLocation: '', toLocation: '' },
        { emitEvent: false },
      );
      this.imagePath.set(null);
    }
    this.uploadError.set(null);
    this.vm.resetAutocomplete();
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
