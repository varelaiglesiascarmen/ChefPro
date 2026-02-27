import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-cancellation-policies',
  imports: [CommonModule],
  templateUrl: './cancellation-policies.component.html',
  styleUrls: ['./cancellation-policies.component.css']
})
export class CancellationPoliciesComponent {
  /**
   * Policy rules structure defines cancellation charges and conditions across three tiers:
   * administrative management, critical notice period, and professional commitment protection.
   * Protects both diners and chefs while maintaining platform transparency.
   */
  policyRules = [
    {
      type: 'Gestión Administrativa',
      detail: 'Toda cancelación solicitada por el comensal conlleva un recargo por gastos operativos.',
      cost: '10% de retención del importe total'
    },
    {
      type: 'Plazo de Preaviso Crítico',
      detail: 'Cancelaciones realizadas con menos de 72 horas de antelación al inicio del servicio contratado.',
      cost: '100% de retención (Sin reembolso)'
    },
    {
      type: 'Compromiso del Profesional',
      detail: 'En caso de que el chef no confirme la solicitud 72 horas antes del evento, el sistema protege al usuario.',
      cost: 'Cancelación automática y reembolso íntegro'
    }
  ];
}
