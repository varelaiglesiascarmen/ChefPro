import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './calendar.component.html',
  styleUrl: './calendar.component.css'
})
export class CalendarComponent implements OnChanges {
  @Input() busyDates: string[] = [];
  @Output() dateSelected = new EventEmitter<string>();

  currentDate = new Date();
  selectedDate: string | null = null;
  daysInMonth: any[] = [];
  weekDays = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
  monthNames = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

  /**
   * Detects changes in busyDates input and regenerates calendar.
   * Reflects availability status in UI when busy dates change.
   */
  ngOnChanges(changes: SimpleChanges): void {
    this.generateCalendar();
  }

  /**
   * Builds calendar grid including past dates, busy slots, and available dates.
   * Computes proper alignment for first day of month using day-of-week offset.
   * Status assignment: 'past' < today, 'busy' = in busyDates array, 'selected' = user selection.
   */
  generateCalendar(): void {
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();

    // Calculate weekday offset for first day of month (0=Monday ... 6=Sunday)
    let firstDayIndex = new Date(year, month, 1).getDay();
    firstDayIndex = firstDayIndex === 0 ? 6 : firstDayIndex - 1;

    const numDays = new Date(year, month + 1, 0).getDate();
    this.daysInMonth = [];

    // Add empty cells to align first day of month correctly
    for (let i = 0; i < firstDayIndex; i++) {
      this.daysInMonth.push({ day: null, fullDate: null, status: 'empty' });
    }

    const todayStr = new Date().toISOString().split('T')[0];

    // Generate day cells with appropriate status
    for (let i = 1; i <= numDays; i++) {
      const dayStr = i < 10 ? `0${i}` : `${i}`;
      const monthStr = (month + 1) < 10 ? `0${month + 1}` : `${month + 1}`;
      const fullDate = `${year}-${monthStr}-${dayStr}`;

      let status = 'available';
      if (fullDate < todayStr) {
        status = 'past';
      } else if (this.busyDates.includes(fullDate)) {
        status = 'busy';
      } else if (fullDate === this.selectedDate) {
        status = 'selected';
      }

      this.daysInMonth.push({ day: i, fullDate, status });
    }
  }

  /**
   * Handles date selection with validation.
   * Prevents selection of past dates, busy slots, and empty cells.
   * Emits selected date to parent component for reservation system.
   */
  selectDay(dayObj: any): void {
    if (dayObj.status === 'past' || dayObj.status === 'busy' || !dayObj.day) {
      return;
    }

    this.selectedDate = dayObj.fullDate;
    this.generateCalendar();
    this.dateSelected.emit(this.selectedDate!);
  }

  /**
   * Updates calendar view to previous or next month.
   * Maintains current selection when navigating between months.
   */
  changeMonth(delta: number): void {
    const newDate = new Date(this.currentDate);
    newDate.setMonth(newDate.getMonth() + delta);
    this.currentDate = newDate;
    this.generateCalendar();
  }
}

