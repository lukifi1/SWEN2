import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { DataApiService } from '../../core/api/data-api.service';
import { TourApiService } from '../../core/api/tour-api.service';
import { Tour, TourCreate } from '../../core/models/tour.model';
import { ToursViewModel } from './tours.viewmodel';

describe('ToursViewModel', () => {
  let api: jasmine.SpyObj<TourApiService>;
  let dataApi: jasmine.SpyObj<DataApiService>;
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
    dataApi = jasmine.createSpyObj<DataApiService>('DataApiService', [
      'exportTour',
      'importTours',
    ]);

    TestBed.configureTestingModule({
      providers: [
        ToursViewModel,
        { provide: TourApiService, useValue: api },
        { provide: DataApiService, useValue: dataApi },
      ],
    });

    vm = TestBed.inject(ToursViewModel);
  });

  it('loads tours and selects the first result', () => {
    api.list.and.returnValue(of([firstTour, secondTour]));

    vm.load();

    expect(vm.tours()).toEqual([firstTour, secondTour]);
    expect(vm.selectedTour()).toEqual(firstTour);
    expect(vm.hasTours()).toBeTrue();
    expect(vm.hasSelectedTour()).toBeTrue();
    expect(vm.selectedTourId()).toBe(firstTour.id);
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
    expect(vm.isCreating()).toBeTrue();
    expect(vm.isFormVisible()).toBeTrue();

    vm.save(dto);

    expect(api.create).toHaveBeenCalledWith(dto);
    expect(vm.selectedTour()).toEqual(secondTour);
    expect(vm.tours()).toEqual([secondTour]);
    expect(vm.mode()).toBe('view');
    expect(vm.isFormVisible()).toBeFalse();
  });

  it('exports the selected tour through the data API', () => {
    const blob = new Blob(['gpx']);
    const onExportReady = jasmine.createSpy('onExportReady');
    dataApi.exportTour.and.returnValue(of(blob));

    vm.select(firstTour);
    vm.exportSelected(onExportReady);

    expect(dataApi.exportTour).toHaveBeenCalledWith(firstTour.id);
    expect(onExportReady).toHaveBeenCalledWith(blob, 'first.gpx');
  });

  it('imports tours and reloads the list', () => {
    const file = new File(['gpx'], 'tour.gpx');
    dataApi.importTours.and.returnValue(of({ imported: 1 }));
    api.list.and.returnValue(of([firstTour]));

    vm.importTours(file);

    expect(dataApi.importTours).toHaveBeenCalledWith(file);
    expect(vm.importMessage()).toBe('Imported 1 tour(s).');
    expect(vm.hasImportMessage()).toBeTrue();
    expect(api.list).toHaveBeenCalled();
  });
});
