import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ChefService } from '../../services/chef.service';
import { PublicProfile } from '../../interfaces/profile.interface';

@Component({
  selector: 'app-public-profile',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './public-profile.html',
  styleUrl: './public-profile.css',
})
export class PublicProfileComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private chefService = inject(ChefService);
  private cdr = inject(ChangeDetectorRef);

  isLoading = true;
  errorMessage: string | null = null;
  chef: PublicProfile | null = null;

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));

      if (!id || Number.isNaN(id)) {
        this.errorMessage = 'No se ha encontrado el usuario.';
        this.isLoading = false;
        this.cdr.detectChanges();
        return;
      }

      this.fetchProfile(id);
    });
  }

  get languageTags(): string[] {
    if (!this.chef?.languages) return [];
    return this.chef.languages
      .split(',')
      .map(language => language.trim())
      .filter(Boolean);
  }

  get heroCover(): string {
    return this.chef?.coverPhoto || this.chef?.photo || '/logos/users.svg';
  }

  get avatar(): string {
    return this.chef?.photo || '/logos/users.svg';
  }

  get reviewsStars(): number[] {
    return [1, 2, 3, 4, 5];
  }

  goToMenu(menuId: number): void {
    this.router.navigate(['/service-detail', 'menu', menuId]);
  }

  goBack(): void {
    this.router.navigate(['/search-results']);
  }

  private fetchProfile(id: number): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.chefService.getPublicProfile(id).subscribe({
      next: (profile) => {
        // El endpoint devuelve datos genéricos que funcionan para chef y diner
        this.chef = {
          ...profile,
          fullName: `${profile.name} ${profile.lastname}`,
        };
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se ha encontrado el perfil.';
        this.isLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

}
