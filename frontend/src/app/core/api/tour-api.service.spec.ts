import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Tour, TourCreate } from '../models/tour.model';
import { API_BASE } from './api.config';
import { TourApiService } from './tour-api.service';

describe('TourApiService', () => {
  let service: TourApiService;
  let http: HttpTestingController;

  const tour: Tour = {
    id: 1,
    name: 'Vienna Tour',
    description: 'City route',
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

  const dto: TourCreate = {
    name: 'Vienna Tour',
    description: 'City route',
    fromLocation: 'Vienna',
    toLocation: 'Graz',
    transportType: 'bike',
    imagePath: null,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TourApiService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(TourApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('loads all tours from the tours endpoint', () => {
    service.list().subscribe((tours) => expect(tours).toEqual([tour]));

    const req = http.expectOne(`${API_BASE}/tours`);
    expect(req.request.method).toBe('GET');
    req.flush([tour]);
  });

  it('sends the search query as a q parameter', () => {
    service.search('bike').subscribe((tours) => expect(tours).toEqual([tour]));

    const req = http.expectOne((request) =>
      request.url === `${API_BASE}/tours/search` && request.params.get('q') === 'bike',
    );
    expect(req.request.method).toBe('GET');
    req.flush([tour]);
  });

  it('posts new tours to the tours endpoint', () => {
    service.create(dto).subscribe((created) => expect(created).toEqual(tour));

    const req = http.expectOne(`${API_BASE}/tours`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush(tour);
  });

  it('updates and deletes tours by id', () => {
    service.update(1, dto).subscribe((updated) => expect(updated).toEqual(tour));
    const put = http.expectOne(`${API_BASE}/tours/1`);
    expect(put.request.method).toBe('PUT');
    expect(put.request.body).toEqual(dto);
    put.flush(tour);

    service.remove(1).subscribe((result) => expect(result).toBeNull());
    const del = http.expectOne(`${API_BASE}/tours/1`);
    expect(del.request.method).toBe('DELETE');
    del.flush(null);
  });
});
