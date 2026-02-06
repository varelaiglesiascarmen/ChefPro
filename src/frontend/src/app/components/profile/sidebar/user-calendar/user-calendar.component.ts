// user-calendar.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReservationService } from '../../../../services/reservation.service';

@Component({
  selector: 'app-user-calendar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-calendar.component.html',
  styleUrls: ['./user-calendar.component.css']
})
export class UserCalendarComponent implements OnInit {
  private resService = inject(ReservationService);

  events: any[] = [];

  ngOnInit() {
    this.loadCalendar();
  }

  loadCalendar() {
    this.resService.getReservations().subscribe((data: any[]) => {
      const confirmadas = data.filter((res: any) => res.status === 'ACCEPTED');

      this.events = confirmadas.map((res: any) => ({
        title: `Servicio: ${res.menuTitle}`,
        start: res.date,
        color: '#C5A059'
      }));
    });
  }
}
