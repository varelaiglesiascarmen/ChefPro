/*  USER MODULE (USERS) */
export interface User {
  user_ID: number;
  role: 'ADMIN' | 'CHEF' | 'DINER';
  name: string;
  lastname: string;
  email: string;
  phone_number?: string;
  address?: string;
  rating_avg: number;
  reviews_count: number;
  languages?: string[];
}

/* SERVICE MODULE (MENUS & DISHES) */
export interface Dish {
  id?: number;
  title: string;
  description: string;
  category: 'Entrante' | 'Principal' | 'Postre' | 'Bebida';
  allergenIds: number[];
}

export interface Menu {
  id: number;
  chefId: string;
  chefName: string;
  title: string;
  description: string;
  price: number;
  minDiners: number;
  maxDiners: number;
  requirements: string;
  photoUrl: string;
  dishes: Dish[];
  busyDates: string[];
  rating?: number;
}

/* RESERVATION MODULE (ORDERS) */
export interface Order {
  id: string;
  chefId: string;
  dinerId: string;
  menuId: number;
  eventDate: string;
  guestsCount: number;
  totalPrice: number;
  deliveryAddress: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'COMPLETED' | 'CANCELLED';
  createdAt: string;
  isReviewedByChef: boolean;
  isReviewedByDiner: boolean;
}

/* REVIEWS */
export interface Review {
  id: string;
  orderId: string;
  fromUserId: string;
  toUserId: string;
  rating: number;
  comment: string;
  date: string;
  type: 'CHEF_TO_DINER' | 'DINER_TO_CHEF';
}

/* ACCESS INTERFACES (DTOs) */
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  token?: string;
  user?: User;
  message?: string;
}

export interface signupRequest {
  name: string;
  surname: string;
  username: string;
  email: string;
  password: string;
  role: 'DINER' | 'CHEF';
}

export interface signupResponse {
  success: boolean;
  user?: User;
  message?: string;
}
