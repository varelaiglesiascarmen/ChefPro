/**
 * DTOs for the public chef profile and menu detail responses.
 * Map 1:1 to the backend DTOs (ChefPublicDetailDto, MenuPublicDetailDto).
 */

export interface ReviewSummary {
  reviewerName: string;
  date: string;
  score: number;
  comment: string;
}

export interface MenuSummary {
  id: number;
  title: string;
  description: string;
  price: number;
  dishesCount: number;
  minDiners: number;
  maxDiners: number;
}

export interface ChefPublicDetail {
  id: number;
  name: string;
  lastname: string;
  fullName: string;
  email: string;
  phoneNumber: string;
  photo: string;
  bio: string;
  prizes: string;
  location: string;
  languages: string;
  coverPhoto: string;
  rating: number;
  reviewsCount: number;
  menus: MenuSummary[];
  reviews: ReviewSummary[];
  busyDates: string[];
}

export interface DishPublic {
  dishId: number;
  title: string;
  description: string;
  category: string;
  allergenIds: number[];
}

export interface MenuPublicDetail {
  id: number;
  title: string;
  description: string;
  price: number;
  minDiners: number;
  maxDiners: number;
  requirements: string;
  chefId: number;
  chefName: string;
  chefPhoto: string;
  dishes: DishPublic[];
  busyDates: string[];
}

/**
 * Interface for partial chef profile updates.
 * All fields are optional (semantic PATCH).
 */
export interface ChefProfileUpdate {
  photo?: string;
  bio?: string;
  prizes?: string;
  location?: string;
  languages?: string;
  coverPhoto?: string;
}
