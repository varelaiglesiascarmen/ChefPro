import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateDishComponent } from './create-dish.component';

describe('CreateDishComponent', () => {
  let component: CreateDishComponent;
  let fixture: ComponentFixture<CreateDishComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateDishComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateDishComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
