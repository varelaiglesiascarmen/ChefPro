import { Component, OnInit, inject } from '@angular/core';
import { AuthService } from '../../../../services/auth.service';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators, FormsModule } from '@angular/forms';
import { debounceTime, switchMap, map, first } from 'rxjs/operators';
import { of } from 'rxjs';

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
  role: string | null = null;

  // Lógica de Tags para Premios
  prizesTags: string[] = [];
  currentPrizeInput: string = '';

  ngOnInit() {
    this.initForm();

    this.authService.user$.subscribe(user => {
      if (user) {
        this.role = user.role;

        // Rellenamos el formulario básico
        this.profileForm.patchValue({
          name: user.name,
          lastname: user.lastname,
          username: user.userName, // Ajustado al modelo de tu AuthService
          bio: user.bio,
          photo: user.photoUrl
        });

        // Cargamos los premios en el array de tags
        if (user.prizes) {
          this.prizesTags = user.prizes.split(',').map((p: string) => p.trim()).filter((p: string) => p !== '');
        }

        // Sincronizamos los grupos de validación doble
        this.profileForm.get('emailGroup.email')?.setValue(user.email);
        this.profileForm.get('emailGroup.confirmEmail')?.setValue(user.email);
      }
    });
  }

  private initForm() {
    this.profileForm = this.fb.group({
      name: [''],
      lastname: [''],
      username: ['', [Validators.required], [this.usernameValidator.bind(this)]],
      bio: ['', [Validators.maxLength(200)]], // Límite de 200 caracteres
      photo: [''],
      prizes: [''], // Se sincroniza con los tags

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

  // --- LÓGICA DE TAGS (REUTILIZADA) ---
  addPrizeTag(event: any): void {
    event.preventDefault();
    const value = this.currentPrizeInput.trim();
    if (value && !this.prizesTags.includes(value)) {
      this.prizesTags.push(value);
      this.syncPrizesToForm();
    }
    this.currentPrizeInput = '';
  }

  removePrizeTag(index: number): void {
    this.prizesTags.splice(index, 1);
    this.syncPrizesToForm();
  }

  private syncPrizesToForm() {
    this.profileForm.get('prizes')?.setValue(this.prizesTags.join(', '));
  }

  // --- VALIDACIONES ---
  matchValidator(controlName: string, matchingControlName: string) {
    return (group: AbstractControl): ValidationErrors | null => {
      const control = group.get(controlName);
      const matchingControl = group.get(matchingControlName);
      return (control && matchingControl && control.value !== matchingControl.value)
        ? { mismatch: true } : null;
    };
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

  // --- ACCIONES ---
  toggleEdit() {
    this.editMode = !this.editMode;
    if (!this.editMode) this.profileForm.markAsPristine();
  }

  saveChanges() {
    if (this.profileForm.valid) {
      const formVal = this.profileForm.value;
      const updatedUser = {
        ...this.authService.currentUserValue, // Mantenemos IDs y campos no editados
        name: formVal.name,
        lastname: formVal.lastname,
        userName: formVal.username,
        bio: formVal.bio,
        prizes: this.prizesTags.join(', '),
        photoUrl: formVal.photo,
        email: formVal.emailGroup.email,
        password: formVal.passGroup.password
      };

      this.authService.updateUser(updatedUser);
      this.editMode = false;
      alert('Perfil actualizado con éxito');
    }
  }
}
