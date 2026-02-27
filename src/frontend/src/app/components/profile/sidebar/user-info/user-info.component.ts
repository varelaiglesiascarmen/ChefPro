import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { debounceTime, map, first, catchError } from 'rxjs/operators';
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
  private cdr = inject(ChangeDetectorRef);

  profileForm!: FormGroup;
  editMode = false;
  role: 'ADMIN' | 'CHEF' | 'DINER' | null = null;
  showSuccessToast = false;
  showErrorToast = false;
  toastMessage = '';
  chefCoverPhotoUrl = '';
  isUploadingCover = false;

  isSaving = false;
  prizesTags: string[] = [];
  currentPrizeInput: string = '';
  private deleteConfirmTimeout?: ReturnType<typeof setTimeout>;
  private isDeletePending = false;

  get profilePhotoUrl(): string {
    const photoValue = this.profileForm?.get('photo')?.value;
    return photoValue && photoValue.trim() !== '' ? photoValue : '/logos/users.svg';
  }

  handleImageError(event: any) {
    event.target.src = '/logos/users.svg';
  }

  handleCoverImageError(): void {
    this.chefCoverPhotoUrl = '';
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      if (!file.type.startsWith('image/')) {
        this.showErrorNotification('Por favor, selecciona un archivo de imagen válido.');
        return;
      }

      if (file.size > 2 * 1024 * 1024) {
        this.showErrorNotification('La imagen es demasiado grande. El tamaño máximo es 2MB.');
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

        const base64Image = canvas.toDataURL('image/jpeg', 0.8);

        this.profileForm.patchValue({ photo: base64Image });
        this.cdr.detectChanges();
      };
      img.src = e.target.result;
    };
    reader.readAsDataURL(file);
  }

  removePhoto(): void {
    this.profileForm.patchValue({ photo: '' });
  }

  onCoverPhotoSelected(event: any): void {
    const file: File = event.target.files?.[0];
    if (!file) {
      return;
    }

    if (!file.type.startsWith('image/')) {
      this.showErrorNotification('Por favor, selecciona un archivo de imagen válido para la portada.');
      event.target.value = '';
      return;
    }

    if (file.size > 4 * 1024 * 1024) {
      this.showErrorNotification('La portada es demasiado grande. El tamaño máximo es 4MB.');
      event.target.value = '';
      return;
    }

    this.isUploadingCover = true;
    this.chefService.uploadChefCoverPhoto(file).subscribe({
      next: (response) => {
        this.chefCoverPhotoUrl = response.coverPhoto || '';
        this.isUploadingCover = false;
        event.target.value = '';
        this.showSuccessNotification('Foto de portada actualizada con éxito.');
      },
      error: (error) => {
        console.error('Error uploading cover photo:', error);
        this.isUploadingCover = false;
        event.target.value = '';
        this.showErrorNotification('No se pudo actualizar la portada. Inténtalo de nuevo.');
      }
    });
  }

  private loadChefCoverPhoto(chefId: number): void {
    this.chefService.getChefPublicProfile(chefId).subscribe({
      next: (chef) => {
        this.chefCoverPhotoUrl = chef.coverPhoto || '';
      },
      error: (error) => {
        console.error('Error loading chef cover photo:', error);
        this.chefCoverPhotoUrl = '';
      }
    });
  }

  urlValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value || value.trim() === '') {
      return null;
    }
    const urlPattern = /^(https?:\/\/)?(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)$/;
    return urlPattern.test(value) ? null : { invalidUrl: true };
  }

  ngOnInit() {
    this.initForm();

    this.authService.user$.subscribe(user => {
      if (user) {
        this.role = user.role;

        // Skip form patching during edit mode to avoid overwriting user edits
        // and re-triggering async validators
        if (this.editMode) return;

        const patchData: any = {
          name: user.name,
          lastname: user.lastname,
          username: user.userName,
          photo: user.photoUrl
        };

        if (user.role === 'CHEF') {
          const chef = user as Chef;
          patchData.bio = chef.bio || '';
          patchData.address = chef.address || '';
          if (chef.prizes) {
            this.prizesTags = chef.prizes.split(',').map(p => p.trim()).filter(p => p !== '');
          }

          if (user.user_ID !== undefined) {
            this.loadChefCoverPhoto(user.user_ID);
          }
        } else if (user.role === 'DINER') {
          const diner = user as Diner;
          patchData.address = diner.address || '';
        }

        this.profileForm.patchValue(patchData, { emitEvent: false });
        this.profileForm.get('emailGroup.email')?.setValue(user.email, { emitEvent: false });
        this.profileForm.get('emailGroup.confirmEmail')?.setValue(user.email, { emitEvent: false });
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
    if (currentUser && control.value === currentUser.userName) {
      return of(null);
    }

    if (!control.value || control.value.trim() === '') {
      return of(null);
    }

    return this.authService.checkUsernameAvailability(control.value).pipe(
      debounceTime(500),
      map(isAvailable => (isAvailable ? null : { alreadyExists: true })),
      catchError(() => of(null)),
      first()
    );
  }

  emailValidator(control: AbstractControl) {
    if (!this.editMode) return of(null);

    const currentUser = this.authService.currentUserValue;
    if (currentUser && control.value === currentUser.email) {
      return of(null);
    }

    if (!control.value || control.value.trim() === '') {
      return of(null);
    }

    return this.authService.checkEmailAvailability(control.value).pipe(
      debounceTime(500),
      map(isAvailable => (isAvailable ? null : { alreadyExists: true })),
      catchError(() => of(null)),
      first()
    );
  }

  matchValidator(controlName: string, matchingControlName: string) {
    return (group: AbstractControl): ValidationErrors | null => {
      const control = group.get(controlName);
      const matchingControl = group.get(matchingControlName);

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

    // Block if form has actual validation errors (not just PENDING async validators)
    if (this.profileForm.invalid) {
      this.showErrorNotification('Por favor, corrige los errores en el formulario antes de guardar.');
      return;
    }

    this.isSaving = true;
    const formVal = this.profileForm.value;
    let updatedUser: any = {
      ...currentUser,
      name: formVal.name,
      lastname: formVal.lastname,
      userName: formVal.username,
      photoUrl: formVal.photo !== undefined && formVal.photo !== null ? formVal.photo : currentUser.photoUrl
    };

    if (this.role === 'CHEF') {
      updatedUser.bio = formVal.bio;
      updatedUser.prizes = this.prizesTags.join(', ');
      updatedUser.address = formVal.address;
    } else if (this.role === 'DINER') {
      updatedUser.address = formVal.address;
    }

    this.authService.updateUser(updatedUser).subscribe({
      next: (user) => {
        const patchData: any = {
          name: user.name,
          lastname: user.lastname,
          username: user.userName,
          photo: user.photoUrl
        };

        if (user.role === 'CHEF') {
          const chef = user as Chef;
          patchData.bio = chef.bio || '';
          patchData.address = chef.address || '';
          if (chef.prizes) {
            this.prizesTags = chef.prizes.split(',').map(p => p.trim()).filter(p => p !== '');
          } else {
            this.prizesTags = [];
          }
        } else if (user.role === 'DINER') {
          const diner = user as Diner;
          patchData.address = diner.address || '';
        }

        // Use emitEvent: false to avoid re-triggering async validators
        this.profileForm.patchValue(patchData, { emitEvent: false });
        this.profileForm.get('emailGroup.email')?.setValue(user.email, { emitEvent: false });
        this.profileForm.get('emailGroup.confirmEmail')?.setValue(user.email, { emitEvent: false });
        this.profileForm.get('passGroup.password')?.setValue('', { emitEvent: false });
        this.profileForm.get('passGroup.confirmPassword')?.setValue('', { emitEvent: false });

        this.isSaving = false;
        this.editMode = false;
        this.showSuccessNotification('Perfil actualizado con éxito');
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error updating profile:', error);
        this.isSaving = false;
        this.showErrorNotification('Error al actualizar el perfil. Por favor, intenta de nuevo.');
      }
    });
  }

  showSuccessNotification(message: string) {
    this.toastMessage = message;
    this.showSuccessToast = true;
    this.cdr.detectChanges();
    setTimeout(() => {
      this.showSuccessToast = false;
      this.cdr.detectChanges();
    }, 3000);
  }

  showErrorNotification(message: string) {
    this.toastMessage = message;
    this.showErrorToast = true;
    this.cdr.detectChanges();
    setTimeout(() => {
      this.showErrorToast = false;
      this.cdr.detectChanges();
    }, 3000);
  }

  deleteAccount() {
    if (!this.isDeletePending) {
      this.isDeletePending = true;
      this.showErrorNotification('Pulsa de nuevo en eliminar cuenta para confirmar. Esta acción es irreversible.');
      this.deleteConfirmTimeout = setTimeout(() => {
        this.isDeletePending = false;
      }, 5000);
      return;
    }

    if (this.deleteConfirmTimeout) {
      clearTimeout(this.deleteConfirmTimeout);
      this.deleteConfirmTimeout = undefined;
    }
    this.isDeletePending = false;
    this.showSuccessNotification('Cuenta eliminada. Redirigiendo...');
    this.authService.logout();
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
