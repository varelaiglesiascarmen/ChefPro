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

export const routes: Routes = [
  // If the path is empty (‘’), redirect to 'homepage'
  { path: '', redirectTo: 'homepage', pathMatch: 'full' },

  // homePage root
  { path: 'homepage', component: HomePageComponent },

  // login root
  { path: 'login', component: LoginComponent },

  // signin root
  { path: 'signIn', component: SignInComponent },

  // index root
  { path: 'index', component: IndexComponent },

  // contact root
  { path: 'contact', component: ContactComponent },

  // about root
  { path: 'about', component: AboutComponent },

  // not found root
  { path: 'not-found', component: NotFoundComponent },

  // profile root
  { path: 'profile', component: ProfileComponent },

  // user-menu root
  { path: 'user-menu', component: UserMenuComponent },

  // search-results root
  { path: 'search-results', component: SearchResultsComponent },

  // new-menu root
  { path: 'new-menu', component: NewMenuComponent },

  // service-detail root
  { path: 'service-detail/:type/:id', component: ServiceDetailPageComponent },

  // If you write something strange, send it to the home page
  { path: '**', redirectTo: 'homepage' }
];
