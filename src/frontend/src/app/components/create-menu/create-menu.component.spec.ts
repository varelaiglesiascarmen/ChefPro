import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateMenuComponent } from './create-menu.component';

describe('CreateMenuComponent', () => {
  let component: CreateMenuComponent;
  let fixture: ComponentFixture<CreateMenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateMenuComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateMenuComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
