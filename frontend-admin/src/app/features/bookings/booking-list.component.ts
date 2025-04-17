import { Component, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface Booking {
  id: number;
  userId: number;
  flightId: number;
  status: string;
  bookingDate: string;
  passengerCount: number;
  totalPrice: number;
}

@Component({
  selector: 'app-booking-list',
  template: `
    <div class="container">
      <h2>Booking Management</h2>
      
      <mat-form-field>
        <mat-label>Filter</mat-label>
        <input matInput (keyup)="applyFilter($event)" placeholder="Search bookings..." #input>
      </mat-form-field>

      <div class="mat-elevation-z8">
        <table mat-table [dataSource]="dataSource">
          <ng-container matColumnDef="id">
            <th mat-header-cell *matHeaderCellDef> ID </th>
            <td mat-cell *matCellDef="let booking"> {{booking.id}} </td>
          </ng-container>

          <ng-container matColumnDef="userId">
            <th mat-header-cell *matHeaderCellDef> User ID </th>
            <td mat-cell *matCellDef="let booking"> {{booking.userId}} </td>
          </ng-container>

          <ng-container matColumnDef="flightId">
            <th mat-header-cell *matHeaderCellDef> Flight ID </th>
            <td mat-cell *matCellDef="let booking"> {{booking.flightId}} </td>
          </ng-container>

          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef> Status </th>
            <td mat-cell *matCellDef="let booking"> {{booking.status}} </td>
          </ng-container>

          <ng-container matColumnDef="bookingDate">
            <th mat-header-cell *matHeaderCellDef> Booking Date </th>
            <td mat-cell *matCellDef="let booking"> {{booking.bookingDate | date}} </td>
          </ng-container>

          <ng-container matColumnDef="passengerCount">
            <th mat-header-cell *matHeaderCellDef> Passengers </th>
            <td mat-cell *matCellDef="let booking"> {{booking.passengerCount}} </td>
          </ng-container>

          <ng-container matColumnDef="totalPrice">
            <th mat-header-cell *matHeaderCellDef> Total Price </th>
            <td mat-cell *matCellDef="let booking"> {{booking.totalPrice | currency}} </td>
          </ng-container>

          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef> Actions </th>
            <td mat-cell *matCellDef="let booking">
              <button mat-icon-button color="primary" (click)="viewDetails(booking)">
                <mat-icon>visibility</mat-icon>
              </button>
              <button mat-icon-button color="warn" (click)="cancelBooking(booking)" 
                      [disabled]="booking.status === 'CANCELLED'">
                <mat-icon>cancel</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>

          <tr class="mat-row" *matNoDataRow>
            <td class="mat-cell" colspan="8">No data matching the filter "{{input.value}}"</td>
          </tr>
        </table>

        <mat-paginator [pageSizeOptions]="[5, 10, 25, 100]" aria-label="Select page of bookings"></mat-paginator>
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
export class BookingListComponent implements OnInit {
  displayedColumns: string[] = [
    'id', 'userId', 'flightId', 'status', 'bookingDate', 
    'passengerCount', 'totalPrice', 'actions'
  ];
  dataSource: MatTableDataSource<Booking>;

  constructor(
    private http: HttpClient,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.dataSource = new MatTableDataSource();
  }

  ngOnInit(): void {
    this.loadBookings();
  }

  loadBookings(): void {
    this.http.get<Booking[]>(`${environment.apiUrl}/bookings`).subscribe({
      next: (bookings) => {
        this.dataSource.data = bookings;
      },
      error: (error) => {
        this.snackBar.open('Error loading bookings', 'Close', { duration: 3000 });
      }
    });
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  viewDetails(booking: Booking): void {
    // TODO: Implement booking details dialog
  }

  cancelBooking(booking: Booking): void {
    if (confirm(`Are you sure you want to cancel booking #${booking.id}?`)) {
      this.http.patch(`${environment.apiUrl}/bookings/${booking.id}/cancel`, {}).subscribe({
        next: () => {
          this.loadBookings();
          this.snackBar.open('Booking cancelled successfully', 'Close', { duration: 3000 });
        },
        error: (error) => {
          this.snackBar.open('Error cancelling booking', 'Close', { duration: 3000 });
        }
      });
    }
  }
}