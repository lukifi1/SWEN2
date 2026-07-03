import { Component, EventEmitter, Input, Output } from '@angular/core';

/**
 * Reusable button component used across the whole application.
 * Encapsulates the visual variants (primary / secondary / danger) and a
 * full-width mode, so call sites stay declarative.
 */
@Component({
  selector: 'app-action-button',
  standalone: true,
  template: `
    <button
      [type]="type"
      [disabled]="disabled"
      [class]="variant"
      [class.full-width]="fullWidth"
      (click)="buttonClick.emit()">
      {{ label }}
    </button>
  `,
  styles: [`
    button {
      border: none;
      border-radius: 10px;
      padding: 10px 16px;
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
      transition: filter 0.15s ease, transform 0.15s ease;
    }
    button:hover:not(:disabled) { filter: brightness(0.95); transform: translateY(-1px); }
    button:disabled { opacity: 0.55; cursor: not-allowed; }
    .full-width { width: 100%; }
    .primary { background: #2563eb; color: #fff; }
    .secondary { background: #e2e8f0; color: #0f172a; }
    .danger { background: #dc2626; color: #fff; }
  `],
})
export class ActionButtonComponent {
  @Input() label = 'Button';
  @Input() type: 'button' | 'submit' = 'button';
  @Input() variant: 'primary' | 'secondary' | 'danger' = 'primary';
  @Input() disabled = false;
  @Input() fullWidth = false;

  @Output() buttonClick = new EventEmitter<void>();
}
