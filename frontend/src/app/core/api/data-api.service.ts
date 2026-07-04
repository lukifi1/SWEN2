import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from './api.config';
import { LocationSuggestion } from '../models/tour.model';

/** Import/export of tour data and tour-image upload. */
@Injectable({ providedIn: 'root' })
export class DataApiService {
  private http = inject(HttpClient);

  exportTours(): Observable<Blob> {
    return this.http.get(`${API_BASE}/tours/export`, { responseType: 'blob' });
  }

  importTours(file: File): Observable<{ imported: number }> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<{ imported: number }>(`${API_BASE}/tours/import`, form);
  }

  uploadImage(file: File): Observable<{ filename: string }> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<{ filename: string }>(`${API_BASE}/images`, form);
  }

  locationSuggestions(query: string): Observable<LocationSuggestion[]> {
    return this.http.get<LocationSuggestion[]>(`${API_BASE}/locations/suggest`, {
      params: { q: query },
    });
  }

  imageUrl(filename: string): string {
    return `${API_BASE}/images/${filename}`;
  }
}
