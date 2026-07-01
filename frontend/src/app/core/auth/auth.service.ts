import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { AuthApiService } from '../api/auth-api.service';
import { AuthResponse, Credentials } from '../models/auth.model';

const TOKEN_KEY = 'tp_token';
const USER_KEY = 'tp_user';

/**
 * Holds the authentication state (signals) and persists the JWT in
 * localStorage. Acts as the view-model for login/registration state.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = inject(AuthApiService);

  readonly token = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  readonly username = signal<string | null>(localStorage.getItem(USER_KEY));
  readonly isAuthenticated = computed(() => this.token() !== null);

  login(credentials: Credentials): Observable<AuthResponse> {
    return this.api.login(credentials).pipe(tap((res) => this.storeSession(res)));
  }

  register(credentials: Credentials): Observable<AuthResponse> {
    return this.api.register(credentials).pipe(tap((res) => this.storeSession(res)));
  }

  logout(): void {
    this.clearSession();
  }

  private storeSession(res: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    localStorage.setItem(USER_KEY, res.username);
    this.token.set(res.token);
    this.username.set(res.username);
  }

  clearSession(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.token.set(null);
    this.username.set(null);
  }
}
