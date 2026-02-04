// import components, services, modules, etc.
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SearchFilterComponent } from './search-filter.component';

// describe component
describe('SearchFilterComponent', () => {
  let component: SearchFilterComponent;
  let fixture: ComponentFixture<SearchFilterComponent>;

  // configure TestBed
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchFilterComponent]
    })
    .compileComponents();

    // create component and fixture
    fixture = TestBed.createComponent(SearchFilterComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  // test case: should create component
  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
