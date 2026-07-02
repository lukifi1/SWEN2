import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { StatsViewModel } from './stats.viewmodel';
import { StatCardComponent } from '../../shared/stat-card/stat-card.component';
import { BarChartComponent, BarDatum } from '../../shared/bar-chart/bar-chart.component';
import { childFriendlinessLabel } from '../../core/tour-format';

@Component({
  selector: 'app-stats-page',
  standalone: true,
  providers: [StatsViewModel],
  imports: [StatCardComponent, BarChartComponent, DecimalPipe],
  templateUrl: './stats-page.component.html',
  styleUrl: './stats-page.component.css',
})
export class StatsPageComponent implements OnInit {
  protected readonly vm = inject(StatsViewModel);
  protected readonly childFriendlinessLabel = childFriendlinessLabel;

  ngOnInit(): void {
    this.vm.load();
  }

  toBars(record: Record<string, number>): BarDatum[] {
    return Object.entries(record).map(([label, value]) => ({ label, value }));
  }

  monthBars(): BarDatum[] {
    return (this.vm.stats()?.distanceOverTime ?? []).map((m) => ({
      label: m.month,
      value: m.distance,
    }));
  }
}
