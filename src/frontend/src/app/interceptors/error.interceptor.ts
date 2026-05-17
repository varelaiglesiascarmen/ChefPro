import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { ToastService } from '../services/toast.service';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const toastService = inject(ToastService);

  // Auth endpoints (login, signup) handle their own errors in the component
  const isAuthRequest = req.url.includes('/auth/login') || req.url.includes('/auth/signup');

  // Public endpoints should not trigger global session-expired redirects
  const publicEndpoints = [
    '/api/auth/health',
    '/api/auth/check-username',
    '/api/auth/check-email',
    '/api/chef/menus/public',
    '/api/chef/search',
    '/api/public-profile'
  ];

  const publicPatterns: RegExp[] = [
    /\/api\/chef\/\d+\/profile$/,
    /\/api\/chef\/menus\/\d+\/public$/
  ];

  const isPublicRequest =
    publicEndpoints.some(endpoint => req.url.includes(endpoint)) ||
    publicPatterns.some(pattern => pattern.test(req.url));

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (isAuthRequest) {
        return throwError(() => error);
      }

      let errorMessage = 'Algo salió mal. Intenta de nuevo.';

      if (error.status === 401) {
        if (isPublicRequest) {
          return throwError(() => error);
        }
        errorMessage = 'Tu sesión ha expirado. Inicia sesión nuevamente.';
        router.navigate(['/login']);
      } else if (error.status === 403) {
        // 403 errors are handled individually by each component
        return throwError(() => error);
      } else if (error.status === 404) {
        errorMessage = 'El recurso que buscas no existe.';
      } else if (error.status === 409) {
        errorMessage = error.error?.message || 'Hay un conflicto con tu solicitud. Intenta más tarde.';
      } else if (error.status >= 500) {
        errorMessage = 'El servidor experimenta problemas. Por favor, intenta más tarde.';
      } else if (error.status === 0) {
        errorMessage = 'No hay conexión a internet. Verifica tu conexión.';
      } else if (error.error?.message) {
        errorMessage = error.error.message;
      }

      toastService.error(errorMessage);

      return throwError(() => error);
    })
  );
};

