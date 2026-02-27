import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { ToastService } from '../services/toast.service';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Algo salió mal. Intenta de nuevo.';

      if (error.status === 401) {
        errorMessage = 'Tu sesión ha expirado. Inicia sesión nuevamente.';
        router.navigate(['/login']);
      } else if (error.status === 403) {
        errorMessage = 'No tienes permiso para realizar esta acción.';
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
      console.error('HTTP Error:', { status: error.status, message: errorMessage });

      return throwError(() => error);
    })
  );
};

