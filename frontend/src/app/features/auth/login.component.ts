import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ActionButtonComponent } from '../../shared/action-button/action-button.component';
import { AuthViewModel } from './auth.viewmodel';

@Component({
  selector: 'app-login',
  standalone: true,
  providers: [AuthViewModel],
  imports: [ReactiveFormsModule, RouterLink, ActionButtonComponent],
  templateUrl: './login.component.html',
  styleUrl: './auth.component.css',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  protected readonly vm = inject(AuthViewModel);

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.vm.login(this.form.getRawValue());
  }
}
