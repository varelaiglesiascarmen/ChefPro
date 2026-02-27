import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { debounceTime, map, first, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthService } from '../../../../services/auth.service';
import { ChefService } from '../../../../services/chef.service';
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
  private chefService = inject(ChefService);

  profileForm!: FormGroup;
  editMode = false;
  role: 'ADMIN' | 'CHEF' | 'DINER' | null = null;
  showSuccessToast = false;
  showErrorToast = false;
  toastMessage = '';

  prizesTags: string[] = [];
  currentPrizeInput: string = '';

  // Fichero seleccionado pendiente de subir — se envía al backend en saveChanges()
  private selectedPhotoFile: File | null = null;

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
      if (!file.type.startsWith('image/')) {
        alert('Por favor, selecciona un archivo de imagen válido.');
        return;
      }
      if (file.size > 2 * 1024 * 1024) {
        alert('La imagen es demasiado grande. El tamaño máximo es 2MB.');
        return;
      }

      // Guardamos el fichero original para subirlo al backend al guardar
      this.selectedPhotoFile = file;

      // Previsualización local con canvas (no cambia el flujo del backend)
      this.resizeAndConvertImage(file);
    }
  }

  resizeAndConvertImage(file: File): void {
    const reader = new FileReader();
    reader.onload = (e: any) => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');

        const MAX_WIDTH = 200;
        const MAX_HEIGHT = 200;

        let width = img.width;
        let height = img.height;

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
        ctx?.drawImage(img, 0, 0, width, height);

        // Solo para previsualización local en el formulario
        const base64Preview = canvas.toDataURL('image/jpeg', 0.8);
        this.profileForm.patchValue({ photo: base64Preview });
      };
      img.src = e.target.result;
    };
    reader.readAsDataURL(file);
  }

  removePhoto(): void {
    this.profileForm.patchValue({ photo: '' });
    this.selectedPhotoFile = null;
  }

  urlValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value || value.trim() === '') return null;
    const urlPattern = /^(https?:\/\/)?(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)$/;
    return urlPattern.test(value) ? null : { invalidUrl: true };
  }

  ngOnInit() {
    this.initForm();

    this.authService.user$.subscribe(user => {
      if (user) {
        this.role = user.role;

        const patchData: any = {
          name: user.name,
          lastname: user.lastname,
          username: user.userName,
          photo: user.photoUrl
        };

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
    if (currentUser && control.value === currentUser.userName) return of(null);
    if (!control.value || control.value.trim() === '') return of(null);
    return this.authService.checkUsernameAvailability(control.value).pipe(
      debounceTime(500),
      map(isAvailable => (isAvailable ? null : { alreadyExists: true })),
      first()
    );
  }

  emailValidator(control: AbstractControl) {
    if (!this.editMode) return of(null);
    const currentUser = this.authService.currentUserValue;
    if (currentUser && control.value === currentUser.email) return of(null);
    if (!control.value || control.value.trim() === '') return of(null);
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
      if (!control?.value || !matchingControl?.value) return null;
      return control.value !== matchingControl.value ? { mismatch: true } : null;
    };
  }

  toggleEdit() {
    this.editMode = !this.editMode;
  }

  saveChanges() {
    const currentUser = this.authService.currentUserValue;
    if (!currentUser || currentUser.user_ID === undefined) return;

    if (!this.profileForm.valid) {
      this.showErrorNotification('Por favor, corrige los errores en el formulario antes de guardar.');
      return;
    }

    const formVal = this.profileForm.value;

    // Si el chef ha seleccionado una foto nueva, subirla primero al backend
    // y luego guardar el resto del perfil con la URL devuelta
    if (this.role === 'CHEF' && this.selectedPhotoFile) {
      this.chefService.uploadChefPhoto(this.selectedPhotoFile).pipe(
        switchMap(response => {
          // La foto ya está guardada en BD — actualizamos el form con la URL real
          this.profileForm.patchValue({ photo: response.photo });
          this.selectedPhotoFile = null;
          // Continuar con el guardado del resto del perfil
          return this.buildAndSaveProfile(formVal, response.photo);
        })
      ).subscribe({
        next: (user) => this.handleSaveSuccess(user),
        error: (err) => {
          console.error('Error subiendo foto:', err);
          this.showErrorNotification('Error al subir la foto. Por favor, intenta de nuevo.');
        }
      });
    } else {
      // Sin foto nueva — guardar directamente el resto del perfil
      this.buildAndSaveProfile(formVal, formVal.photo || currentUser.photoUrl).subscribe({
        next: (user) => this.handleSaveSuccess(user),
        error: (err) => {
          console.error('Error updating profile:', err);
          this.showErrorNotification('Error al actualizar el perfil. Por favor, intenta de nuevo.');
        }
      });
    }
  }

  private buildAndSaveProfile(formVal: any, photoUrl: string) {
    const currentUser = this.authService.currentUserValue!;

    let updatedUser: any = {
      ...currentUser,
      name: formVal.name,
      lastname: formVal.lastname,
      userName: formVal.username,
      photoUrl: photoUrl
    };

    if (this.role === 'CHEF') {
      updatedUser.bio = formVal.bio;
      updatedUser.prizes = this.prizesTags.join(', ');
      updatedUser.address = formVal.address;
    } else if (this.role === 'DINER') {
      updatedUser.address = formVal.address;
    }

    return this.authService.updateUser(updatedUser);
  }

  private handleSaveSuccess(user: any) {
    const patchData: any = {
      name: user.name,
      lastname: user.lastname,
      username: user.userName,
      photo: user.photoUrl
    };

    if (user.role === 'CHEF') {
      const chef = user as Chef;
      patchData.bio = chef.bio || '';
      this.prizesTags = chef.prizes
        ? chef.prizes.split(',').map((p: string) => p.trim()).filter((p: string) => p !== '')
        : [];
    } else if (user.role === 'DINER') {
      const diner = user as Diner;
      patchData.address = diner.address || '';
    }

    this.profileForm.patchValue(patchData);
    this.profileForm.get('emailGroup.email')?.setValue(user.email);
    this.profileForm.get('emailGroup.confirmEmail')?.setValue(user.email);
    this.profileForm.get('passGroup.password')?.setValue('');
    this.profileForm.get('passGroup.confirmPassword')?.setValue('');

    setTimeout(() => {
      this.editMode = false;
      this.showSuccessNotification('Perfil actualizado con éxito');
    }, 0);
  }

  showSuccessNotification(message: string) {
    this.toastMessage = message;
    this.showSuccessToast = true;
    setTimeout(() => { this.showSuccessToast = false; }, 3000);
  }

  showErrorNotification(message: string) {
    this.toastMessage = message;
    this.showErrorToast = true;
    setTimeout(() => { this.showErrorToast = false; }, 3000);
  }

  deleteAccount() {
    const confirmacion = confirm('¿Estás seguro de que deseas eliminar tu cuenta? Esta acción es irreversible.');
    if (confirmacion) {
      alert('Cuenta eliminada. Redirigiendo...');
      this.authService.logout();
    }
  }

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
