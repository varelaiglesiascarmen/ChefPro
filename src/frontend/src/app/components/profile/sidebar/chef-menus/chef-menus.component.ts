import { Component, OnInit, OnDestroy, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MenuService } from '../../../../services/menu.service';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-chef-menus',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './chef-menus.component.html',
  styleUrls: ['./chef-menus.component.css']
})
export class ChefMenusComponent implements OnInit, OnDestroy {
  private router = inject(Router);
  private menuService = inject(MenuService);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);
  private destroy$ = new Subject<void>();

  menus: any[] = [];

  ngOnInit() {
    this.loadMenus();
  }

  loadMenus() {
    console.log('ChefMenus: Cargando menús del chef...');
    this.menuService.getMenusByChef().pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (data) => {
        console.log('ChefMenus: Menús cargados:', data.length);
        this.menus = [...data];
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('ChefMenus: Error cargando menús:', err);
      }
    });
  }

  confirmDelete(id: number) {
    if (confirm('¿Deseas retirar este menú de tu carta profesional?')) {
      console.log('ChefMenus: Eliminando menú con ID:', id);
      this.menuService.deleteMenu(id).pipe(
        takeUntil(this.destroy$)
      ).subscribe({
        next: () => {
          console.log('ChefMenus: Menú eliminado exitosamente, recargando lista...');
          this.loadMenus();
        },
        error: (err) => {
          console.error('ChefMenus: Error al eliminar menú:', err);

          // Verificar status HTTP 403 (Forbidden) que indica que hay restricciones (reservas)
          if (err.status === 403) {
            alert('No se puede eliminar este menú porque tiene reservas confirmadas.\n\n' +
                  'Por favor, espera a que finalicen todas las reservas antes de eliminar el menú.');
          } else {
            // Verificar si el mensaje contiene información sobre reservas
            const errorMsg = err.error?.message || err.error || err.message || '';
            if (errorMsg.includes('reservas confirmadas') ||
                errorMsg.includes('reservations') ||
                errorMsg.includes('foreign key constraint')) {
              alert('No se puede eliminar este menú porque tiene reservas confirmadas.\n\n' +
                    'Por favor, espera a que finalicen todas las reservas antes de eliminar el menú.');
            } else {
              alert('No se pudo eliminar el menú. Inténtalo de nuevo.');
            }
          }
        }
      });
    }
  }

  goToEdit(id: number) {
    this.router.navigate(['/profile/edit-menu', id]);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
