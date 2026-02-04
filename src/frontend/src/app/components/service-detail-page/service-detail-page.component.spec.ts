import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceDetailPageComponent } from './service-detail-page.component';

describe('ServiceDetailPageComponent', () => {
  let component: ServiceDetailPageComponent;
  let fixture: ComponentFixture<ServiceDetailPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ServiceDetailPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ServiceDetailPageComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
