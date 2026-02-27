import { Routes } from '@angular/router';
import { HomePageComponent } from './components/home-page/home-page.component';
import { LoginComponent } from './components/login/login.component';
import { SignInComponent } from './components/sign-in/sign-in.component';
import {IndexComponent} from './components/index/index.component';
import { ContactComponent } from './components/contact/contact.component';
import { AboutComponent } from './components/about/about.component';
import { NotFoundComponent } from './components/not-found/not-found.component';
import { ProfileComponent } from './components/profile/profile.component';
import { UserMenuComponent } from './components/user-menu/user-menu.component';
import { SearchResultsComponent } from './components/search-results/search-results.component';
import { ServiceDetailPageComponent } from './components/service-detail-page/service-detail-page.component';
import { NewMenuComponent } from './components/new-menu/new-menu.component';
import { EditMenuComponent } from './components/profile/sidebar/edit-menu/edit-menu.component';
import { UserInfoComponent } from './components/profile/sidebar/user-info/user-info.component';
import { ChefMenusComponent } from './components/profile/sidebar/chef-menus/chef-menus.component';
import { UserCalendarComponent } from './components/profile/sidebar/user-calendar/user-calendar.component';
import { UserOrdersComponent } from './components/profile/sidebar/user-orders/user-orders.component';
import { CancellationPoliciesComponent } from './components/cancellation-policies/cancellation-policies.component';
import { PrivacyPolicyComponent } from './components/privacy-policy/privacy-policy.component';
import { authRedirectGuard } from './guards/auth-redirect.guard';

export const routes: Routes = [

  // If the path is empty (‘’), redirect to 'homepage'
  { path: '', redirectTo: 'homepage', pathMatch: 'full' },

  // homePage root
  { path: 'homepage', component: HomePageComponent },
  // index root
  { path: 'index', component: IndexComponent },

  // contact root
  { path: 'contact', component: ContactComponent },
  // about root
  { path: 'about', component: AboutComponent },
  // cancellation-policies root
  { path: 'cancellation-policies', component: CancellationPoliciesComponent },
  // privacy-policy root
  { path: 'privacy-policy', component: PrivacyPolicyComponent },

  // search-results root
  { path: 'search-results', component: SearchResultsComponent },
  // service-detail root
  { path: 'service-detail/:type/:id', component: ServiceDetailPageComponent },

  // login root (redirect to profile if already authenticated)
  { path: 'login', component: LoginComponent, canActivate: [authRedirectGuard] },
  // signin root (redirect to profile if already authenticated)
  { path: 'signIn', component: SignInComponent, canActivate: [authRedirectGuard] },

  //profile root
  {
    path: 'profile',
    component: ProfileComponent,
    children: [
      { path: '', redirectTo: 'info', pathMatch: 'full' },
      { path: 'info', component: UserInfoComponent },
      { path: 'menus', component: ChefMenusComponent },
      { path: 'new-menu', component: NewMenuComponent },
      { path: 'edit-menu/:id', component: EditMenuComponent },
      { path: 'calendar', component: UserCalendarComponent },
      { path: 'orders', component: UserOrdersComponent }
    ]
  },
  // user-menu root
  { path: 'user-menu', component: UserMenuComponent },

  // if there is a 404, display the error interface
  { path: '404', component: NotFoundComponent },
  // If you write something strange, send it to the home page
  { path: '**', redirectTo: 'homepage' }
];
