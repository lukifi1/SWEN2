import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from './api.config';
import { Tour, TourCreate } from '../models/tour.model';

@Injectable({ providedIn: 'root' })
export class TourApiService {
  private http = inject(HttpClient);
  private url = `${API_BASE}/tours`;

  list(): Observable<Tour[]> {
    return this.http.get<Tour[]>(this.url);
  }

  get(id: number): Observable<Tour> {
    return this.http.get<Tour>(`${this.url}/${id}`);
  }

  search(query: string): Observable<Tour[]> {
    const params = new HttpParams().set('q', query);
    return this.http.get<Tour[]>(`${this.url}/search`, { params });
  }

  create(tour: TourCreate): Observable<Tour> {
    return this.http.post<Tour>(this.url, tour);
  }

  update(id: number, tour: TourCreate): Observable<Tour> {
    return this.http.put<Tour>(`${this.url}/${id}`, tour);
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
