import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';
import { ChefService } from '../../services/chef.service';

import { PublicProfileComponent } from './public-profile';

describe('PublicProfileComponent', () => {
  let component: PublicProfileComponent;
  let fixture: ComponentFixture<PublicProfileComponent>;

  const chefServiceMock = {
    getPublicProfile: () => of({
      id: 1,
      name: 'Laura',
      lastname: 'Santos',
      fullName: 'Laura Santos',
      email: 'laura@chefpro.com',
      phoneNumber: '600000000',
      photo: '',
      bio: 'Chef especializada en cocina de temporada.',
      prizes: '',
      location: 'Madrid',
      languages: 'Español, English',
      coverPhoto: '',
      rating: 4.8,
      reviewsCount: 12,
      menus: [],
      reviews: [],
      busyDates: []
    })
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PublicProfileComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(convertToParamMap({ id: '1' }))
          }
        },
        {
          provide: ChefService,
          useValue: chefServiceMock
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PublicProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
