Perfecto, he analizado completamente el proyecto. Aquí está el estado actual del Hito 2:

## Estado del Hito 2 (Entrega HOY 27/02/2026)

### COMPLETADO (85-90%)

| Requisito | Estado | Archivos |
|-----------|--------|----------|
| Sistema de Valoraciones | 100% | review-form, user-orders |
| Dashboard Chef (Reservas) | 100% | user-orders (aceptar/rechazar/cancelar) |
| Calendario Chef | 100% | user-calendar |
| UI/UX Responsive | 100% | Media queries en todos los componentes |
| Spinners de carga | 100% | service-detail, search-results, sign-in |
| Limpieza console.log | 100% | No quedan console.log/debug |
| Sin alert() nativos | 100% | ToastService implementado (P0) |
| Gestión de errores HTTP | 100% | error.interceptor global |

### PENDIENTE (10-15%)

1. CRITICO - Fotos de Perfil NO usan backend
   - Backend tiene uploadChefPhoto() y uploadChefCoverPhoto() OK
   - Frontend [user-info.component.ts](src/frontend/src/app/components/profile/sidebar/user-info/user-info.component.ts) no los usa
   - Problema: Convierte localmente y guarda en users.photo via authService
   - Deberia: Usar chefService.uploadChefPhoto() -> backend PhotoUploadService

2. MEDIO - Cover Photo NO tiene UI
   - Backend soporta cover photo OK
   - Frontend no tiene input para subirla
   - Solo se visualiza si ya existe

3. MEDIO - Fotos de Platos
   - Frontend envia Base64 en createDish/updateDish (completado en P1)
   - Backend necesita columna photo en tabla dishes (tu companera trabajando)

---

## Plan de Accion URGENTE (para HOY)

### Prioridad 1: Integrar uploadChefPhoto en user-info

Problema: user-info hace resize local y no usa el endpoint backend dedicado.

Solucion: Modificar [user-info.component.ts](src/frontend/src/app/components/profile/sidebar/user-info/user-info.component.ts) para:
1. Usar chefService.uploadChefPhoto(file) en lugar de resizeAndConvertImage()
2. Permitir que backend maneje la foto (max 5MB)
3. Actualizar el profileForm con la URL devuelta

Tiempo estimado: 15-20 minutos

---

### Prioridad 2: Agregar UI para Cover Photo

Problema: Backend soporta cover photo pero frontend no tiene input.

Solucion: Agregar en user-info:
1. Otro input[type="file"] para cover photo
2. Preview de cover photo actual
3. Boton para cambiarla usando chefService.uploadChefCoverPhoto(file)

Tiempo estimado: 20-30 minutos

---

## Empezamos

Tenemos dos tareas rapidas para completar el Hito 2 al 100%:

1. Conectar user-info con uploadChefPhoto (critico - 15 min)
2. Agregar UI para cover photo (recomendado - 25 min)

Creo la rama de trabajo y empezamos con la Prioridad 1.
