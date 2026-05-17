import { ReviewSummary } from '../models/chef-detail.model';

export interface PublicProfile{
  id: number;
  name: string;
  lastname: string;
  fullName: string;
  photo: string;
  bio: string;
  location: string;
  languages: string;
  reviews: ReviewSummary[];
  rating: number;
  reviewsCount: number;
  email?: string;
  phoneNumber?: string;
  menus?: any[];
  busyDates?: string[];
  prizes?: string;
  coverPhoto?: string;
}
