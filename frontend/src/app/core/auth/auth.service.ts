import { Injectable, computed, signal } from '@angular/core';
import { AuthResponse } from '../models/auth.model';

const TOKEN_KEY = 'tp_token';
const USER_KEY = 'tp_user';

/**
 * Holds the global authentication session state and persists it in localStorage.
 * UI flows and API orchestration belong to auth view-models.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly token = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  readonly username = signal<string | null>(localStorage.getItem(USER_KEY));
  readonly isAuthenticated = computed(() => this.token() !== null);

  logout(): void {
    this.clearSession();
  }

  storeSession(res: AuthResponse): void {
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
