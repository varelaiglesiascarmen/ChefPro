import { Component, inject, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-review-form',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <div class="review-modal-overlay" *ngIf="isOpen" (click)="close()">
      <div class="review-modal" (click)="$event.stopPropagation()">
        <div class="review-header">
          <h3>Valora tu experiencia</h3>
          <button class="close-btn" (click)="close()" type="button">×</button>
        </div>

        <form [formGroup]="reviewForm" (ngSubmit)="submitReview()" class="review-form">
          <!-- Rating -->
          <div class="form-group">
            <label>Puntuación</label>
            <div class="star-rating">
              <button
                type="button"
                *ngFor="let i of [1, 2, 3, 4, 5]"
                (click)="setRating(i)"
                [class.active]="rating >= i"
                class="star"
              >
                ★
              </button>
            </div>
            <span class="rating-text">{{ rating > 0 ? rating + ' de 5 estrellas' : 'Selecciona una puntuación' }}</span>
          </div>

          <!-- Comment -->
          <div class="form-group">
            <label>Tu comentario (opcional)</label>
            <textarea
              formControlName="comment"
              placeholder="Comparte tu experiencia con este chef..."
              maxlength="500"
              rows="4"
            ></textarea>
            <span class="char-count">{{ (reviewForm.get('comment')?.value || '').length }} / 500</span>
          </div>

          <!-- Actions -->
          <div class="review-actions">
            <button type="button" class="btn-secondary" (click)="close()" [disabled]="isSubmitting">
              Cancelar
            </button>
            <button type="submit" class="btn-primary" [disabled]="rating === 0 || isSubmitting">
              {{ isSubmitting ? 'Enviando...' : 'Enviar valoración' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .review-modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(44, 24, 16, 0.7);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
      animation: fadeIn 0.2s ease;
    }

    .review-modal {
      background: white;
      border-radius: 12px;
      padding: 28px 24px;
      max-width: 420px;
      width: 90%;
      box-shadow: 0 20px 60px rgba(44, 24, 16, 0.2);
      animation: slideUp 0.3s ease;
    }

    .review-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    .review-header h3 {
      margin: 0;
      color: #2C1810;
      font-size: 18px;
      font-family: 'Playfair Display', serif;
    }

    .close-btn {
      background: none;
      border: none;
      font-size: 26px;
      cursor: pointer;
      color: #999;
      padding: 0;
      width: 28px;
      height: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: color 0.2s;
    }

    .close-btn:hover {
      color: #2C1810;
    }

    .form-group {
      margin-bottom: 20px;
    }

    label {
      display: block;
      margin-bottom: 10px;
      font-weight: 600;
      color: #2C1810;
      font-size: 14px;
    }

    .star-rating {
      display: flex;
      gap: 8px;
      margin-bottom: 10px;
    }

    .star {
      background: none;
      border: none;
      font-size: 32px;
      cursor: pointer;
      color: #e5e5e5;
      transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
      padding: 0;
    }

    .star:hover {
      color: #ffc107;
      transform: scale(1.2);
    }

    .star.active {
      color: #ffc107;
    }

    .rating-text {
      font-size: 12px;
      color: #999;
    }

    textarea {
      width: 100%;
      padding: 10px;
      border: 1px solid #e5e5e5;
      border-radius: 6px;
      font-family: 'Montserrat', sans-serif;
      resize: none;
      font-size: 14px;
      color: #333;
    }

    textarea:focus {
      outline: none;
      border-color: #C5A059;
      box-shadow: 0 0 0 2px rgba(197, 160, 89, 0.1);
    }

    .char-count {
      font-size: 12px;
      color: #999;
      float: right;
      margin-top: 6px;
    }

    .review-actions {
      display: flex;
      gap: 12px;
      margin-top: 24px;
      clear: both;
    }

    .btn-secondary,
    .btn-primary {
      flex: 1;
      padding: 10px;
      border: none;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
      font-size: 14px;
    }

    .btn-secondary {
      background: #f9f9f9;
      color: #2C1810;
      border: 1px solid #e5e5e5;
    }

    .btn-secondary:hover:not(:disabled) {
      background: #f0f0f0;
      border-color: #d5d5d5;
    }

    .btn-primary {
      background: #C5A059;
      color: white;
    }

    .btn-primary:hover:not(:disabled) {
      background: #a67c2f;
    }

    .btn-primary:disabled,
    .btn-secondary:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    @keyframes fadeIn {
      from {
        opacity: 0;
      }
      to {
        opacity: 1;
      }
    }

    @keyframes slideUp {
      from {
        transform: translateY(20px);
        opacity: 0;
      }
      to {
        transform: translateY(0);
        opacity: 1;
      }
    }

    @media (max-width: 640px) {
      .review-modal {
        padding: 20px;
      }

      .star {
        font-size: 28px;
      }

      .review-header h3 {
        font-size: 16px;
      }
    }
  `]
})
export class ReviewFormComponent implements OnInit {
  @Input() isOpen = false;
  @Input() chefId!: number;
  @Input() reservationDate!: string;
  @Output() closed = new EventEmitter<void>();
  @Output() submitted = new EventEmitter<{ chefId: number; reservationDate: string }>();

  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private toastService = inject(ToastService);

  reviewForm!: FormGroup;
  rating = 0;
  isSubmitting = false;

  ngOnInit() {
    this.reviewForm = this.fb.group({
      comment: ['', [Validators.maxLength(500)]]
    });
  }

  setRating(score: number) {
    this.rating = score;
  }

  submitReview() {
    if (this.rating === 0) {
      this.toastService.warning('Por favor selecciona una puntuación');
      return;
    }

    if (!this.chefId || !this.reservationDate) {
      this.toastService.error('No se pudo cargar la información de la reserva');
      return;
    }

    this.isSubmitting = true;
    const payload = {
      chefId: this.chefId,
      reservationDate: this.reservationDate,
      score: this.rating,
      comment: this.reviewForm.get('comment')?.value || ''
    };

    this.http.post(`${environment.apiUrl}/reservations/review`, payload).subscribe({
      next: () => {
        this.toastService.success('Valoración enviada correctamente');
        this.submitted.emit({ chefId: this.chefId, reservationDate: this.reservationDate });
        this.close();
        this.isSubmitting = false;
      },
      error: (err) => {
        this.isSubmitting = false;
        const msg = err?.error?.error || '';
        if (msg.includes('already submitted')) {
          this.toastService.warning('Ya has valorado a este chef anteriormente.');
          this.close();
        } else {
          this.toastService.error('Vaya, algo no ha ido bien al enviar tu valoraci\u00f3n. \u00bfPuedes intentarlo de nuevo?');
        }
      }
    });
  }

  close() {
    this.isOpen = false;
    this.rating = 0;
    this.reviewForm.reset();
    this.closed.emit();
  }
}
