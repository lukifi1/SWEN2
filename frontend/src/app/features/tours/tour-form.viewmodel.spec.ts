import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { FormControl } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { DataApiService } from '../../core/api/data-api.service';
import { LocationSuggestion } from '../../core/models/tour.model';
import { TourFormViewModel } from './tour-form.viewmodel';

describe('TourFormViewModel', () => {
  let dataApi: jasmine.SpyObj<DataApiService>;
  let vm: TourFormViewModel;

  const vienna: LocationSuggestion = {
    label: 'Vienna, Austria',
    latitude: 48.2082,
    longitude: 16.3738,
  };

  beforeEach(() => {
    dataApi = jasmine.createSpyObj<DataApiService>('DataApiService', [
      'locationSuggestions',
      'uploadImage',
      'imageUrl',
    ]);

    TestBed.configureTestingModule({
      providers: [
        TourFormViewModel,
        { provide: DataApiService, useValue: dataApi },
      ],
    });

    vm = TestBed.inject(TourFormViewModel);
  });

  it('loads debounced from-location suggestions', fakeAsync(() => {
    const control = new FormControl('', { nonNullable: true });
    dataApi.locationSuggestions.and.returnValue(of([vienna]));

    vm.bindLocationAutocomplete('fromLocation', control);
    control.setValue('Vie');
    tick(300);

    expect(dataApi.locationSuggestions).toHaveBeenCalledWith('Vie');
    expect(vm.fromSuggestions()).toEqual([vienna]);
    expect(vm.hasFromSuggestions()).toBeTrue();
    expect(vm.locationLookupError()).toBeNull();
    expect(vm.hasLocationLookupError()).toBeFalse();
  }));

  it('clears suggestions and exposes lookup errors', fakeAsync(() => {
    const control = new FormControl('', { nonNullable: true });
    dataApi.locationSuggestions.and.returnValue(throwError(() => new Error('failed')));

    vm.bindLocationAutocomplete('toLocation', control);
    control.setValue('Gra');
    tick(300);

    expect(vm.toSuggestions()).toEqual([]);
    expect(vm.locationLookupError()).toBe('Location lookup failed.');
    expect(vm.hasLocationLookupError()).toBeTrue();
  }));

  it('applies a selected suggestion to the target control', () => {
    const control = new FormControl('', { nonNullable: true });
    vm.toSuggestions.set([vienna]);

    vm.applySuggestion('toLocation', control, vienna);

    expect(control.value).toBe('Vienna, Austria');
    expect(vm.toSuggestions()).toEqual([]);
  });

  it('uploads an image and stores the returned filename', () => {
    const file = new File(['image'], 'tour.png', { type: 'image/png' });
    dataApi.uploadImage.and.returnValue(of({ filename: 'tour-1.png' }));

    vm.uploadImage(file);

    expect(dataApi.uploadImage).toHaveBeenCalledWith(file);
    expect(vm.imagePath()).toBe('tour-1.png');
    expect(vm.hasImage()).toBeTrue();
    expect(vm.uploadingImage()).toBeFalse();
    expect(vm.uploadError()).toBeNull();
    expect(vm.hasUploadError()).toBeFalse();
  });

  it('exposes upload errors and clears the loading state', () => {
    const file = new File(['image'], 'tour.png', { type: 'image/png' });
    dataApi.uploadImage.and.returnValue(throwError(() => new Error('failed')));

    vm.uploadImage(file);

    expect(vm.uploadError()).toBe('Image upload failed.');
    expect(vm.hasUploadError()).toBeTrue();
    expect(vm.uploadingImage()).toBeFalse();
  });

  it('builds an image preview URL from the stored image path', () => {
    dataApi.imageUrl.and.returnValue('/api/images/tour-1.png');

    vm.setImagePath('tour-1.png');

    expect(vm.displayImageUrl()).toBe('/api/images/tour-1.png');
    expect(dataApi.imageUrl).toHaveBeenCalledWith('tour-1.png');
  });
});
