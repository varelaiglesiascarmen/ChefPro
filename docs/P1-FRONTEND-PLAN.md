# P1 - Frontend Photo Integration Plan

## Estado Actual (después de pull)

### ✅ Implementado en Backend
- `PhotoUploadService` y `PhotoUploadServiceImpl` para chef photo y cover photo
- Endpoints: `POST /api/chef/profile/photo` y `POST /api/chef/profile/cover-photo`
- Acepta `MultipartFile`, devuelve Base64 con data URI
- Validación: max 5MB, solo imágenes

### ✅ Implementado en Frontend Service
- `chef.service.ts` tiene métodos `uploadChefPhoto()` y `uploadChefCoverPhoto()`
- Acepta `File`, construye `FormData`, llama al backend

### ❌ Pendiente en Frontend Components

1. **user-info.component.ts** - Perfil de usuario
   - Actualmente: Redimensiona a 200x200px con Canvas y convierte a Base64 localmente
   - Problema: No usa los nuevos métodos `uploadChefPhoto/uploadChefCoverPhoto`
   - Estado: Funciona pero NO aprovecha validación del backend

2. **edit-menu.component.ts** - Editar menú
   - **BLOQUEANTE**: NO envía fotos al actualizar platos
   - Falta método `fileToBase64()`
   - Falta hacer `saveDishes()` async para manejar Promises
   - Falta agregar campo `photo` al payload del PATCH

3. **new-menu.component.ts** - Crear menú
   - Estado: ✅ Funciona correctamente
   - Convierte fotos a Base64 y las envía en el payload
   - No requiere cambios inmediatos

---

## Plan de Acción P1 - Frontend

### Fase 1: Corregir Edit Menu (PRIORIDAD ALTA) 🔴

**Problema**: Cuando el usuario edita un plato y cambia su foto, la foto NO se envía al backend.

**Archivos afectados**:
- `src/frontend/src/app/components/profile/sidebar/edit-menu/edit-menu.component.ts`

**Tareas**:
1. [ ] Agregar método `fileToBase64()` (copiar de new-menu)
2. [ ] Hacer `saveDishes()` async
3. [ ] Convertir fotos a Base64 antes de enviar
4. [ ] Agregar campo `photo` al payload del updateDish
5. [ ] Agregar campo `photo` al payload del createDish (platos nuevos en menú existente)

**Código requerido**:
```typescript
// 1. Agregar método fileToBase64
private fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result ?? ''));
    reader.onerror = () => reject(new Error('Error al procesar la imagen'));
    reader.readAsDataURL(file);
  });
}

// 2. Actualizar saveDishes para ser async
private async saveDishes(): Promise<void> {
  const requests: Observable<any>[] = [];
  let nextDishId = this.getMaxDishId();

  for (const dish of this.dishes) {
    const allergens = this.mapAllergenIdsToNames(dish.allergenIds);
    
    // Convertir foto si existe
    const photoBase64 = dish.photo ? await this.fileToBase64(dish.photo) : null;

    if (dish.dishId) {
      // Actualizar plato existente
      requests.push(this.menuService.updateDish({
        menuId: this.menuId,
        dishId: dish.dishId,
        title: dish.title.trim(),
        description: dish.description.trim(),
        category: dish.category,
        allergens,
        photo: photoBase64  // ✅ AGREGAR
      }));
    } else {
      // Crear plato nuevo
      nextDishId += 1;
      dish.dishId = nextDishId;
      requests.push(this.menuService.createDish({
        menu_ID: this.menuId,
        dish_ID: dish.dishId,
        title: dish.title.trim(),
        description: dish.description.trim(),
        category: dish.category,
        allergens,
        photo: photoBase64  // ✅ AGREGAR
      }));
    }
  }

  if (requests.length === 0) {
    this.isSaving = false;
    this.router.navigate(['/profile/menus']);
    return;
  }

  forkJoin(requests).pipe(
    takeUntil(this.destroy$)
  ).subscribe({
    next: () => {
      this.isSaving = false;
      this.router.navigate(['/profile/menus']);
    },
    error: (err) => {
      console.error('EditMenu: Error al guardar platos:', err);
      this.isSaving = false;
      this.errorMessage = 'No se pudieron guardar los platos. Intentalo de nuevo.';
      this.cdr.detectChanges();
    }
  });
}
```

---

### Fase 2: Integrar PhotoUploadService en User Info (OPCIONAL - MEJORA) 🟡

**Objetivo**: Aprovechar validación del backend en lugar de hacerla solo en frontend.

**Archivos afectados**:
- `src/frontend/src/app/components/profile/sidebar/user-info/user-info.component.ts`

**Decisión requerida**: 
- ¿Mantener sistema actual (resize en frontend + Base64)?
- ¿O cambiar a usar `uploadChefPhoto()` del backend?

**Trade-offs**:

| Aspecto | Sistema Actual (Frontend) | Nuevo Sistema (Backend) |
|---------|---------------------------|-------------------------|
| Resize | ✅ 200x200px en Canvas | ❌ No hace resize |
| Validación | Frontend only | ✅ Backend + Frontend |
| Tamaño máximo | 2MB | 5MB |
| Request size | Pequeño (~10-20KB) | Grande (~2-5MB) |
| Proceso | Inmediato | Requiere upload |
| Consistencia | Photo en `users.photo` | Photo en `chefs.photo` |

**Estado actual de la tabla**:
```sql
-- users.photo (LONGTEXT) - se usa actualmente
-- chefs.photo (VARCHAR(255)) - NO se usa, pero backend nuevo lo usa
```

**PROBLEMA IDENTIFICADO**: 
- Backend guarda en `chefs.photo` pero frontend espera que esté en `users.photo`
- Hay duplicación: foto puede estar en ambos lados

**RECOMENDACIÓN**: 
1. **Corto plazo**: Mantener sistema actual de user-info (funciona bien)
2. **Medio plazo**: Coordinar con backend para decidir:
   - ¿Foto solo en `users.photo`?
   - ¿O solo en `chefs.photo`?
   - ¿Sincronizar ambos?

**Acción por ahora**: ⏸️ **SKIP** - No tocar user-info hasta decisión arquitectónica

---

### Fase 3: Validar New Menu (VERIFICACIÓN) 🟢

**Objetivo**: Asegurar que new-menu funciona correctamente con el backend actualizado.

**Archivos**:
- `src/frontend/src/app/components/new-menu/new-menu.component.ts`

**Tareas**:
1. [ ] Test manual: Crear menú con fotos
2. [ ] Verificar que las fotos aparecen en la BD (tabla `dishes.photo`)
3. [ ] Verificar que las fotos se muestran en búsqueda/detalles

**Estado**: ✅ Ya funciona (envia `photo: photoBase64` en createDish)

---

## Checklist de Implementación

### Inmediato (Esta sesión):
- [ ] Fix edit-menu: agregar método `fileToBase64()`
- [ ] Fix edit-menu: hacer `saveDishes()` async
- [ ] Fix edit-menu: enviar `photo` en payload de updateDish
- [ ] Fix edit-menu: enviar `photo` en payload de createDish (nuevos platos)
- [ ] Test manual: editar menú y cambiar fotos de platos
- [ ] Test manual: agregar nuevo plato con foto a menú existente

### Futuro (Siguiente PR):
- [ ] Decisión arquitectónica: ¿dónde guardar foto de perfil? (users vs chefs)
- [ ] Integrar uploadChefPhoto en user-info (si se decide)
- [ ] Implementar cover photo en frontend (campo existe en BD, no en UI)
- [ ] Agregar visualización de cover photo en perfil público

### Backend (informar a compañera):
- [ ] Agregar columna `photo LONGTEXT` a tabla `dishes` (DDL)
- [ ] Actualizar entidad `Dish.java` con campo `photo`
- [ ] Actualizar DTOs: `DishCReqDto`, `DishUReqDto`, `DishDto`
- [ ] Actualizar `DishService` y `DishServiceImpl` para manejar campo `photo`

---

## Arquitectura de Fotos - Estado Actual

### Tabla `users`
```sql
photo LONGTEXT DEFAULT NULL  -- Usado por auth.service.ts
```
- Usado por: `updateUser()` en frontend
- Componente: `user-info.component.ts`
- Proceso: Resize 200x200 → Base64 → Guarda en users.photo

### Tabla `chefs`
```sql
photo varchar(255) DEFAULT NULL       -- Usado por PhotoUploadService (backend)
cover_photo text DEFAULT NULL         -- Existe pero NO usado en frontend
```
- Usado por: `uploadChefPhoto()` (backend nuevo)
- ⚠️ **INCONSISTENCIA**: Backend guarda aquí, pero frontend NO consume desde aquí

### Tabla `dishes`
```sql
-- ❌ FALTA: columna photo (debe agregarse en migración SQL)
```
- Usado por: new-menu y edit-menu envían `photo` pero BD no tiene columna
- ⚠️ **BLOQUEANTE BACKEND**: Sin esta columna, las fotos se pierden

---

## Testing Plan

### Test 1: Crear menú con fotos
1. Login como chef
2. Ir a "Crear menú"
3. Agregar 2 platos con fotos
4. Guardar
5. ✅ Verificar: Fotos aparecen en "Mis menús"

### Test 2: Editar menú - cambiar foto
1. Login como chef
2. Abrir menú existente
3. Cambiar foto de un plato
4. Guardar
5. ✅ Verificar: Nueva foto aparece

### Test 3: Editar menú - agregar plato con foto
1. Login como chef
2. Abrir menú existente
3. Agregar nuevo plato con foto
4. Guardar
5. ✅ Verificar: Nuevo plato con foto aparece

### Test 4: Perfil - cambiar foto
1. Login como chef
2. Ir a perfil
3. Cambiar foto de perfil
4. Guardar
5. ✅ Verificar: Nueva foto aparece en navbar y perfil público

---

## Notas Importantes

1. **Backend ya tiene PhotoUploadService** pero solo para chef photo/cover photo, NO para dishes
2. **Tabla dishes NO tiene columna photo** - backend debe agregarla
3. **Frontend new-menu YA envia fotos** - listo para cuando backend agregue columna
4. **Frontend edit-menu NO envia fotos** - esto es lo que debemos arreglar AHORA

---

## Próximos Pasos

1. **HOY**: Corregir edit-menu para enviar fotos ✅
2. **Informar backend**: Necesitan agregar columna `photo` a tabla `dishes`
3. **Después**: Decisión sobre arquitectura de fotos de perfil (users vs chefs)

---

**Rama de trabajo**: `feature/p1-frontend-photo-integration`
**Estado**: En progreso
**Prioridad**: Fase 1 (Edit Menu) es bloqueante
