# P1 Backend - Dish Photo Support Implementation

## Contexto

**Estado Frontend**: ✅ Ya está preparado para enviar fotos de platos
- `new-menu.component.ts` envía `photo` en Base64 al crear platos
- `edit-menu.component.ts` ahora también envía `photo` en Base64 al actualizar platos
- Ambos convierten las imágenes a Base64 usando `fileToBase64()` antes de enviar

**Estado Backend**: ❌ NO puede recibir ni guardar fotos de platos
- La tabla `dishes` NO tiene columna `photo`
- La entidad `Dish.java` NO tiene campo `photo`
- Los DTOs NO tienen campo `photo`
- El servicio NO procesa campo `photo`

---

## 📋 Checklist de Implementación

### 1. Migración SQL - Agregar Columna Photo a Dishes

**Archivo**: `src/backend/src/main/resources/database/03-add-dish-photo-column.sql`

```sql
-- Agregar columna photo a tabla dishes para guardar imágenes en Base64
USE chef_pro;

ALTER TABLE `dishes` 
ADD COLUMN `photo` LONGTEXT DEFAULT NULL COMMENT 'Base64 encoded dish photo' 
AFTER `category`;
```

**Ejecutar**:
```bash
# Si usas MySQL Workbench o terminal MySQL:
mysql -u root -p chef_pro < src/backend/src/main/resources/database/03-add-dish-photo-column.sql

# O ejecutar manualmente en tu cliente SQL
```

---

### 2. Actualizar Entidad Dish.java

**Archivo**: `src/backend/src/main/java/com/chefpro/backendjava/common/object/entity/Dish.java`

**Ubicación**: Después del campo `category`, antes de `allergenDishes`

```java
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

  // ✅ AGREGAR ESTE CAMPO
  @Lob
  @Column(name = "photo")
  private String photo;  // Base64 encoded image data with data URI prefix

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

---

### 3. Actualizar DTOs

#### 3.1 DishCReqDto.java (Create Request DTO)

**Archivo**: `src/backend/src/main/java/com/chefpro/backendjava/common/object/dto/DishCReqDto.java`

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
  private String photo;  // Base64 encoded image data (opcional, puede ser null)
}
```

#### 3.2 DishUReqDto.java (Update Request DTO)

**Archivo**: `src/backend/src/main/java/com/chefpro/backendjava/common/object/dto/DishUReqDto.java`

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
  private String photo;  // Base64 encoded image data (opcional, puede ser null)
}
```

#### 3.3 DishDto.java (Response DTO)

**Archivo**: Buscar donde esté definido (probablemente en `common/object/dto/`)

**Si existe**, agregar:
```java
private String photo;  // Base64 encoded image data
```

**Si NO existe**, necesitarás revisar qué devuelve `createDish()` y `updateDish()` actualmente. Si devuelven `Dish` directamente, está bien (el campo `photo` ya estará disponible después de actualizar la entidad).

---

### 4. Actualizar DishServiceImpl

**Archivo**: `src/backend/src/main/java/com/chefpro/backendjava/service/impl/DishServiceImpl.java`

#### 4.1 Método `createDish()`

Buscar el método `createDish()` y asegurarte de que guarda el campo `photo`:

```java
@Override
public void createDish(Authentication authentication, DishCReqDto dishCReqDto) {
  // ... código de validación existente ...
  
  // Crear entidad Dish
  Dish dish = Dish.builder()
    .menuId(dishCReqDto.getMenuId())
    .dishId(dishCReqDto.getDishId())
    .title(dishCReqDto.getTitle())
    .description(dishCReqDto.getDescription())
    .category(dishCReqDto.getCategory())
    .photo(dishCReqDto.getPhoto())  // ✅ AGREGAR ESTA LÍNEA
    .build();
  
  // Guardar en base de datos
  dishRepository.save(dish);
  
  // ... código de allergens existente ...
}
```

#### 4.2 Método `updateDish()`

Buscar el método `updateDish()` y asegurarte de que actualiza el campo `photo`:

```java
@Override
public DishDto updateDish(Authentication authentication, DishUReqDto dishUReqDto) {
  // ... código de validación existente ...
  
  // Buscar plato existente
  Dish dish = dishRepository.findById(
    new Dish.DishId(dishUReqDto.getMenuId(), dishUReqDto.getDishId())
  ).orElseThrow(() -> new RuntimeException("Dish not found"));
  
  // Actualizar campos si vienen en el request
  if (dishUReqDto.getTitle() != null) {
    dish.setTitle(dishUReqDto.getTitle());
  }
  if (dishUReqDto.getDescription() != null) {
    dish.setDescription(dishUReqDto.getDescription());
  }
  if (dishUReqDto.getCategory() != null) {
    dish.setCategory(dishUReqDto.getCategory());
  }
  
  // ✅ AGREGAR ESTAS LÍNEAS
  if (dishUReqDto.getPhoto() != null) {
    dish.setPhoto(dishUReqDto.getPhoto());
  }
  
  // Guardar cambios
  Dish updated = dishRepository.save(dish);
  
  // ... resto del código (allergens, mapeo a DTO, etc.) ...
  
  return mapToDishDto(updated);
}
```

#### 4.3 Método Mapper (si existe `mapToDishDto`)

Si tienes un método para convertir `Dish` → `DishDto`, asegúrate de incluir `photo`:

```java
private DishDto mapToDishDto(Dish dish) {
  return DishDto.builder()
    .menuId(dish.getMenuId())
    .dishId(dish.getDishId())
    .title(dish.getTitle())
    .description(dish.getDescription())
    .category(dish.getCategory())
    .photo(dish.getPhoto())  // ✅ AGREGAR ESTA LÍNEA
    .allergens(getAllergenNames(dish))
    .build();
}
```

---

### 5. Consideraciones de Validación (OPCIONAL pero recomendado)

#### 5.1 Validar Tamaño de Foto

En `DishServiceImpl`, antes de guardar:

```java
private void validatePhotoSize(String photoBase64) {
  if (photoBase64 == null || photoBase64.isEmpty()) {
    return; // Photo es opcional
  }
  
  // Estimar tamaño en bytes (Base64 es ~33% más grande que el binario)
  int estimatedSizeBytes = (photoBase64.length() * 3) / 4;
  int maxSizeBytes = 5 * 1024 * 1024; // 5 MB
  
  if (estimatedSizeBytes > maxSizeBytes) {
    throw new IllegalArgumentException("Photo exceeds maximum size of 5 MB");
  }
}

// Llamar en createDish() y updateDish():
validatePhotoSize(dishCReqDto.getPhoto());
```

#### 5.2 Validar Formato Base64

```java
private void validatePhotoFormat(String photoBase64) {
  if (photoBase64 == null || photoBase64.isEmpty()) {
    return;
  }
  
  // Verificar que tenga prefijo data URI válido
  if (!photoBase64.startsWith("data:image/")) {
    throw new IllegalArgumentException("Photo must be a valid Base64 image with data URI prefix");
  }
}
```

---

## 🧪 Testing

### Test Manual con Postman

#### Test 1: Crear Plato con Foto

**Request**:
```http
POST http://localhost:8080/api/chef/plato
Content-Type: application/json
Authorization: Bearer <your-jwt-token>

{
  "menu_ID": 1,
  "dish_ID": 1,
  "title": "Pasta Carbonara",
  "description": "Classic Italian pasta",
  "category": "Main Course",
  "allergens": ["Huevos", "Lácteos"],
  "photo": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD..."
}
```

**Response Esperada**:
```http
HTTP/1.1 201 Created
```

**Verificación**:
```sql
SELECT menu_ID, dish_ID, title, 
       SUBSTRING(photo, 1, 50) AS photo_preview
FROM dishes 
WHERE menu_ID = 1 AND dish_ID = 1;
```

#### Test 2: Actualizar Foto de Plato

**Request**:
```http
PATCH http://localhost:8080/api/chef/plato
Content-Type: application/json
Authorization: Bearer <your-jwt-token>

{
  "menuId": 1,
  "dishId": 1,
  "photo": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
}
```

**Response Esperada**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "menuId": 1,
  "dishId": 1,
  "title": "Pasta Carbonara",
  "description": "Classic Italian pasta",
  "category": "Main Course",
  "allergens": ["Huevos", "Lácteos"],
  "photo": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
}
```

### Test Unit (JUnit)

```java
@Test
public void testCreateDishWithPhoto() {
  // Arrange
  Authentication auth = mock(Authentication.class);
  when(auth.getName()).thenReturn("chef1");
  
  DishCReqDto request = DishCReqDto.builder()
    .menuId(1L)
    .dishId(1L)
    .title("Test Dish")
    .description("Description")
    .category("Main")
    .allergens(List.of("Gluten"))
    .photo("data:image/jpeg;base64,/9j/4AAQSkZJRg...")
    .build();
  
  // Act
  dishService.createDish(auth, request);
  
  // Assert
  Dish saved = dishRepository.findById(new Dish.DishId(1L, 1L)).orElseThrow();
  assertNotNull(saved.getPhoto());
  assertTrue(saved.getPhoto().startsWith("data:image"));
}

@Test
public void testUpdateDishPhoto() {
  // Arrange
  Dish existing = createSampleDish();
  dishRepository.save(existing);
  
  String newPhoto = "data:image/png;base64,iVBORw0KGgo...";
  DishUReqDto request = DishUReqDto.builder()
    .menuId(1L)
    .dishId(1L)
    .photo(newPhoto)
    .build();
  
  Authentication auth = mock(Authentication.class);
  when(auth.getName()).thenReturn("chef1");
  
  // Act
  DishDto result = dishService.updateDish(auth, request);
  
  // Assert
  assertEquals(newPhoto, result.getPhoto());
  
  Dish updated = dishRepository.findById(new Dish.DishId(1L, 1L)).orElseThrow();
  assertEquals(newPhoto, updated.getPhoto());
}
```

---

## 🔄 Integración con Frontend

Una vez implementados estos cambios en backend:

### Frontend → Backend Flow

1. **Usuario sube imagen** en new-menu o edit-menu
2. Frontend valida: max 2MB, solo imágenes
3. Frontend convierte a Base64 usando `fileToBase64()`
4. Frontend envía POST/PATCH con campo `photo` incluido
5. **Backend recibe y valida** (opcional: tamaño, formato)
6. **Backend guarda** en `dishes.photo` (LONGTEXT)
7. Backend devuelve respuesta con `photo` incluido
8. Frontend muestra imagen usando: `<img [src]="dish.photo" />`

### Flujo de Datos

```
┌─────────────────┐
│  Usuario        │
│  selecciona     │
│  imagen (File)  │
└────────┬────────┘
         │
         ▼
┌─────────────────────┐
│  Frontend           │
│  fileToBase64()     │
│  → Base64 string    │
└──────────┬──────────┘
           │
           │ HTTP POST/PATCH
           │ { photo: "data:image/..." }
           ▼
┌─────────────────────────┐
│  Backend                │
│  DishServiceImpl        │
│  → save to DB           │
│  dishes.photo LONGTEXT  │
└──────────┬──────────────┘
           │
           │ HTTP Response
           │ { photo: "data:image/..." }
           ▼
┌─────────────────────┐
│  Frontend           │
│  <img [src]="..." > │
│  Display photo      │
└─────────────────────┘
```

---

## ⚠️ Notas Importantes

### 1. Campo Photo es Opcional

- Un plato puede NO tener foto (null)
- Frontend enviará `null` si no se seleccionó imagen
- Backend debe manejar `null` sin errores

### 2. Base64 Incluye Data URI

El frontend envía:
```
data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD...
```

NO solo:
```
/9j/4AAQSkZJRgABAQEAYABgAAD...
```

El prefijo `data:image/jpeg;base64,` permite que el navegador use directamente en `<img src="">`.

### 3. Tamaño de Columna LONGTEXT

- MySQL `LONGTEXT` soporta hasta 4GB
- Base64 de imagen de 2MB → ~2.7MB en Base64
- `LONGTEXT` es más que suficiente

### 4. Performance

- Las fotos NO afectan queries de listado si usas `@Lob` con `LAZY` fetch
- Por ahora está bien (MVP)
- Si en futuro hay problemas de performance, migrar a almacenamiento externo (S3, Azure Blob, etc.)

---

## 📝 Resumen de Archivos a Modificar

1. ✅ **SQL**: `src/backend/src/main/resources/database/03-add-dish-photo-column.sql`
2. ✅ **Entity**: `Dish.java` → agregar campo `photo`
3. ✅ **DTOs**: 
   - `DishCReqDto.java` → agregar campo `photo`
   - `DishUReqDto.java` → agregar campo `photo`
   - `DishDto.java` (si existe) → agregar campo `photo`
4. ✅ **Service**: `DishServiceImpl.java` → 
   - Guardar `photo` en `createDish()`
   - Actualizar `photo` en `updateDish()`
   - Mapear `photo` en respuestas
5. ✅ **(Opcional)** Agregar validaciones de tamaño y formato

---

## ✅ Checklist Final

- [ ] Ejecutar migración SQL (agregar columna `photo` a `dishes`)
- [ ] Actualizar `Dish.java` con campo `photo`
- [ ] Actualizar `DishCReqDto.java` con campo `photo`
- [ ] Actualizar `DishUReqDto.java` con campo `photo`
- [ ] Actualizar `DishServiceImpl.createDish()` para guardar `photo`
- [ ] Actualizar `DishServiceImpl.updateDish()` para actualizar `photo`
- [ ] (Opcional) Agregar validaciones de tamaño y formato
- [ ] Ejecutar tests unitarios
- [ ] Test manual con Postman: crear plato con foto
- [ ] Test manual con Postman: actualizar foto de plato
- [ ] Verificar en DB que las fotos se guardan correctamente
- [ ] Test integración con frontend: crear menú con fotos
- [ ] Test integración con frontend: editar menú y cambiar fotos

---

## 🚀 Próximos Pasos

Una vez completada esta implementación:

1. **Frontend ya está listo** (commit actual en `feature/p1-frontend-photo-integration`)
2. **Backend puede recibir y guardar fotos** (tu implementación)
3. **Test end-to-end** juntos:
   - Crear menú con fotos → verificar en BD
   - Editar menú y cambiar fotos → verificar actualización
   - Ver fotos en búsqueda y detalles → verificar visualización

4. **Merge a main** cuando todo funcione

---

**Documento generado**: 27 de Febrero, 2026
**Estado Frontend**: ✅ Completado en rama `feature/p1-frontend-photo-integration`
**Estado Backend**: ⏳ Pendiente de implementación

**Co-authored-by**: reyes-art-car <reyes-art-car@users.noreply.github.com>
