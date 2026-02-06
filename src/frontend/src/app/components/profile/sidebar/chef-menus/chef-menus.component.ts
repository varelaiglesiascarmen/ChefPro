import { Component, OnInit, inject } from '@angular/core';
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

  menus: any[] = [];

  ngOnInit() {
    this.loadMenus();
  }

  loadMenus() {
    const chefId = this.authService.currentUserValue?.user_ID;
    if (chefId) {
      this.menuService.getMenusByChef(chefId).subscribe(data => {
        this.menus = data;
      });
    }
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
