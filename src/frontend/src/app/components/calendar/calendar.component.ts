import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'calendar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.css']
})
export class CalendarComponent implements OnChanges {

  @Input() busyDates: string[] = []; 
  @Output() dateSelected = new EventEmitter<string>();

  currentDate = new Date();
  selectedDate: string | null = null;

  daysInMonth: any[] = [];
  weekDays = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
  monthNames = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

  ngOnChanges(changes: SimpleChanges) {
    this.generateCalendar();
  }

  generateCalendar() {
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();

    let firstDayIndex = new Date(year, month, 1).getDay();
    firstDayIndex = firstDayIndex === 0 ? 6 : firstDayIndex - 1;

    const numDays = new Date(year, month + 1, 0).getDate();

    this.daysInMonth = [];

    for (let i = 0; i < firstDayIndex; i++) {
      this.daysInMonth.push({ day: null, fullDate: null, status: 'empty' });
    }

    const todayStr = new Date().toISOString().split('T')[0];

    for (let i = 1; i <= numDays; i++) {
      const dayStr = i < 10 ? `0${i}` : `${i}`;
      const monthStr = (month + 1) < 10 ? `0${month + 1}` : `${month + 1}`;
      const fullDate = `${year}-${monthStr}-${dayStr}`;

      let status = 'available';

      if (fullDate < todayStr) status = 'past';
      else if (this.busyDates.includes(fullDate)) status = 'busy';
      else if (fullDate === this.selectedDate) status = 'selected';

      this.daysInMonth.push({ day: i, fullDate: fullDate, status: status });
    }
  }

  selectDay(dayObj: any) {
    if (dayObj.status === 'past' || dayObj.status === 'busy' || !dayObj.day) return;

    this.selectedDate = dayObj.fullDate;
    this.generateCalendar();
    this.dateSelected.emit(this.selectedDate!);
  }

  changeMonth(delta: number) {
    const newDate = new Date(this.currentDate);
    newDate.setMonth(newDate.getMonth() + delta);
    this.currentDate = newDate;
    this.generateCalendar();
  }
}
