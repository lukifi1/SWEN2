import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthApiService } from '../../core/api/auth-api.service';
import { AuthService } from '../../core/auth/auth.service';
import { AuthResponse, Credentials } from '../../core/models/auth.model';
import { AuthViewModel } from './auth.viewmodel';

describe('AuthViewModel', () => {
  let api: jasmine.SpyObj<AuthApiService>;
  let auth: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  let vm: AuthViewModel;

  const credentials: Credentials = { username: 'lukas', password: 'secret123' };
  const response: AuthResponse = { token: 'jwt-token', username: 'lukas' };

  beforeEach(() => {
    api = jasmine.createSpyObj<AuthApiService>('AuthApiService', ['login', 'register']);
    auth = jasmine.createSpyObj<AuthService>('AuthService', ['storeSession']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    router.navigate.and.resolveTo(true);

    TestBed.configureTestingModule({
      providers: [
        AuthViewModel,
        { provide: AuthApiService, useValue: api },
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
      ],
    });

    vm = TestBed.inject(AuthViewModel);
  });

  it('logs in, stores the session and navigates to tours', () => {
    api.login.and.returnValue(of(response));

    vm.login(credentials);

    expect(api.login).toHaveBeenCalledWith(credentials);
    expect(auth.storeSession).toHaveBeenCalledWith(response);
    expect(router.navigate).toHaveBeenCalledWith(['/tours']);
    expect(vm.loading()).toBeFalse();
    expect(vm.error()).toBeNull();
  });

  it('registers, stores the session and navigates to tours', () => {
    api.register.and.returnValue(of(response));

    vm.register(credentials);

    expect(api.register).toHaveBeenCalledWith(credentials);
    expect(auth.storeSession).toHaveBeenCalledWith(response);
    expect(router.navigate).toHaveBeenCalledWith(['/tours']);
    expect(vm.loading()).toBeFalse();
    expect(vm.error()).toBeNull();
  });

  it('exposes login errors as UI state', () => {
    api.login.and.returnValue(throwError(() => new Error('failed')));

    vm.login(credentials);

    expect(vm.error()).toBe('Login failed.');
    expect(vm.hasError()).toBeTrue();
    expect(vm.loading()).toBeFalse();
    expect(auth.storeSession).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
