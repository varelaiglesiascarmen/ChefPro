import { HttpInterceptorFn } from '@angular/common/http';

/*
HttpInterceptorFn > Function that intercepts HTTP requests
req > object that stores the request information
next > if the request is not corrupt, let it continue
*/

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Public endpoints that don't need authentication
  const publicEndpoints = [
    '/api/auth/login',
    '/api/auth/signup',
    '/api/auth/health',
    '/api/auth/check-username',
    '/api/auth/check-email',
    '/api/chef/menus/public',
    '/api/chef/search'
  ];

  // Dynamic public URL patterns (e.g. /api/chef/42/profile, /api/chef/menus/7/public)
  const publicPatterns: RegExp[] = [
    /\/api\/chef\/\d+\/profile$/,
    /\/api\/chef\/menus\/\d+\/public$/
  ];

  // Check if the request is to a public endpoint
  const isPublicEndpoint =
    publicEndpoints.some(endpoint => req.url.includes(endpoint)) ||
    publicPatterns.some(pattern => pattern.test(req.url));

  if (isPublicEndpoint) {
    return next(req);
  }

  const token = localStorage.getItem('chefpro_token');

  if (token) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    // feedback with token in header
    return next(cloned);
  }

  // If there is no token, the request follows its normal course
  return next(req);
};
