import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-dashboard',
  template: `
    <mat-sidenav-container class="sidenav-container">
      <mat-sidenav #drawer class="sidenav" fixedInViewport
          [mode]="'side'"
          [opened]="true">
        <mat-toolbar>Admin Panel</mat-toolbar>
        <mat-nav-list>
          <a mat-list-item routerLink="/dashboard/users">
            <mat-icon matListItemIcon>people</mat-icon>
            <span matListItemTitle>Users</span>
          </a>
          <a mat-list-item routerLink="/dashboard/bookings">
            <mat-icon matListItemIcon>book</mat-icon>
            <span matListItemTitle>Bookings</span>
          </a>
          <a mat-list-item routerLink="/dashboard/flights">
            <mat-icon matListItemIcon>flight</mat-icon>
            <span matListItemTitle>Flights</span>
          </a>
          <a mat-list-item routerLink="/dashboard/statistics">
            <mat-icon matListItemIcon>bar_chart</mat-icon>
            <span matListItemTitle>Statistics</span>
          </a>
        </mat-nav-list>
      </mat-sidenav>
      <mat-sidenav-content>
        <mat-toolbar color="primary">
          <span>SendByOP Admin</span>
          <span class="spacer"></span>
          <button mat-icon-button (click)="logout()">
            <mat-icon>logout</mat-icon>
          </button>
        </mat-toolbar>
        <div class="content">
          <router-outlet></router-outlet>
        </div>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .sidenav-container {
      height: 100vh;
    }
    .sidenav {
      width: 250px;
    }
    .spacer {
      flex: 1 1 auto;
    }
    .content {
      padding: 20px;
    }
    mat-toolbar {
      position: sticky;
      top: 0;
      z-index: 1;
    }
  `]
})
export class DashboardComponent {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}