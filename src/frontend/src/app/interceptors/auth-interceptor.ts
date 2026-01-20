import { HttpInterceptorFn } from '@angular/common/http';

/*
HttpInterceptorFn > Function that intercepts HTTP requests
req > object that stores the request information
next > if the request is not corrupt, let it continue
*/

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // localStorage.getItem('token') > we look for the token in local storage
  const token = localStorage.getItem('token');

  /*
  When reaching the if statement, check if there is a token. If there is,
  make a copy of the request to add the token to the header, thus ensuring that
  all future requests automatically carry the user's identity.
  */
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
