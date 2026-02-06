/*  USER MODULE (USERS) */
export interface User {
  user_ID: number;
  role: 'ADMIN' | 'CHEF' | 'DINER';
  userName: string;
  name: string;
  lastname: string;
  email: string;
  phone_number?: string;
  reviews_count: number;
  languages?: string[];
  photoUrl?: string;
}

/* EXTENSIÓN: Exclusive data from CHEF */
export interface Chef extends User {
  role: 'CHEF';
  bio: string;
  prizes: string;
  // average score of reviews
  rating_avg: number;
  // number of reviews received
  reviews_count: number;
}

/* EXTENSIÓN: Exclusive data from DINER */
export interface Diner extends User {
  role: 'DINER';
  address: string;
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
  chefId: number;
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
  order_id: number;
  chefId: number;
  dinerId: number;
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
  fromUserId: number;
  toUserId: number;
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
