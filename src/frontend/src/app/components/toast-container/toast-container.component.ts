import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container" *ngIf="(toastService.toasts | async) as toasts">
      <div
        *ngFor="let toast of toasts"
        [class]="'toast toast-' + toast.type"
        [@slideIn]
        (mouseenter)="pauseTimeout(toast.id)"
        (mouseleave)="resumeTimeout(toast.id)"
      >
        <div class="toast-content">
          <span class="toast-icon" [class]="getIconClass(toast.type)"></span>
          <span class="toast-message">{{ toast.message }}</span>
        </div>
        <button class="toast-close" (click)="removeToast(toast.id)" type="button" aria-label="Cerrar notificación">
          ×
        </button>
      </div>
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 12px;
      max-width: 400px;
      pointer-events: none;
    }

    .toast {
      padding: 14px 16px;
      border-radius: 8px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      animation: slideIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
      font-size: 14px;
      font-weight: 500;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
      pointer-events: auto;
      backdrop-filter: blur(4px);
    }

    .toast-content {
      display: flex;
      align-items: center;
      gap: 12px;
      flex: 1;
    }

    .toast-icon {
      font-size: 18px;
      flex-shrink: 0;
    }

    .toast-message {
      line-height: 1.4;
    }

    .toast-success {
      background: rgba(16, 185, 129, 0.95);
      color: white;
      border-left: 4px solid #10b981;
    }

    .toast-error {
      background: rgba(239, 68, 68, 0.95);
      color: white;
      border-left: 4px solid #ef4444;
    }

    .toast-info {
      background: rgba(59, 130, 246, 0.95);
      color: white;
      border-left: 4px solid #3b82f6;
    }

    .toast-warning {
      background: rgba(245, 158, 11, 0.95);
      color: white;
      border-left: 4px solid #f59e0b;
    }

    .toast-close {
      background: none;
      border: none;
      color: inherit;
      font-size: 24px;
      cursor: pointer;
      margin-left: 12px;
      padding: 0;
      flex-shrink: 0;
      opacity: 0.8;
      transition: opacity 0.2s;
    }

    .toast-close:hover {
      opacity: 1;
    }

    @keyframes slideIn {
      from {
        transform: translateX(420px);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }

    @media (max-width: 640px) {
      .toast-container {
        left: 10px;
        right: 10px;
        max-width: none;
        top: 10px;
      }

      .toast {
        padding: 12px 14px;
        font-size: 13px;
      }

      .toast-icon {
        font-size: 16px;
      }

      .toast-message {
        word-break: break-word;
      }
    }
  `]
})
export class ToastContainerComponent {
  toastService = inject(ToastService);
  private timeoutMap = new Map<string, NodeJS.Timeout>();

  getIconClass(type: string): string {
    const icons: Record<string, string> = {
      success: 'fa-solid fa-circle-check',
      error: 'fa-solid fa-circle-xmark',
      info: 'fa-solid fa-circle-info',
      warning: 'fa-solid fa-triangle-exclamation'
    };
    return icons[type] || 'fa-solid fa-circle-info';
  }

  removeToast(id: string) {
    if (this.timeoutMap.has(id)) {
      clearTimeout(this.timeoutMap.get(id)!);
      this.timeoutMap.delete(id);
    }
  }

  pauseTimeout(id: string) {
    if (this.timeoutMap.has(id)) {
      clearTimeout(this.timeoutMap.get(id)!);
      this.timeoutMap.delete(id);
    }
  }

  resumeTimeout(id: string) {
    // El toast seguirá visible hasta que se dispare el setTimeout original
    // Este es más un placeholder para la pausa en hover
  }
}
