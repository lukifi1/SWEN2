import { Component, EventEmitter, Input, Output } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { TourLog } from '../shared/models/tour.model';
import { ActionButtonComponent } from '../shared/action-button/action-button.component';

@Component({
  selector: 'app-tour-logs',
  standalone: true,
  imports: [FormsModule, ActionButtonComponent],
  templateUrl: './tour-logs.component.html',
  styles: [`
    .log-list { display: flex; flex-direction: column; gap: 12px; }
    .log-card { border: 1px solid #e5e7eb; border-radius: 14px; padding: 16px; background: #ffffff; }
    p { margin: 0 0 6px 0; }
  `]
})
export class TourLogsComponent {
  @Input() logs: TourLog[] = [];
  @Output() logsChange = new EventEmitter<TourLog[]>();

  logForm: TourLog = this.createEmptyLog();
  editingLogId: number | null = null;

  saveLog() {
    let updatedLogs = [...this.logs];
    if (this.editingLogId) {
      const index = updatedLogs.findIndex(l => l.id === this.editingLogId);
      updatedLogs[index] = { ...this.logForm, id: this.editingLogId };
    } else {
      updatedLogs.push({ ...this.logForm, id: Date.now() });
    }
    this.logsChange.emit(updatedLogs);
    this.resetLogForm();
  }

  editLog(log: TourLog) {
    this.logForm = { ...log };
    this.editingLogId = log.id;
  }

  deleteLog(id: number) {
    this.logsChange.emit(this.logs.filter(l => l.id !== id));
    if (this.editingLogId === id) this.resetLogForm();
  }

  resetLogForm() {
    this.logForm = this.createEmptyLog();
    this.editingLogId = null;
  }

  createEmptyLog(): TourLog {
    return { id: 0, dateTime: '', comment: '', difficulty: 1, totalDistance: 0, totalTime: 0, rating: 1 };
  }
}
