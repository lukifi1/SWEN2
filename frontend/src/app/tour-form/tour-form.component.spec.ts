import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TourFormComponent } from './tour-form.component';

describe('TourFormComponent', () => {
  let component: TourFormComponent;
  let fixture: ComponentFixture<TourFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TourFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TourFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
