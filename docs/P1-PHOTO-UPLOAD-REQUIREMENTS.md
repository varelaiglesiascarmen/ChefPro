# P1 - Photo Upload Service Requirements

## Resumen Ejecutivo

**Estado P0**: ✅ 100% completado y mergeado en `main`
- Eliminadas todas las alertas/confirms nativas
- Removidos todos los console.log
- Reservations API estandarizada
- Rama de trabajo: `p1-photo-upload-service` (creada basada en main)

**Objetivo P1**: Implementar sistema completo de manejo de fotos para platos de menú y perfiles de chef/diner.

---

## 1. Análisis del Frontend (Estado Actual)

El frontend YA está preparado para enviar fotos. Esto es lo que está haciendo:

### 1.1 Fotos de Platos (Dishes)

**Archivos involucrados**:
- `src/frontend/src/app/components/new-menu/new-menu.component.ts`
- `src/frontend/src/app/components/profile/sidebar/edit-menu/edit-menu.component.ts`

**Flujo actual**:

1. **Al crear plato en new-menu.component.ts**:
   ```typescript
   // El usuario selecciona una imagen
   onFileSelected(event: any, index: number) {
     const file = event.target.files[0];
     if (file.size > 2 * 1024 * 1024) { // Max 2MB
       this.toastService.error('La imagen es demasiado pesada. Máximo 2MB.');
       return;
     }
     this.dishes[index].photo = file;
   }

   // Al guardar, convierte a Base64
   private fileToBase64(file: File): Promise<string> {
     return new Promise((resolve, reject) => {
       const reader = new FileReader();
       reader.onload = () => resolve(String(reader.result ?? ''));
       reader.onerror = () => reject(new Error('Error al procesar la imagen'));
       reader.readAsDataURL(file);
     });
   }

   // Envia al backend con este payload:
   const dishPayload = {
     menu_ID: newMenuId,
     dish_ID: index + 1,
     title: dish.title,
     description: dish.description,
     category: dish.category,
     allergens: allergenNames,
     photo: photoBase64  // ← AQUI VA LA FOTO EN BASE64
   };
   ```

2. **POST a `/api/chef/plato`** con el payload completo que incluye `photo: photoBase64`

3. **Al editar platos en edit-menu**:
   - El usuario puede seleccionar una foto nueva
   - ⚠️ **PROBLEMA**: Actualmente **NO se envía la foto** al PATCH. Solo envía:
     ```typescript
     {
       menuId, dishId, title, description, category, allergens
     }
     ```
   - **NECESITA CORREGIRSE**: Agregar `photo` al payload del PATCH

### 1.2 Fotos de Perfil (User)

**Archivos involucrados**:
- `src/frontend/src/app/components/profile/sidebar/user-info/user-info.component.ts`

**Flujo actual**:

1. Usuario selecciona foto de perfil (máx 2MB)
2. Se redimensiona a 200x200px usando Canvas
3. Se convierte a Base64 en formato JPEG
4. Se envía al backend vía `authService.updateUser()` con campo `photoUrl: base64String`
5. El backend recibe en tabla `users` columna `photo LONGTEXT`

---

## 2. Estructura Base de Datos (Estado Actual)

### 2.1 Tabla `dishes` (INCOMPLETA)

```sql
CREATE TABLE `dishes` (
  `menu_ID` int(11) NOT NULL,
  `dish_ID` int(11) NOT NULL,
  `title` varchar(150) NOT NULL,
  `description` text DEFAULT NULL,
  `category` varchar(50) DEFAULT NULL,
  -- ⚠️ FALTA: columna para foto del plato
  PRIMARY KEY (`menu_ID`,`dish_ID`),
  CONSTRAINT `dishes_ibfk_1` FOREIGN KEY (`menu_ID`) REFERENCES `menu` (`menu_ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

### 2.2 Tabla `users` (COMPLETA)

```sql
CREATE TABLE `users` (
  user_ID int(11) NOT NULL AUTO_INCREMENT,
  ...
  `photo` LONGTEXT DEFAULT NULL,  -- ✅ Aqui se guardan fotos en Base64
  ...
  PRIMARY KEY (`user_ID`),
  ...
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

### 2.3 Tabla `chefs` (Parcialmente completa)

```sql
CREATE TABLE `chefs` (
  `user_ID` int(11) NOT NULL,
  `photo` varchar(255) DEFAULT NULL,        -- Existente pero no usado
  `bio` text DEFAULT NULL,
  `prizes` text DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `address` varchar(500) DEFAULT NULL,
  `languages` varchar(255) DEFAULT NULL,
  `cover_photo` text DEFAULT NULL,          -- Cover photo vacía
  PRIMARY KEY (`user_ID`),
  CONSTRAINT `chefs_ibfk_1` FOREIGN KEY (`user_ID`) REFERENCES `users` (`user_ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

---

## 3. Requerimientos de Implementación para Backend - PhotoUploadService

### 3.1 Migración de Base de Datos (DDL)

**Opción A - Agregar columna a tabla dishes (RECOMENDADA - Simple)**:

```sql
-- Archivo: src/backend/src/main/resources/database/03-add-photo-column.sql
ALTER TABLE `dishes` 
ADD COLUMN `photo` LONGTEXT DEFAULT NULL COMMENT 'Base64 encoded dish photo' AFTER `category`;
```

**OR Opción B - Crear tabla separada photos (Más escalable)**:

Si quieres normalización de base de datos:

```sql
CREATE TABLE `dish_photos` (
  `photo_ID` int(11) NOT NULL AUTO_INCREMENT,
  `menu_ID` int(11) NOT NULL,
  `dish_ID` int(11) NOT NULL,
  `photo_data` LONGTEXT NOT NULL,
  `uploaded_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`photo_ID`),
  UNIQUE KEY `unique_dish` (`menu_ID`, `dish_ID`),
  CONSTRAINT `dish_photos_ibfk_1` FOREIGN KEY (`menu_ID`, `dish_ID`) REFERENCES `dishes` (`menu_ID`, `dish_ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

**RECOMENDACIÓN**: Usa **Opción A** por simplicidad. Si se necesita foto de múltiples versiones del plato en el futuro, migrar a tabla separada es fácil.

### 3.2 Modelo JPA - Photo/DishPhoto Entity

**Si usas Opción A (columna en dishes)**:

Modifica `Dish.java`:

```java
package com.chefpro.backendjava.common.object.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dishes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(Dish.DishId.class)
public class Dish {

  @Id
  @Column(name = "menu_ID", nullable = false)
  private Long menuId;

  @Id
  @Column(name = "dish_ID", nullable = false)
  private Long dishId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "menu_ID", insertable = false, updatable = false)
  private Menu menu;

  @Column(name = "title", length = 150, nullable = false)
  private String title;

  @Lob
  @Column(name = "description")
  private String description;

  @Column(name = "category", length = 50)
  private String category;

  // ✅ AGREGAR ESTA COLUMNA
  @Lob
  @Column(name = "photo")
  private String photo;  // Base64 encoded image data

  @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<AllergenDish> allergenDishes = new ArrayList<>();

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DishId implements Serializable {
    private Long menuId;
    private Long dishId;
  }
}
```

**Si usas Opción B (tabla separada)**: Crea entidad `DishPhoto.java` nueva.

### 3.3 DTOs - Actualizar Request/Response DTOs

**DishCReqDto.java** (Create Request):

```java
package com.chefpro.backendjava.common.object.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DishCReqDto {

  @JsonProperty("menu_ID")
  private Long menuId;

  @JsonProperty("dish_ID")
  private Long dishId;

  private String title;
  private String description;
  private String category;
  private List<String> allergens;
  
  // ✅ AGREGAR ESTE CAMPO
  private String photo;  // Base64 encoded image data (opcional)
}
```

**DishUReqDto.java** (Update Request):

```java
package com.chefpro.backendjava.common.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DishUReqDto {

  private Long menuId;
  private Long dishId;
  private String title;
  private String description;
  private String category;
  private List<String> allergens;
  
  // ✅ AGREGAR ESTE CAMPO
  private String photo;  // Base64 encoded image data (opcional)
}
```

**DishDto.java** (Response DTO) - Actualizar para incluir foto:

```java
package com.chefpro.backendjava.common.object.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DishDto {

  private Long menuId;
  private Long dishId;
  private String title;
  private String description;
  private String category;
  private List<String> allergens;
  
  // ✅ AGREGAR ESTE CAMPO
  private String photo;     // Base64 encoded image data
}
```

### 3.4 Servicio - DishService & DishServiceImpl

**Actualizar DishService.java**:

```java
package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.DishCReqDto;
import com.chefpro.backendjava.common.object.dto.DishDto;
import com.chefpro.backendjava.common.object.dto.DishUReqDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface DishService {
  List<DishDto> getDish(Authentication authentication, String nombrePlato);
  
  void createDish(Authentication authentication, DishCReqDto dishCReqDto);
  
  DishDto updateDish(Authentication authentication, DishUReqDto dishUReqDto);
  
  void deleteDish(Authentication authentication, Long menuId, Long dishId);
  
  // ✅ AGREGAR ESTOS MÉTODOS PARA FOTOS
  /**
   * Sube/actualiza la foto de un plato
   * @param menuId ID del menú
   * @param dishId ID del plato
   * @param photoBase64 Datos de imagen en Base64
   * @return DishDto actualizado con la foto
   */
  DishDto uploadDishPhoto(Long menuId, Long dishId, String photoBase64);
  
  /**
   * Obtiene la foto de un plato específico
   * @param menuId ID del menú
   * @param dishId ID del plato
   * @return String con foto en Base64 o null
   */
  String getDishPhoto(Long menuId, Long dishId);
  
  /**
   * Elimina la foto de un plato
   * @param menuId ID del menú
   * @param dishId ID del plato
   */
  void deleteDishPhoto(Long menuId, Long dishId);
}
```

**Actualizar DishServiceImpl.java**:

En los métodos existentes `createDish()` y `updateDish()`, asegúrate de:

```java
@Override
public void createDish(Authentication authentication, DishCReqDto dishCReqDto) {
  // ... código existente ...
  
  Dish dish = Dish.builder()
    // ... campos existentes ...
    .photo(dishCReqDto.getPhoto())  // ✅ GUARDAR FOTO SI VIENE
    .build();
  
  dishRepository.save(dish);
}

@Override
public DishDto updateDish(Authentication authentication, DishUReqDto dishUReqDto) {
  // ... código existente ...
  
  Dish dish = dishRepository.findById(new Dish.DishId(...)).orElseThrow(...);
  
  if (dishUReqDto.getTitle() != null) {
    dish.setTitle(dishUReqDto.getTitle());
  }
  if (dishUReqDto.getDescription() != null) {
    dish.setDescription(dishUReqDto.getDescription());
  }
  if (dishUReqDto.getCategory() != null) {
    dish.setCategory(dishUReqDto.getCategory());
  }
  // ✅ ACTUALIZAR FOTO SI VIENE
  if (dishUReqDto.getPhoto() != null) {
    dish.setPhoto(dishUReqDto.getPhoto());
  }
  
  Dish updated = dishRepository.save(dish);
  return mapToDishDto(updated);
}

// ✅ IMPLEMENTAR NUEVOS MÉTODOS
@Override
public DishDto uploadDishPhoto(Long menuId, Long dishId, String photoBase64) {
  Dish dish = dishRepository.findById(new Dish.DishId(menuId, dishId))
    .orElseThrow(() -> new RuntimeException("Dish not found"));
  
  dish.setPhoto(photoBase64);
  Dish updated = dishRepository.save(dish);
  return mapToDishDto(updated);
}

@Override
public String getDishPhoto(Long menuId, Long dishId) {
  Dish dish = dishRepository.findById(new Dish.DishId(menuId, dishId))
    .orElseThrow(() -> new RuntimeException("Dish not found"));
  return dish.getPhoto();
}

@Override
public void deleteDishPhoto(Long menuId, Long dishId) {
  Dish dish = dishRepository.findById(new Dish.DishId(menuId, dishId))
    .orElseThrow(() -> new RuntimeException("Dish not found"));
  dish.setPhoto(null);
  dishRepository.save(dish);
}
```

### 3.5 Controlador - Endpoints para Foto

**Actualizar ChefController.java** para agregar endpoints de foto:

```java
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/chef")
public class ChefController {
  
  private final DishService dishService;
  
  // ... métodos existentes ...
  
  // ✅ AGREGAR ESTOS ENDPOINTS
  
  /**
   * GET /api/chef/plato/{menuId}/{dishId}/photo
   * Obtiene la foto de un plato específico
   */
  @GetMapping("/plato/{menuId}/{dishId}/photo")
  public ResponseEntity<String> getDishPhoto(
    @PathVariable Long menuId,
    @PathVariable Long dishId
  ) {
    String photo = dishService.getDishPhoto(menuId, dishId);
    if (photo == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(photo);
  }
  
  /**
   * POST /api/chef/plato/{menuId}/{dishId}/photo
   * Sube/actualiza la foto de un plato
   * Body: { "photo": "data:image/jpeg;base64,..." }
   */
  @PostMapping("/plato/{menuId}/{dishId}/photo")
  public ResponseEntity<DishDto> uploadDishPhoto(
    Authentication authentication,
    @PathVariable Long menuId,
    @PathVariable Long dishId,
    @RequestBody PhotoUploadDto photoUploadDto
  ) {
    // Validar autenticación del chef propietario
    // ... código de validación ...
    
    DishDto updated = dishService.uploadDishPhoto(menuId, dishId, photoUploadDto.getPhoto());
    return ResponseEntity.ok(updated);
  }
  
  /**
   * DELETE /api/chef/plato/{menuId}/{dishId}/photo
   * Elimina la foto de un plato
   */
  @DeleteMapping("/plato/{menuId}/{dishId}/photo")
  public ResponseEntity<Void> deleteDishPhoto(
    Authentication authentication,
    @PathVariable Long menuId,
    @PathVariable Long dishId
  ) {
    // Validar autenticación del chef propietario
    // ... código de validación ...
    
    dishService.deleteDishPhoto(menuId, dishId);
    return ResponseEntity.noContent().build();
  }
}
```

### 3.6 DTO Auxiliar para Upload

```java
package com.chefpro.backendjava.common.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhotoUploadDto {
  private String photo;  // Base64 encoded image data
}
```

---

## 4. Problema Identificado: Edit Menu No Envía Foto

**Ubicación**: `src/frontend/src/app/components/profile/sidebar/edit-menu/edit-menu.component.ts`

**Problema**: Cuando el usuario actualiza un plato existente y cambia su foto, la foto NO se envía al backend.

**Código actual (línea ~407)**:
```typescript
if (dish.dishId) {
  requests.push(this.menuService.updateDish({
    menuId: this.menuId,
    dishId: dish.dishId,
    title: dish.title.trim(),
    description: dish.description.trim(),
    category: dish.category,
    allergens
    // ⚠️ FALTA: photo
  }));
}
```

**Solución requerida**:
```typescript
// Convertir foto a Base64 si existe
const photoBase64 = dish.photo ? await this.fileToBase64(dish.photo) : null;

if (dish.dishId) {
  requests.push(this.menuService.updateDish({
    menuId: this.menuId,
    dishId: dish.dishId,
    title: dish.title.trim(),
    description: dish.description.trim(),
    category: dish.category,
    allergens,
    photo: photoBase64  // ✅ AGREGAR
  }));
}
```

**Necesita**:
- Implementar método `fileToBase64()` en edit-menu.component.ts (copiar de new-menu.component.ts)
- Hacer `saveDishes()` async y manejar Promises correctamente

---

## 5. Estructura de Carpetas - Assets y Recursos

**Para recuperar fotos en el frontend desde el backend**:

Se pueden usar dos enfoques:

### Opción A - Fotos incrustadas en MongoDB/Base (Actual - Simple)
- La foto viene en Base64 como parte del objeto Dish
- Frontend usa directamente: `<img [src]="dish.photo" />`
- **Ventaja**: Sin requests HTTP adicionales
- **Desventaja**: JSON response grande

### Opción B - Endpoint separado (Escalable)
- Backend devuelve solo URL: `dish.photoUrl = '/api/chef/plato/123/456/photo'`
- Frontend hace GET a ese endpoint cuando necesita la foto
- **Ventaja**: Fotos grandes no ralentizan listados
- **Desventaja**: Request HTTP adicional por foto

**RECOMENDACIÓN**: Mantener Opción A por ahora (lo que hace nuevo-menu). Si rendimiento es problema, migrar a Opción B después.

---

## 6. Testing Requerido

### 6.1 Testes Backend

```java
@Test
public void testCreateDishWithPhoto() {
  DishCReqDto request = DishCReqDto.builder()
    .menuId(1L)
    .dishId(1L)
    .title("Pasta Carbonara")
    .description("Classic Roman pasta")
    .category("Main Course")
    .allergens(List.of("Lactose", "Eggs"))
    .photo("data:image/jpeg;base64,/9j/4AAQSkZJRg...")
    .build();
  
  dishService.createDish(authentication, request);
  
  Dish dish = dishRepository.findById(new Dish.DishId(1L, 1L)).orElseThrow();
  assertNotNull(dish.getPhoto());
  assertTrue(dish.getPhoto().startsWith("data:image"));
}

@Test
public void testUpdateDishPhotoOnly() {
  String newPhoto = "data:image/jpeg;base64,/9j/4AAQSkZJRg...";
  
  DishDto updated = dishService.uploadDishPhoto(1L, 1L, newPhoto);
  
  assertNotNull(updated.getPhoto());
  assertEquals(newPhoto, updated.getPhoto());
}

@Test
public void testDeleteDishPhoto() {
  dishService.deleteDishPhoto(1L, 1L);
  
  String photo = dishService.getDishPhoto(1L, 1L);
  assertNull(photo);
}
```

### 6.2 Testes Frontend (Angular)

```typescript
it('should send photo when creating new menu', (done) => {
  const photoBase64 = 'data:image/jpeg;base64,...';
  spyOn(chefService, 'createDish').and.returnValue(of({}));
  
  component.dishes[0].photo = mockFile;
  component.fileToBase64(mockFile).then(base64 => {
    // Verificar que se envía
    expect(chefService.createDish).toHaveBeenCalledWith(
      jasmine.objectContaining({
        photo: base64
      })
    );
    done();
  });
});

it('should send photo when updating dish', (done) => {
  spyOn(menuService, 'updateDish').and.returnValue(of({}));
  
  component.dishes[0].photo = mockFile;
  component.saveDishes();
  
  setTimeout(() => {
    expect(menuService.updateDish).toHaveBeenCalledWith(
      jasmine.objectContaining({
        photo: jasmine.any(String)
      })
    );
    done();
  }, 100);
});
```

---

## 7. Checklist de Implementación

### Backend:

- [ ] Crear archivo de migración SQL: `03-add-photo-column.sql`
- [ ] Actualizar entidad `Dish.java` con campo `photo`
- [ ] Actualizar `DishCReqDto.java` agregar `photo`
- [ ] Actualizar `DishUReqDto.java` agregar `photo`
- [ ] Actualizar `DishDto.java` agregar `photo`
- [ ] Crear `PhotoUploadDto.java`
- [ ] Actualizar `DishService.java` agregar métodos de foto
- [ ] Actualizar `DishServiceImpl.java` implementar métodos
- [ ] Actualizar `ChefController.java` agregar endpoints `/photo`
- [ ] Ejecutar testes unitarios
- [ ] Validar endpoints con Postman
- [ ] Manejar excepciones (foto muy grande, formato inválido)

### Frontend (Correcciones necesarias):

- [ ] Agregar método `fileToBase64()` a `edit-menu.component.ts`
- [ ] Actualizar `saveDishes()` para hacer async y enviar `photo`
- [ ] Actualizar payload del PATCH en `updateDish()` para incluir `photo`
- [ ] Testes unitarios para upload de foto en edit-menu
- [ ] Validar integración con backend

### Testing:

- [ ] Postman: POST `/api/chef/plato` con foto
- [ ] Postman: PATCH `/api/chef/plato` con foto actualizada
- [ ] Postman: GET `/api/chef/plato/{menuId}/{dishId}/photo`
- [ ] Postman: DELETE `/api/chef/plato/{menuId}/{dishId}/photo`
- [ ] Frontend: Crear menú con fotos
- [ ] Frontend: Editar menú y cambiar fotos
- [ ] Frontend: Visualizar fotos en búsqueda y detalles

---

## 8. Consideraciones de Performance & Seguridad

### Security:

1. **Validación de tamaño**: Backend debe validar que `photo` Base64 no exceda límite (ej: 5MB)
2. **Validación de formato**: Verificar que sea Base64 válido
3. **Autorización**: Solo el chef propietario del menú puede actualizar fotos
4. **Rate limiting**: Proteger endpoint de foto de abuso (max 10 uploads/min por usuario)

### Performance:

1. **Lazy loading**: Frontend solo cargar fotos cuando sea necesario
2. **Compresión**: Considerar comprimir Base64 en DB (usar COLUMN COMPRESSION en MySQL 5.7+)
3. **Índices**: No hay índices especiales necesarios para columna LONGTEXT
4. **Caché**: Considerar caché en Redis para fotos frecuentemente solicitadas (P2 futura)

### SQL Optimization:

```sql
-- Si quieres, puedes crear índice de tamaño para fotos (opcional)
ALTER TABLE `dishes` 
ADD INDEX `idx_has_photo` (
  `menu_ID`, `dish_ID`, 
  (IF(photo IS NOT NULL, 1, 0))
);
```

---

## 9. Dependencias Externas Necesarias

Verifica que estas dependencias están en `pom.xml`:

```xml
<!-- Para validación de Base64 (si usas commons-codec) -->
<dependency>
  <groupId>commons-codec</groupId>
  <artifactId>commons-codec</artifactId>
  <version>1.15</version>
</dependency>

<!-- Para file size validation -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## 10. Notas Importantes

1. **El frontend YA ESTÁ PREPARADO** para enviar fotos. Solo es necesario que el backend las reciba y guarde.

2. **El único problema identificado**: `edit-menu.component.ts` no envía foto al actualizar - necesita corrección en frontend.

3. **Base64 en LONGTEXT**: Es una decisión válida para este MVP. Ventajas:
   - Simple
   - No necesita servicio de storage externo
   - Funciona en cualquier BD
   - Escalable hasta ~100K fotos sin problemas

4. **Alternativas futuras** (P2 o P3):
   - Azure Blob Storage
   - AWS S3
   - MinIO local
   - CDN para distribuir fotos

5. **Validaciones recomendadas en Base64**:
   - Máximo 2-3MB de imagen
   - Solo formatos: JPEG, PNG, WebP
   - Rechazar SVG y formatos ejecutables

---

## 11. Rama de Trabajo

```bash
# Rama creada para P1:
git checkout p1-photo-upload-service

# Para actualizar tu rama local con cambios de main:
git pull origin main

# Para hacer push de tus cambios:
git push -u origin p1-photo-upload-service
```

---

## Contacto & Dudas

Si tu compañera tiene dudas sobre:
- Estructura de las DTOs
- Cómo validar Base64 en Java
- Cómo manejar fotos muy grandes
- Alternativas de almacenamiento

Que revise el código en `src/backend/src/main/java/com/chefpro/backendjava/` para seguir las convenciones ya establecidas.

---

**Documento generado**: 27 de Febrero, 2026
**Estado P0**: ✅ Completado en commit `13a96ec`
**P1 - PhotoUploadService**: En desarrollo en rama `p1-photo-upload-service`
