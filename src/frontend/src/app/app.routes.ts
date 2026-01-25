import { Routes } from '@angular/router';
import { HomePageComponent } from './components/home-page.component/home-page.component';
import { LoginComponent } from './components/login.component/login.component';
import { SignInComponent } from './components/sign-in.component/sign-in.component';
import {IndexComponent} from './components/index.component/index.component';
import { ContactComponent } from './components/contact.component/contact.component';
import { AboutComponent } from './components/about.component/about.component';
import { NotFoundComponent } from './components/not-found.component/not-found.component';
import { ProfileComponent } from './components/profile.component/profile.component';
import { SearchComponent } from './components/search.component/search.component';
import { UserMenuComponent } from './components/user-menu.component/user-menu.component';

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

  // search root
  { path: 'search', component: SearchComponent },

  // user-menu root
  { path: 'user-menu', component: UserMenuComponent },

  // If you write something strange, send it to the home page
  { path: '**', redirectTo: 'homepage' }
];
