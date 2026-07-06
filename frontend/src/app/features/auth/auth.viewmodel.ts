import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthApiService } from '../../core/api/auth-api.service';
import { AuthService } from '../../core/auth/auth.service';
import { extractMessage } from '../../core/api/http-error';
import { Credentials } from '../../core/models/auth.model';

@Injectable()
export class AuthViewModel {
  private api = inject(AuthApiService);
  private auth = inject(AuthService);
  private router = inject(Router);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly hasError = computed(() => this.error() !== null);

  login(credentials: Credentials): void {
    this.runAuthCommand(
      this.api.login(credentials),
      'Login failed.',
    );
  }

  register(credentials: Credentials): void {
    this.runAuthCommand(
      this.api.register(credentials),
      'Registration failed.',
    );
  }

  clearError(): void {
    this.error.set(null);
  }

  private runAuthCommand(
    request: ReturnType<AuthApiService['login']>,
    fallbackMessage: string,
  ): void {
    this.loading.set(true);
    this.error.set(null);

    request.subscribe({
      next: (res) => {
        this.auth.storeSession(res);
        this.loading.set(false);
        void this.router.navigate(['/tours']);
      },
      error: (err) => {
        this.error.set(extractMessage(err, fallbackMessage));
        this.loading.set(false);
      },
    });
  }
}
