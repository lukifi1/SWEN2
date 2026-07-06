import { TestBed } from '@angular/core/testing';
import { AuthResponse } from '../models/auth.model';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;

  const response: AuthResponse = { token: 'jwt-token', username: 'lukas' };

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [AuthService],
    });

    service = TestBed.inject(AuthService);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('stores the token and username in the current session', () => {
    service.storeSession(response);

    expect(service.token()).toBe('jwt-token');
    expect(service.username()).toBe('lukas');
    expect(service.isAuthenticated()).toBeTrue();
    expect(localStorage.getItem('tp_token')).toBe('jwt-token');
  });

  it('clears the session on logout', () => {
    service.storeSession(response);

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

  it('initializes the session from localStorage', () => {
    localStorage.setItem('tp_token', 'stored-token');
    localStorage.setItem('tp_user', 'stored-user');
    TestBed.resetTestingModule();

    TestBed.configureTestingModule({ providers: [AuthService] });
    service = TestBed.inject(AuthService);

    expect(service.token()).toBe('stored-token');
    expect(service.username()).toBe('stored-user');
    expect(service.isAuthenticated()).toBeTrue();
  });
});
