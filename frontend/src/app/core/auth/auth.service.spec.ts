import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AuthApiService } from '../api/auth-api.service';
import { AuthResponse, Credentials } from '../models/auth.model';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let api: jasmine.SpyObj<AuthApiService>;
  let service: AuthService;

  const credentials: Credentials = { username: 'lukas', password: 'secret123' };
  const response: AuthResponse = { token: 'jwt-token', username: 'lukas' };

  beforeEach(() => {
    localStorage.clear();
    api = jasmine.createSpyObj<AuthApiService>('AuthApiService', ['login', 'register']);

    TestBed.configureTestingModule({
      providers: [AuthService, { provide: AuthApiService, useValue: api }],
    });

    service = TestBed.inject(AuthService);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('stores the token and username after login', () => {
    api.login.and.returnValue(of(response));

    service.login(credentials).subscribe();

    expect(service.token()).toBe('jwt-token');
    expect(service.username()).toBe('lukas');
    expect(service.isAuthenticated()).toBeTrue();
    expect(localStorage.getItem('tp_token')).toBe('jwt-token');
  });

  it('stores the token and username after registration', () => {
    api.register.and.returnValue(of(response));

    service.register(credentials).subscribe();

    expect(api.register).toHaveBeenCalledWith(credentials);
    expect(service.isAuthenticated()).toBeTrue();
    expect(localStorage.getItem('tp_user')).toBe('lukas');
  });

  it('clears the session on logout', () => {
    api.login.and.returnValue(of(response));
    service.login(credentials).subscribe();

    service.logout();

    expect(service.token()).toBeNull();
    expect(service.username()).toBeNull();
    expect(service.isAuthenticated()).toBeFalse();
  });

  it('removes stored auth data when clearing the session directly', () => {
    localStorage.setItem('tp_token', 'old-token');
    localStorage.setItem('tp_user', 'old-user');

    service.clearSession();

    expect(localStorage.getItem('tp_token')).toBeNull();
    expect(localStorage.getItem('tp_user')).toBeNull();
  });
});
