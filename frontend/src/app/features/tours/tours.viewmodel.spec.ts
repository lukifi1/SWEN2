import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { TourApiService } from '../../core/api/tour-api.service';
import { Tour, TourCreate } from '../../core/models/tour.model';
import { ToursViewModel } from './tours.viewmodel';

describe('ToursViewModel', () => {
  let api: jasmine.SpyObj<TourApiService>;
  let vm: ToursViewModel;

  const firstTour: Tour = {
    id: 1,
    name: 'First',
    description: 'First tour',
    fromLocation: 'Vienna',
    toLocation: 'Graz',
    transportType: 'bike',
    distance: 120,
    estimatedTime: 5,
    routeGeometry: null,
    imagePath: null,
    popularity: 0,
    childFriendliness: 0,
  };

  const secondTour: Tour = { ...firstTour, id: 2, name: 'Second' };

  beforeEach(() => {
    api = jasmine.createSpyObj<TourApiService>('TourApiService', [
      'list',
      'search',
      'get',
      'create',
      'update',
      'remove',
    ]);

    TestBed.configureTestingModule({
      providers: [ToursViewModel, { provide: TourApiService, useValue: api }],
    });

    vm = TestBed.inject(ToursViewModel);
  });

  it('loads tours and selects the first result', () => {
    api.list.and.returnValue(of([firstTour, secondTour]));

    vm.load();

    expect(vm.tours()).toEqual([firstTour, secondTour]);
    expect(vm.selectedTour()).toEqual(firstTour);
    expect(vm.hasTours()).toBeTrue();
    expect(vm.loading()).toBeFalse();
  });

  it('creates a tour and switches back to view mode', () => {
    const dto: TourCreate = {
      name: 'Second',
      description: 'Created tour',
      fromLocation: 'Vienna',
      toLocation: 'Graz',
      transportType: 'bike',
      imagePath: null,
    };
    api.create.and.returnValue(of(secondTour));

    vm.startCreate();
    vm.save(dto);

    expect(api.create).toHaveBeenCalledWith(dto);
    expect(vm.selectedTour()).toEqual(secondTour);
    expect(vm.tours()).toEqual([secondTour]);
    expect(vm.mode()).toBe('view');
  });
});
