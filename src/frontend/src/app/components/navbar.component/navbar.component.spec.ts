// Unit test for NavbarComponent
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NavbarComponent } from './navbar.component';

// Verify that the navbar is created successfully
describe('NavbarComponent', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;

  // Clear the status before each test
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavbarComponent]
    })
    .compileComponents();

    // Create the component instance
    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  // Verify that the component is created successfully
  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
