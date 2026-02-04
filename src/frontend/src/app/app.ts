import { Component, OnInit, inject } from '@angular/core';
import { Router, NavigationEnd, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';
import { NavbarComponent } from './components/navbar/navbar.component';
import { FooterComponent } from './components/footer/footer.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    NavbarComponent,
    FooterComponent
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent implements OnInit {
  title = 'chefpro';

  private router = inject(Router);
  // This variable controls w to display > navbar/footer from homePage or general navbar/footer
  showGlobalNav = true;

  ngOnInit() {
    // where is user
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      /*
      VISUAL BUSINESS LOGIC:
        - If you are on the home page (‘/homepage’ or ‘/’), we hide the global navbar/footer.
        - If you navigate to search for chefs, login, etc., we display them.
      */
      const isHome = event.urlAfterRedirects.includes('/homepage') || event.urlAfterRedirects === '/';

      this.showGlobalNav = !isHome;
    });
  }
}
