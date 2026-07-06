import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { FormControl } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import { DataApiService } from '../../core/api/data-api.service';
import { extractMessage } from '../../core/api/http-error';
import { LocationSuggestion } from '../../core/models/tour.model';

export type LocationControlName = 'fromLocation' | 'toLocation';

@Injectable()
export class TourFormViewModel {
  private dataApi = inject(DataApiService);
  private destroyRef = inject(DestroyRef);

  readonly locationLookupError = signal<string | null>(null);
  readonly fromSuggestions = signal<LocationSuggestion[]>([]);
  readonly toSuggestions = signal<LocationSuggestion[]>([]);
  readonly imagePath = signal<string | null>(null);
  readonly uploadingImage = signal(false);
  readonly uploadError = signal<string | null>(null);
  readonly hasLocationLookupError = computed(() => this.locationLookupError() !== null);
  readonly hasFromSuggestions = computed(() => this.fromSuggestions().length > 0);
  readonly hasToSuggestions = computed(() => this.toSuggestions().length > 0);
  readonly hasUploadError = computed(() => this.uploadError() !== null);
  readonly hasImage = computed(() => this.imagePath() !== null);
  readonly displayImageUrl = computed(() => {
    const path = this.imagePath();
    return path ? this.dataApi.imageUrl(path) : null;
  });

  bindLocationAutocomplete(controlName: LocationControlName, control: FormControl<string>): void {
    control.valueChanges
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
      .subscribe((suggestions) => this.suggestionsFor(controlName).set(suggestions));
  }

  applySuggestion(
    controlName: LocationControlName,
    control: FormControl<string>,
    suggestion: LocationSuggestion,
  ): void {
    control.setValue(suggestion.label, { emitEvent: false });
    this.suggestionsFor(controlName).set([]);
  }

  clearSuggestions(controlName: LocationControlName): void {
    window.setTimeout(() => this.suggestionsFor(controlName).set([]), 150);
  }

  resetAutocomplete(): void {
    this.fromSuggestions.set([]);
    this.toSuggestions.set([]);
    this.locationLookupError.set(null);
  }

  setImagePath(path: string | null): void {
    this.imagePath.set(path);
    this.uploadError.set(null);
  }

  uploadImage(file: File): void {
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

  private suggestionsFor(controlName: LocationControlName) {
    return controlName === 'fromLocation' ? this.fromSuggestions : this.toSuggestions;
  }
}
