import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-privacy-policy',
  imports: [CommonModule, RouterLink],
  templateUrl: './privacy-policy.component.html',
  styleUrls: ['./privacy-policy.component.css']
})
export class PrivacyPolicyComponent {

  dataCollected = [
    {
      icon: 'fa-solid fa-address-card',
      title: 'Datos de Identidad',
      desc: 'Nombre, apellidos y datos necesarios para verificar tu perfil en la plataforma.'
    },
    {
      icon: 'fa-solid fa-envelope',
      title: 'Datos de Contacto',
      desc: 'Email, teléfono y dirección de servicio para la coordinación de las experiencias.'
    },
    {
      icon: 'fa-solid fa-credit-card',
      title: 'Información Financiera',
      desc: 'Datos de pago procesados de forma segura a través de pasarelas encriptadas (Stripe/PayPal).'
    },
    {
      icon: 'fa-solid fa-cookie-bite',
      title: 'Datos Técnicos',
      desc: 'Dirección IP, datos de navegación y preferencias para mejorar tu experiencia de usuario.'
    },
    {
      icon: 'fa-solid fa-location-dot',
      title: 'Datos de Localización',
      desc: 'Ubicación geográfica aproximada para sugerir chefs cercanos y optimizar las rutas de desplazamiento.'
    },
    {
      icon: 'fa-solid fa-wheat-awn-circle-exclamation',
      title: 'Perfil Dietético',
      desc: 'Alergias, intolerancias y restricciones alimentarias compartidas exclusivamente para garantizar tu salud.'
    }
  ];

  userRights = [
    { title: 'Acceso', desc: 'Derecho a solicitar una copia de tus datos personales.' },
    { title: 'Rectificación', desc: 'Derecho a corregir datos inexactos o incompletos.' },
    { title: 'Supresión', desc: 'Derecho al "olvido" y borrado de tus datos en nuestros sistemas.' },
    { title: 'Portabilidad', desc: 'Derecho a recibir tus datos en un formato estructurado.' }
  ];
}
