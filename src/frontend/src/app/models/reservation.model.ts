/**
 * Interface for creating a new reservation.
 * Maps to the backend DTO: ReservationsCReqDto.
 * Fields like dinerId, totalPrice and status are set server-side.
 */
export interface ReservationCreateDto {
  chefId: number;
  menuId: number;
  date: string;            // ISO format 'YYYY-MM-DD'
  numberOfDiners: number;
  address: string;
}

/**
 * Interface for updating a reservation's status.
 * Maps to the backend DTO: ReservationsUReqDto.
 */
export interface ReservationStatusUpdate {
  chefId: number;
  date: string;
  status: 'PENDING' | 'CONFIRMED' | 'REJECTED' | 'CANCELLED';
  numberOfDiners?: number;
  address?: string;
  menuId?: number;
}
