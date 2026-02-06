import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserCalendarComponent } from './user-calendar.component';

describe('UserCalendarComponent', () => {
  let component: UserCalendarComponent;
  let fixture: ComponentFixture<UserCalendarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserCalendarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserCalendarComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
