import { Component, Input } from '@angular/core';

/** Reusable summary card showing an icon, a large value and a label. */
@Component({
  selector: 'app-stat-card',
  standalone: true,
  template: `
    <div class="stat-card">
      <div class="stat-icon">{{ icon }}</div>
      <div class="stat-body">
        <div class="stat-value">{{ value }}</div>
        <div class="stat-label">{{ label }}</div>
      </div>
    </div>
  `,
  styles: [`
    .stat-card {
      display: flex;
      align-items: center;
      gap: 14px;
      background: #fff;
      border: 1px solid var(--border);
      border-radius: 14px;
      padding: 18px;
      box-shadow: var(--shadow);
    }
    .stat-icon {
      font-size: 26px;
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #eff6ff;
      border-radius: 12px;
      flex-shrink: 0;
    }
    .stat-value { font-size: 1.6rem; font-weight: 800; line-height: 1.1; }
    .stat-label { font-size: 0.85rem; color: var(--muted); }
  `],
})
export class StatCardComponent {
  @Input() icon = '📊';
  @Input() value: string | number | null = '';
  @Input() label = '';
}
