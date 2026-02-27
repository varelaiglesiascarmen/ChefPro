import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map } from 'rxjs/operators';

/**
 * Guard to prevent authenticated users from accessing login/signup pages
 * Redirects logged-in users to their profile page
 */
export const authRedirectGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.user$.pipe(
    map(user => {
      if (user) {
        // User is authenticated, redirect to profile
        router.navigate(['/profile']);
        return false;
      }
      // User is not authenticated, allow access to login/signup
      return true;
    })
  );
};
