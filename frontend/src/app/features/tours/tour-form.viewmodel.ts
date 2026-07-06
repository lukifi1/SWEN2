import { DestroyRef, Injectable, inject, signal } from '@angular/core';
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

  private suggestionsFor(controlName: LocationControlName) {
    return controlName === 'fromLocation' ? this.fromSuggestions : this.toSuggestions;
  }
}
