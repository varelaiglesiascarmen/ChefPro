import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-about',
  imports: [CommonModule],
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css']
})
export class AboutComponent {
  /**
   * Team member collection displayed in the about page.
   * Contains professional information including roles, descriptions, and social links.
   * Used for rendering team cards with Angular *ngFor directive.
   */
  team = [
    {
      name: 'Carmen Varela Iglesias',
      role: 'Frontend & UX Engineer con responsabilidades Full-Stack',
      desc: 'Responsable del diseño y desarrollo de las interfaces y la experiencia de usuario utilizando Angular Standalone, asegurando una aplicación intuitiva y eficiente. Además, diseñó y gestionó la base de datos en SQL, se encargó del despliegue del proyecto con Docker Compose  y colaboró activamente con el desarrollo backend, apoyando la integración entre frontend, APIs y base de datos.',
      photo: 'team/carmen-varela-team.png',
      github: 'https://github.com/varelaiglesiascarmen',
      linkedin: 'https://www.linkedin.com/in/carmen-varela-iglesias/'
    },
    {
      name: 'María Reyes Artacho Carrero',
      role: 'Backend Developer & API Engineer',
      desc: 'Responsable del desarrollo del backend con Spring Boot, implementando la lógica de negocio y creando APIs propias definiendo los contratos en OpenAPI para la comunicación con el frontend. Gestionó el acceso y consumo de la base de datos SQL con Spring Data, asegurando la integridad de los datos, el rendimiento y la escalabilidad del sistema mediante JWT.',
      photo: 'team/reyes-team.png',
      github: 'https://github.com/reyes-art-car',
      linkedin: 'https://www.linkedin.com/in/maria-reyes-a-carrero/'
    }
  ];

}
