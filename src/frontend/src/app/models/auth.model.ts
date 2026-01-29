// Object > login attempt

// form data
export interface LoginRequest {
  username: string;
  password: string;
}

// W defines your user throughout the entire app
export interface User {
  id: string;
  username: string;
  email: string;
  name: string;
  photoUrl?: string; // ? = Opcional
  role: 'CHEF' | 'CLIENT' | 'ADMIN';

  // recommendation algorithm
  preferences?: {
    dietary?: string[];
    favoriteCuisines?: string[];
    location?: string;
  };
}

// Object > login, what the server should return
export interface LoginResponse {
  success: boolean;
  token?: string;
  user?: User;
  message?: string;
}

// Object > register attempt, what the server should return
export interface RegisterRequest {
    name: string;
    surname: string;
    username: string;
    email: string;
    password: string;
}
