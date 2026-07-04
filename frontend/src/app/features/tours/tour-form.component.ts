import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  WritableSignal,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { LocationSuggestion, Tour, TourCreate } from '../../core/models/tour.model';
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
  private destroyRef = inject(DestroyRef);

  readonly transportTypes = ['Hiking', 'Running', 'Bike', 'Car', 'Vacation'];
  readonly imagePath = signal<string | null>(null);
  readonly uploadingImage = signal(false);
  readonly uploadError = signal<string | null>(null);
  readonly locationLookupError = signal<string | null>(null);
  readonly fromSuggestions = signal<LocationSuggestion[]>([]);
  readonly toSuggestions = signal<LocationSuggestion[]>([]);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', Validators.maxLength(3000)],
    fromLocation: ['', Validators.required],
    toLocation: ['', Validators.required],
    transportType: ['Hiking', Validators.required],
  });

  constructor() {
    this.bindLocationAutocomplete('fromLocation', this.fromSuggestions);
    this.bindLocationAutocomplete('toLocation', this.toSuggestions);
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
    this.fromSuggestions.set([]);
    this.toSuggestions.set([]);
    this.uploadError.set(null);
    this.locationLookupError.set(null);
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

  applySuggestion(controlName: 'fromLocation' | 'toLocation', suggestion: LocationSuggestion): void {
    this.form.controls[controlName].setValue(suggestion.label, { emitEvent: false });
    this.suggestionsFor(controlName).set([]);
  }

  clearSuggestions(controlName: 'fromLocation' | 'toLocation'): void {
    window.setTimeout(() => this.suggestionsFor(controlName).set([]), 150);
  }

  private bindLocationAutocomplete(
    controlName: 'fromLocation' | 'toLocation',
    target: WritableSignal<LocationSuggestion[]>,
  ): void {
    this.form.controls[controlName].valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((value) => {
          const query = value.trim();
          this.locationLookupError.set(null);
          if (query.length < 3) {
            return of([]);
          }
          return this.dataApi.locationSuggestions(query).pipe(
            catchError((err) => {
              this.locationLookupError.set(extractMessage(err, 'Location lookup failed.'));
              return of([]);
            }),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((suggestions) => target.set(suggestions));
  }

  private suggestionsFor(controlName: 'fromLocation' | 'toLocation'): WritableSignal<LocationSuggestion[]> {
    return controlName === 'fromLocation' ? this.fromSuggestions : this.toSuggestions;
  }
}
