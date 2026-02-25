import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MenuService } from '../../../../services/menu.service';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-chef-menus',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './chef-menus.component.html',
  styleUrls: ['./chef-menus.component.css']
})
export class ChefMenusComponent implements OnInit {
  private router = inject(Router);
  private menuService = inject(MenuService);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  menus: any[] = [];

  ngOnInit() {
    this.loadMenus();
  }

  loadMenus() {
    this.menuService.getMenusByChef().subscribe({
      next: (data) => {
        this.menus = [...data];
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando menús:', err);
      }
    });
  }

  confirmDelete(id: number) {
    if (confirm('¿Deseas retirar este menú de tu carta profesional?')) {
      this.menuService.deleteMenu(id).subscribe(() => this.loadMenus());
    }
  }

  goToEdit(id: number) {
    this.router.navigate(['/profile/edit-menu', id]);
  }
}
