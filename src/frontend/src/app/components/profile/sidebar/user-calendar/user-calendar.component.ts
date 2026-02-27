// user-calendar.component.ts
import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReservationService } from '../../../../services/reservation.service';
import { AuthService } from '../../../../services/auth.service';
import { ReservationStatusUpdate } from '../../../../models/reservation.model';
import { ToastService } from '../../../../services/toast.service';

@Component({
  selector: 'app-user-calendar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-calendar.component.html',
  styleUrls: ['./user-calendar.component.css']
})
export class UserCalendarComponent implements OnInit {
  private resService = inject(ReservationService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);
  private cdr = inject(ChangeDetectorRef);

  weekDays = ['Lun', 'Mar', 'Mie', 'Jue', 'Vie', 'Sab', 'Dom'];
  currentMonth = new Date().getMonth();
  currentYear = new Date().getFullYear();
  calendarDays: CalendarDay[] = [];
  confirmedReservations: ReservationEvent[] = [];
  selectedDateKey = '';
  selectedEvents: ReservationEvent[] = [];
  nextReservation: ReservationEvent | null = null;
  cancelModalOpen = false;
  cancelModalMode: 'confirm' | 'error' = 'confirm';
  cancelModalMessage = '';
  pendingCancellation: ReservationEvent | null = null;
  isCancelling = false;

  get monthLabel(): string {
    const date = new Date(this.currentYear, this.currentMonth, 1);
    return date.toLocaleDateString('es-ES', { month: 'long' }).replace(/^./, (c) => c.toUpperCase());
  }

  ngOnInit() {
    this.loadCalendar();
  }

  loadCalendar() {
    const user = this.authService.currentUserValue;
    if (!user || user.role !== 'CHEF') {
      return;
    }

    this.resService.getChefReservations().subscribe({
      next: (data: ReservationEvent[]) => {
        this.confirmedReservations = data
          .filter(res => res.status === 'CONFIRMED' || res.status === 'ACCEPTED')
          .map(res => ({
            ...res,
            isPaid: true
          }))
          .sort((a, b) => a.date.localeCompare(b.date));

        this.nextReservation = this.confirmedReservations.find(res => new Date(res.date) >= new Date()) || null;
        this.buildCalendar();
        this.deferViewUpdate(() => this.cdr.detectChanges());
      },
      error: () => {
        this.confirmedReservations = [];
        this.buildCalendar();
        this.cdr.detectChanges();
      }
    });
  }

  goToPreviousMonth() {
    if (this.currentMonth === 0) {
      this.currentMonth = 11;
      this.currentYear -= 1;
    } else {
      this.currentMonth -= 1;
    }
    this.buildCalendar();
  }

  goToNextMonth() {
    if (this.currentMonth === 11) {
      this.currentMonth = 0;
      this.currentYear += 1;
    } else {
      this.currentMonth += 1;
    }
    this.buildCalendar();
  }

  goToCurrentMonth() {
    const today = new Date();
    this.currentMonth = today.getMonth();
    this.currentYear = today.getFullYear();
    this.buildCalendar();
  }

  isCurrentMonth(): boolean {
    const today = new Date();
    return this.currentMonth === today.getMonth() && this.currentYear === today.getFullYear();
  }

  selectDay(day: CalendarDay) {
    if (!day.dateKey) {
      return;
    }
    this.selectedDateKey = day.dateKey;
    this.selectedEvents = day.events;
  }

  openCancelModal(event: ReservationEvent) {
    this.pendingCancellation = event;
    this.cancelModalMode = 'confirm';
    this.cancelModalMessage = '';
    this.cancelModalOpen = true;
  }

  closeCancelModal() {
    if (this.isCancelling) {
      return;
    }
    this.cancelModalOpen = false;
    this.pendingCancellation = null;
    this.cancelModalMessage = '';
  }

  confirmCancelReservation() {
    const user = this.authService.currentUserValue;
    if (!user || !this.pendingCancellation) {
      this.cancelModalMode = 'error';
      this.cancelModalMessage = 'No se ha podido validar tu sesion. Vuelve a iniciar sesion e intentalo de nuevo.';
      return;
    }

    const payload: ReservationStatusUpdate = {
      chefId: user.user_ID,
      date: this.pendingCancellation.date,
      status: 'CANCELLED'
    };

    this.isCancelling = true;
    this.resService.updateReservationStatus(payload).subscribe({
      next: () => {
        this.confirmedReservations = this.confirmedReservations.filter(
          res => !this.isSameReservation(res, this.pendingCancellation as ReservationEvent)
        );
        this.buildCalendar();
        this.deferViewUpdate(() => {
          this.isCancelling = false;
          this.closeCancelModal();
          this.cdr.detectChanges();
        });
      },
      error: (err) => {
        this.deferViewUpdate(() => {
          this.isCancelling = false;
          this.cancelModalMode = 'error';
          this.cancelModalMessage = 'No pudimos cancelar la reserva. Intentalo de nuevo en unos minutos.';
          this.cdr.detectChanges();
        });
      }
    });
  }

  private buildCalendar() {
    const firstDay = new Date(this.currentYear, this.currentMonth, 1);
    const lastDay = new Date(this.currentYear, this.currentMonth + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startWeekday = (firstDay.getDay() + 6) % 7;

    const days: CalendarDay[] = [];
    const reservationsByDate = this.groupReservationsByDate();

    for (let i = 0; i < startWeekday; i += 1) {
      days.push({ label: '', dateKey: '', isCurrentMonth: false, isToday: false, events: [] });
    }

    for (let day = 1; day <= daysInMonth; day += 1) {
      const dateObj = new Date(this.currentYear, this.currentMonth, day);
      const dateKey = this.formatDateKey(dateObj);
      const events = reservationsByDate.get(dateKey) || [];
      days.push({
        label: String(day),
        dateKey,
        isCurrentMonth: true,
        isToday: this.isToday(dateObj),
        events
      });
    }

    this.calendarDays = days;

    const todayKey = this.formatDateKey(new Date());
    this.selectedDateKey = this.selectedDateKey || todayKey;
    this.selectedEvents = reservationsByDate.get(this.selectedDateKey) || [];
  }

  private groupReservationsByDate(): Map<string, ReservationEvent[]> {
    const map = new Map<string, ReservationEvent[]>();
    this.confirmedReservations.forEach(res => {
      const list = map.get(res.date) || [];
      list.push(res);
      map.set(res.date, list);
    });
    return map;
  }

  private isSameReservation(a: ReservationEvent, b: ReservationEvent): boolean {
    return (
      a.chefId === b.chefId &&
      a.date === b.date &&
      a.menuTitle === b.menuTitle &&
      a.dinerName === b.dinerName &&
      a.numberOfDiners === b.numberOfDiners &&
      (a.address || '') === (b.address || '')
    );
  }

  private deferViewUpdate(callback: () => void) {
    setTimeout(callback, 0);
  }

  private formatDateKey(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  private isToday(date: Date): boolean {
    const today = new Date();
    return (
      date.getFullYear() === today.getFullYear() &&
      date.getMonth() === today.getMonth() &&
      date.getDate() === today.getDate()
    );
  }
}

type ReservationEvent = {
  chefId?: number;
  date: string;
  menuTitle: string;
  dinerName: string;
  numberOfDiners: number;
  address?: string;
  status: string;
  isPaid?: boolean;
};

type CalendarDay = {
  label: string;
  dateKey: string;
  isCurrentMonth: boolean;
  isToday: boolean;
  events: ReservationEvent[];
};
