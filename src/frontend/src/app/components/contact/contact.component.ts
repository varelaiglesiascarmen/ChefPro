import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import emailjs from '@emailjs/browser';

@Component({
  selector: 'app-contact',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './contact.component.html',
  styleUrls: ['./contact.component.css']
})
export class ContactComponent {
  contactForm: FormGroup;
  feedbackMessage: string | null = null;
  isError: boolean = false;
  isSubmitting = false;

  private fb = inject(FormBuilder);

  /**
   * We have used the EmailJS library to utilize its email service as an API,
   * since mailto is unprofessional and poses a security risk.
   */
  constructor() {
    this.contactForm = this.fb.group({
      name: ['', [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(50),
        Validators.pattern(/^[a-zA-ZÀ-ÿ\u00f1\u00d1\s]+$/)
      ]],
      email: ['', [
        Validators.required,
        Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/)
      ]],
      subject: ['', [
        Validators.required,
        Validators.minLength(5),
        Validators.maxLength(100),
        Validators.pattern(/.*[\S]+.*/)
      ]],
      message: ['', [
        Validators.required,
        Validators.minLength(10),
        Validators.maxLength(1000),
        Validators.pattern(/.*[\S]+.*/)
      ]]
    });
  }

  public async onSubmit() {
    if (this.contactForm.invalid) {
      this.contactForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.isError = false;

    try {
      const serviceID = 'service_ofm18f4';
      const templateID = 'template_c1fx72m';
      const publicKey = 'B7KS6zMv7XBfBk0lW';

      const templateParams = {
        from_name: this.contactForm.value.name.trim(),
        reply_to: this.contactForm.value.email.trim(),
        subject: this.contactForm.value.subject.trim(),
        message: this.contactForm.value.message.trim(),
      };

      await emailjs.send(serviceID, templateID, templateParams, {
        publicKey: publicKey,
      });

      this.isError = false;
      this.feedbackMessage = 'Mensaje enviado con éxito. Nuestro equipo se pondrá en contacto pronto.';
      this.contactForm.reset();

    } catch (error) {
      console.error('Fallo en el servicio de correo:', error);
      this.isError = true;
      this.feedbackMessage = 'Hubo un problema de conexión. Por favor, inténtalo más tarde.';
    } finally {
      this.isSubmitting = false;
      this.clearToast();
    }
  }

  private clearToast() {
    setTimeout(() => {
      this.feedbackMessage = null;
    }, 5000);
  }
}
