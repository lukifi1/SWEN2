import { Component, Input } from '@angular/core';

export interface BarDatum {
  label: string;
  value: number;
}

/**
 * Reusable, dependency-free bar chart (CSS based) used throughout the
 * statistics dashboard for distributions and time series.
 */
@Component({
  selector: 'app-bar-chart',
  standalone: true,
  template: `
    @if (data.length === 0) {
      <p class="muted empty">No data.</p>
    } @else {
      <div class="bars">
        @for (d of data; track d.label) {
          <div class="bar-col">
            <div class="bar-value">{{ d.value }}</div>
            <div class="bar-track">
              <div class="bar-fill"
                   [style.height.%]="percentage(d.value)"
                   [style.background]="color"></div>
            </div>
            <div class="bar-label" [title]="d.label">{{ d.label }}</div>
          </div>
        }
      </div>
    }
  `,
  styles: [`
    .bars {
      display: flex;
      align-items: flex-end;
      gap: 10px;
      height: 200px;
      padding-top: 10px;
    }
    .bar-col {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      height: 100%;
      min-width: 0;
    }
    .bar-value { font-size: 12px; font-weight: 700; margin-bottom: 4px; }
    .bar-track {
      flex: 1;
      width: 100%;
      max-width: 46px;
      display: flex;
      align-items: flex-end;
      background: #f1f5f9;
      border-radius: 6px 6px 0 0;
      overflow: hidden;
    }
    .bar-fill {
      width: 100%;
      border-radius: 6px 6px 0 0;
      transition: height 0.4s ease;
      min-height: 2px;
    }
    .bar-label {
      font-size: 11px;
      color: var(--muted);
      margin-top: 6px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 100%;
    }
    .empty { padding: 40px 0; text-align: center; }
  `],
})
export class BarChartComponent {
  @Input() data: BarDatum[] = [];
  @Input() color = '#2563eb';

  percentage(value: number): number {
    const max = Math.max(...this.data.map((d) => d.value), 0);
    return max > 0 ? (value / max) * 100 : 0;
  }
}
