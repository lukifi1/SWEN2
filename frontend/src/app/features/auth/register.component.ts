import { Component, inject } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ActionButtonComponent } from '../../shared/action-button/action-button.component';
import { AuthViewModel } from './auth.viewmodel';

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return password === confirm ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-register',
  standalone: true,
  providers: [AuthViewModel],
  imports: [ReactiveFormsModule, RouterLink, ActionButtonComponent],
  templateUrl: './register.component.html',
  styleUrl: './auth.component.css',
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  protected readonly vm = inject(AuthViewModel);

  readonly form = this.fb.nonNullable.group(
    {
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordsMatch },
  );

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { username, password } = this.form.getRawValue();
    this.vm.register({ username, password });
  }
}
