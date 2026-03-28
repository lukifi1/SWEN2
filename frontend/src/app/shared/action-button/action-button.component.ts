import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-action-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button
      [type]="type"
      [disabled]="disabled"
      [ngClass]="variant"
      (click)="buttonClick.emit()">
      {{ label }}
    </button>
  `,
  styles: [`
    button { border: none; border-radius: 10px; padding: 10px 14px; font-size: 14px; cursor: pointer; transition: 0.2s ease; }
    button:disabled { opacity: 0.5; cursor: not-allowed; }
    .primary { background: #2563eb; color: white; }
    .secondary { background: #e5e7eb; color: #111827; }
    .danger { background: #dc2626; color: white; }
  `]
})
export class ActionButtonComponent {
  @Input() label = 'Button';
  @Input() type: 'button' | 'submit' = 'button';
  @Input() variant: 'primary' | 'secondary' | 'danger' = 'primary';
  @Input() disabled = false;

  @Output() buttonClick = new EventEmitter<void>();
}
