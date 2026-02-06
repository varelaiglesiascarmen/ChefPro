import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChefMenusComponent } from './chef-menus.component';

describe('ChefMenusComponent', () => {
  let component: ChefMenusComponent;
  let fixture: ComponentFixture<ChefMenusComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChefMenusComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChefMenusComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
