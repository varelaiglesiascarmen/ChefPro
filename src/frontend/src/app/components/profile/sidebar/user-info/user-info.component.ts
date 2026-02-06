import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { debounceTime, map, first } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthService } from '../../../../services/auth.service';
import { User, Chef, Diner } from '../../../../models/auth.model';

@Component({
  selector: 'app-user-info',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './user-info.component.html',
  styleUrls: ['./user-info.component.css']
})
export class UserInfoComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);

  profileForm!: FormGroup;
  editMode = false;
  role: 'ADMIN' | 'CHEF' | 'DINER' | null = null;

  prizesTags: string[] = [];
  currentPrizeInput: string = '';

  get profilePhotoUrl(): string {
    const photoValue = this.profileForm?.get('photo')?.value;
    return photoValue && photoValue.trim() !== '' ? photoValue : '/logos/users.svg';
  }

  ngOnInit() {
    this.initForm();

    this.authService.user$.subscribe(user => {
      console.log('User data received in user-info component:', user);

      if (user) {
        this.role = user.role;
        console.log('User role:', this.role);

        const patchData: any = {
          name: user.name,
          lastname: user.lastname,
          username: user.userName,
          photo: user.photoUrl
        };

        console.log('Patch data prepared:', patchData);

        if (user.role === 'CHEF') {
          const chef = user as Chef;
          patchData.bio = chef.bio || '';
          if (chef.prizes) {
            this.prizesTags = chef.prizes.split(',').map(p => p.trim()).filter(p => p !== '');
          }
        } else if (user.role === 'DINER') {
          const diner = user as Diner;
          patchData.address = diner.address || '';
        }

        this.profileForm.patchValue(patchData);
        this.profileForm.get('emailGroup.email')?.setValue(user.email);
        this.profileForm.get('emailGroup.confirmEmail')?.setValue(user.email);

        console.log('Form patched with values:', this.profileForm.value);
      }
    });
  }

  private initForm() {
    this.profileForm = this.fb.group({
      name: [''],
      lastname: [''],
      username: ['', [Validators.required], [this.usernameValidator.bind(this)]],
      photo: [''],
      bio: ['', [Validators.maxLength(200)]],
      address: [''],
      prizes: [''],

      emailGroup: this.fb.group({
        email: ['', [Validators.required, Validators.email], [this.emailValidator.bind(this)]],
        confirmEmail: ['', [Validators.required]]
      }, { validators: this.matchValidator('email', 'confirmEmail') }),

      passGroup: this.fb.group({
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required]]
      }, { validators: this.matchValidator('password', 'confirmPassword') })
    });
  }

  usernameValidator(control: AbstractControl) {
    if (!this.editMode) return of(null);
    return this.authService.checkUsernameAvailability(control.value).pipe(
      debounceTime(500),
      map(isAvailable => (isAvailable ? null : { alreadyExists: true })),
      first()
    );
  }

  emailValidator(control: AbstractControl) {
    if (!this.editMode) return of(null);
    return this.authService.checkEmailAvailability(control.value).pipe(
      debounceTime(500),
      map(isAvailable => (isAvailable ? null : { alreadyExists: true })),
      first()
    );
  }

  matchValidator(controlName: string, matchingControlName: string) {
    return (group: AbstractControl): ValidationErrors | null => {
      const control = group.get(controlName);
      const matchingControl = group.get(matchingControlName);
      return (control && matchingControl && control.value !== matchingControl.value) ? { mismatch: true } : null;
    };
  }

  toggleEdit() {
    this.editMode = !this.editMode;
  }

  saveChanges() {
    const currentUser = this.authService.currentUserValue;
    if (!currentUser || currentUser.user_ID === undefined) return;

    if (this.profileForm.valid) {
      const formVal = this.profileForm.value;
      let updatedUser: any = {
        ...currentUser,
        name: formVal.name,
        lastname: formVal.lastname,
        userName: formVal.username,
        photoUrl: formVal.photo,
        email: formVal.emailGroup.email,
        password: formVal.passGroup.password
      };

      if (this.role === 'CHEF') {
        updatedUser.bio = formVal.bio;
        updatedUser.prizes = this.prizesTags.join(', ');
      } else if (this.role === 'DINER') {
        updatedUser.address = formVal.address;
      }

      this.authService.updateUser(updatedUser);
      this.editMode = false;
      alert('Perfil actualizado con éxito');
    }
  }

  // count delete account action
  deleteAccount() {
    const confirmacion = confirm('¿Estás seguro de que deseas eliminar tu cuenta? Esta acción es irreversible.');
    if (confirmacion) {
      console.log('Eliminando cuenta del usuario ID:', this.authService.currentUserValue?.user_ID);
      alert('Cuenta eliminada. Redirigiendo...');
      this.authService.logout();
    }
  }

  // tags logic for prizes
  addPrizeTag(event: any): void {
    event.preventDefault();
    const value = this.currentPrizeInput.trim();
    if (value && !this.prizesTags.includes(value)) {
      this.prizesTags.push(value);
      this.profileForm.get('prizes')?.setValue(this.prizesTags.join(', '));
    }
    this.currentPrizeInput = '';
  }

  removePrizeTag(index: number): void {
    this.prizesTags.splice(index, 1);
    this.profileForm.get('prizes')?.setValue(this.prizesTags.join(', '));
  }
}
