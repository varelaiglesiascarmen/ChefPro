Estado actualizado del Hito 2 con validaciones reales en entorno Docker.

## Estado del Hito 2 (actualizado)

### COMPLETADO (90-95%)

| Requisito | Estado | Nota |
|-----------|--------|------|
| Sistema de Valoraciones | 100% | Flujo funcional en frontend/backend |
| Dashboard Chef (Reservas) | 100% | Ver/aceptar/rechazar/cancelar |
| Calendario Chef | 100% | Integrado con reservas |
| UI/UX Responsive | 100% | Breakpoints y layouts revisados |
| Spinners y feedback | 100% | Toasts + estados de carga |
| Limpieza P0 | 100% | Sin `console.log/debug` ni `alert/confirm` nativos |
| Gestión errores HTTP | 100% | interceptor global |
| Foto de perfil (ambos roles) | 100% | Validado con flujo `auth/profile` |

### PENDIENTE

1. MEDIO - Cover Photo sin UI en perfil
   - Backend soporta subida de cover photo.
   - Frontend aún no tiene input/preview para gestionarla.

2. MEDIO - Fotos de platos dependen de backend
   - Frontend ya envía Base64 en alta/edición.
   - Falta cerrar backend/BD (`dishes.photo`) por parte de backend.

3. BAJO - Ajustes finales de hardening
   - Verificación final de consola limpia en casos límite.
   - Validación end-to-end final antes de cierre de Hito 2.

---

## Cambio de decisión técnica (importante)

Inicialmente se planteó mover la foto de perfil a `/api/chef/profile/photo`.

Tras pruebas reales, se confirmó que ese endpoint está protegido por `ROLE_CHEF`, lo que provoca `403` para otros roles. Como la foto de perfil debe funcionar para todos los usuarios, se mantiene el flujo común:

- Frontend: selección + resize local (200x200, máx 2MB)
- Persistencia: `PUT /api/auth/profile`
- Resultado: funcional para CHEF y DINER

Esta decisión mantiene compatibilidad con el modelo actual y evita regresiones de permisos.

---

## Plan de actuación (siguiente bloque)

### Prioridad 1: UI de Cover Photo en perfil

Implementar en `user-info`:
1. Input de subida de cover photo.
2. Vista previa de cover actual.
3. Botón cambiar/eliminar cover.
4. Feedback visual (loading + toast de éxito/error).

### Prioridad 2: Cierre integración fotos de platos

Una vez backend confirme columna/endpoint de platos:
1. Validar guardado real en BD.
2. Verificar renderizado en resultados/detalle.
3. Prueba E2E de alta y edición con imagen.

---

## Resumen ejecutivo

- La foto de perfil ya funciona para ambos roles y queda cerrada.
- El siguiente trabajo es Cover Photo UI + cierre de fotos de platos con backend.
- El proyecto está en fase final para completar Hito 2.
