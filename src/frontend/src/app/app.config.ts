import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { authInterceptor } from './interceptors/auth-interceptor';
import { provideAnimations } from '@angular/platform-browser/animations';

export const appConfig: ApplicationConfig = {
  // providers list we need to add the HTTP client with the interceptor
  providers: [
    provideRouter(routes),
    // Tell Angular that we need de HTTP protocol with our interceptor.ts
    provideHttpClient(
      // Command to Angular: “Every time you use the HTTP client, make sure it goes through this interceptor.”
      withInterceptors([authInterceptor])
    ),
    provideAnimations()
  ]
};
