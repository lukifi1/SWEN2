import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from './api.config';
import { TourLog, TourLogCreate } from '../models/tour.model';

@Injectable({ providedIn: 'root' })
export class TourLogApiService {
  private http = inject(HttpClient);

  private logsUrl(tourId: number): string {
    return `${API_BASE}/tours/${tourId}/logs`;
  }

  list(tourId: number): Observable<TourLog[]> {
    return this.http.get<TourLog[]>(this.logsUrl(tourId));
  }

  create(tourId: number, log: TourLogCreate): Observable<TourLog> {
    return this.http.post<TourLog>(this.logsUrl(tourId), log);
  }

  update(tourId: number, logId: number, log: TourLogCreate): Observable<TourLog> {
    return this.http.put<TourLog>(`${this.logsUrl(tourId)}/${logId}`, log);
  }

  remove(tourId: number, logId: number): Observable<void> {
    return this.http.delete<void>(`${this.logsUrl(tourId)}/${logId}`);
  }
}
