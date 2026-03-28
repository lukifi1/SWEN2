import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TourLogsComponent } from './tour-logs.component';

describe('TourLogsComponent', () => {
  let component: TourLogsComponent;
  let fixture: ComponentFixture<TourLogsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TourLogsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TourLogsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
