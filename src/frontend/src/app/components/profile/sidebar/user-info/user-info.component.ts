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

  handleImageError(event: any) {
    event.target.src = '/logos/users.svg';
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      // Verificar que sea una imagen
      if (!file.type.startsWith('image/')) {
        alert('Por favor, selecciona un archivo de imagen válido.');
        return;
      }

      // Verificar tamaño máximo (2MB)
      if (file.size > 2 * 1024 * 1024) {
        alert('La imagen es demasiado grande. El tamaño máximo es 2MB.');
        return;
      }

      this.resizeAndConvertImage(file);
    }
  }

  resizeAndConvertImage(file: File): void {
    const reader = new FileReader();
    reader.onload = (e: any) => {
      const img = new Image();
      img.onload = () => {
        // Crear canvas para redimensionar
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');

        // Dimensiones del avatar (200x200px)
        const MAX_WIDTH = 200;
        const MAX_HEIGHT = 200;

        let width = img.width;
        let height = img.height;

        // Calcular proporciones manteniendo aspect ratio
        if (width > height) {
          if (width > MAX_WIDTH) {
            height = (height * MAX_WIDTH) / width;
            width = MAX_WIDTH;
          }
        } else {
          if (height > MAX_HEIGHT) {
            width = (width * MAX_HEIGHT) / height;
            height = MAX_HEIGHT;
          }
        }

        canvas.width = width;
        canvas.height = height;

        // Dibujar imagen redimensionada
        ctx?.drawImage(img, 0, 0, width, height);

        // Convertir a Base64 JPEG con compresión
        const base64Image = canvas.toDataURL('image/jpeg', 0.8);

        // Actualizar el formulario con la imagen en Base64
        this.profileForm.patchValue({ photo: base64Image });

        console.log('Imagen redimensionada y convertida. Tamaño aproximado:',
          Math.round(base64Image.length * 0.75 / 1024), 'KB');
      };
      img.src = e.target.result;
    };
    reader.readAsDataURL(file);
  }

  removePhoto(): void {
    this.profileForm.patchValue({ photo: '' });
  }

  urlValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value || value.trim() === '') {
      return null; // Permitir valores vacíos
    }
    const urlPattern = /^(https?:\/\/)?(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)$/;
    return urlPattern.test(value) ? null : { invalidUrl: true };
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
        email: ['', [Validators.email], [this.emailValidator.bind(this)]],
        confirmEmail: ['']
      }, { validators: this.matchValidator('email', 'confirmEmail') }),

      passGroup: this.fb.group({
        password: ['', [Validators.minLength(6)]],
        confirmPassword: ['']
      }, { validators: this.matchValidator('password', 'confirmPassword') })
    });
  }

  usernameValidator(control: AbstractControl) {
    if (!this.editMode) return of(null);

    const currentUser = this.authService.currentUserValue;
    // Si el username es igual al actual, no validar
    if (currentUser && control.value === currentUser.userName) {
      return of(null);
    }

    // Si el campo está vacío, no validar
    if (!control.value || control.value.trim() === '') {
      return of(null);
    }

    return this.authService.checkUsernameAvailability(control.value).pipe(
      debounceTime(500),
      map(isAvailable => (isAvailable ? null : { alreadyExists: true })),
      first()
    );
  }

  emailValidator(control: AbstractControl) {
    if (!this.editMode) return of(null);

    const currentUser = this.authService.currentUserValue;
    // Si el email es igual al actual, no validar
    if (currentUser && control.value === currentUser.email) {
      return of(null);
    }

    // Si el campo está vacío, no validar
    if (!control.value || control.value.trim() === '') {
      return of(null);
    }

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

      // Solo validar si ambos campos tienen valor
      if (!control?.value || !matchingControl?.value) {
        return null;
      }

      return control.value !== matchingControl.value ? { mismatch: true } : null;
    };
  }

  toggleEdit() {
    this.editMode = !this.editMode;
  }

  saveChanges() {
    const currentUser = this.authService.currentUserValue;
    if (!currentUser || currentUser.user_ID === undefined) return;

    console.log('Form valid:', this.profileForm.valid);
    console.log('Form errors:', this.profileForm.errors);
    console.log('Form value:', this.profileForm.value);

    if (this.profileForm.valid) {
      const formVal = this.profileForm.value;
      let updatedUser: any = {
        ...currentUser,
        name: formVal.name,
        lastname: formVal.lastname,
        userName: formVal.username,
        photoUrl: formVal.photo || currentUser.photoUrl
      };

      if (this.role === 'CHEF') {
        updatedUser.bio = formVal.bio;
        updatedUser.prizes = this.prizesTags.join(', ');
        updatedUser.address = formVal.address;
      } else if (this.role === 'DINER') {
        updatedUser.address = formVal.address;
      }

      console.log('Updating user:', updatedUser);

      this.authService.updateUser(updatedUser).subscribe({
        next: (user) => {
          this.editMode = false;
          alert('Perfil actualizado con éxito');
        },
        error: (error) => {
          console.error('Error updating profile:', error);
          alert('Error al actualizar el perfil. Por favor, intenta de nuevo.');
        }
      });
    } else {
      console.log('Form is invalid');
      Object.keys(this.profileForm.controls).forEach(key => {
        const control = this.profileForm.get(key);
        if (control?.invalid) {
          console.log(`Invalid control: ${key}`, control.errors);
        }
      });
      alert('Por favor, corrige los errores en el formulario antes de guardar.');
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
