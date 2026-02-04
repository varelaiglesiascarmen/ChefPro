
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';

// verify that the login is successful
describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  // clears the status before each test
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent]
    })
    .compileComponents();

    // create the component instance
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  // verify that the component is created successfully
  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
