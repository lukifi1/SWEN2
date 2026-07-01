import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from './api.config';
import { AuthResponse, Credentials } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private http = inject(HttpClient);
  private url = `${API_BASE}/auth`;

  register(credentials: Credentials): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.url}/register`, credentials);
  }

  login(credentials: Credentials): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.url}/login`, credentials);
  }
}
