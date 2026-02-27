import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface Toast {
  id: string;
  message: string;
  type: 'success' | 'error' | 'info' | 'warning';
  duration: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private toasts$ = new BehaviorSubject<Toast[]>([]);
  public toasts: Observable<Toast[]> = this.toasts$.asObservable();

  show(message: string, type: 'success' | 'error' | 'info' | 'warning' = 'info', duration = 4000) {
    const id = Date.now().toString();
    const toast: Toast = { id, message, type, duration };

    const current = this.toasts$.value;
    this.toasts$.next([...current, toast]);

    setTimeout(() => {
      this.toasts$.next(this.toasts$.value.filter(t => t.id !== id));
    }, duration);
  }

  success(msg: string, duration = 4000) {
    this.show(msg, 'success', duration);
  }

  error(msg: string, duration = 5000) {
    this.show(msg, 'error', duration);
  }

  info(msg: string, duration = 4000) {
    this.show(msg, 'info', duration);
  }

  warning(msg: string, duration = 4000) {
    this.show(msg, 'warning', duration);
  }
}
