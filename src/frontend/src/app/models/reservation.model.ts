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
