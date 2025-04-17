import { Component, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  createdAt: string;
}

@Component({
  selector: 'app-user-list',
  template: `
    <div class="container">
      <h2>User Management</h2>
      
      <mat-form-field>
        <mat-label>Filter</mat-label>
        <input matInput (keyup)="applyFilter($event)" placeholder="Search users..." #input>
      </mat-form-field>

      <div class="mat-elevation-z8">
        <table mat-table [dataSource]="dataSource">
          <ng-container matColumnDef="id">
            <th mat-header-cell *matHeaderCellDef> ID </th>
            <td mat-cell *matCellDef="let user"> {{user.id}} </td>
          </ng-container>

          <ng-container matColumnDef="email">
            <th mat-header-cell *matHeaderCellDef> Email </th>
            <td mat-cell *matCellDef="let user"> {{user.email}} </td>
          </ng-container>

          <ng-container matColumnDef="firstName">
            <th mat-header-cell *matHeaderCellDef> First Name </th>
            <td mat-cell *matCellDef="let user"> {{user.firstName}} </td>
          </ng-container>

          <ng-container matColumnDef="lastName">
            <th mat-header-cell *matHeaderCellDef> Last Name </th>
            <td mat-cell *matCellDef="let user"> {{user.lastName}} </td>
          </ng-container>

          <ng-container matColumnDef="role">
            <th mat-header-cell *matHeaderCellDef> Role </th>
            <td mat-cell *matCellDef="let user"> {{user.role}} </td>
          </ng-container>

          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef> Actions </th>
            <td mat-cell *matCellDef="let user">
              <button mat-icon-button color="primary" (click)="editUser(user)">
                <mat-icon>edit</mat-icon>
              </button>
              <button mat-icon-button color="warn" (click)="deleteUser(user)">
                <mat-icon>delete</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>

          <tr class="mat-row" *matNoDataRow>
            <td class="mat-cell" colspan="6">No data matching the filter "{{input.value}}"</td>
          </tr>
        </table>

        <mat-paginator [pageSizeOptions]="[5, 10, 25, 100]" aria-label="Select page of users"></mat-paginator>
      </div>
    </div>
  `,
  styles: [`
    .container {
      padding: 20px;
    }
    .mat-form-field {
      width: 100%;
      margin-bottom: 20px;
    }
    table {
      width: 100%;
    }
  `]
})
export class UserListComponent implements OnInit {
  displayedColumns: string[] = ['id', 'email', 'firstName', 'lastName', 'role', 'actions'];
  dataSource: MatTableDataSource<User>;

  constructor(
    private http: HttpClient,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.dataSource = new MatTableDataSource();
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.http.get<User[]>(`${environment.apiUrl}/users`).subscribe({
      next: (users) => {
        this.dataSource.data = users;
      },
      error: (error) => {
        this.snackBar.open('Error loading users', 'Close', { duration: 3000 });
      }
    });
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  editUser(user: User): void {
    // TODO: Implement edit user dialog
  }

  deleteUser(user: User): void {
    if (confirm(`Are you sure you want to delete user ${user.email}?`)) {
      this.http.delete(`${environment.apiUrl}/users/${user.id}`).subscribe({
        next: () => {
          this.loadUsers();
          this.snackBar.open('User deleted successfully', 'Close', { duration: 3000 });
        },
        error: (error) => {
          this.snackBar.open('Error deleting user', 'Close', { duration: 3000 });
        }
      });
    }
  }
}