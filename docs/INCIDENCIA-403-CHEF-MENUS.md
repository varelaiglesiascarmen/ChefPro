# Incidencia: 403 en `GET /api/chef/menus` (perfil Chef)

## 1) Resumen para backend
En la interfaz de Chef (`/profile/menus`) el frontend llama a:

- `GET /api/chef/menus`

La API responde `403 Forbidden`.

### Conclusión funcional
El backend está tratando al usuario autenticado como **DINER** (o al menos sin autoridad `ROLE_CHEF`) cuando en frontend el usuario se está usando como Chef.

---

## 2) Qué ve la usuaria en frontend
- Pantalla: gestión de menús de Chef.
- Resultado actual: no carga menús y aparece mensaje de permiso denegado.
- Motivo: la seguridad del backend bloquea `/api/chef/**` si no hay `ROLE_CHEF`.

---

## 3) Dónde se rompe exactamente
La ruta está protegida con rol Chef:

- `requestMatchers("/api/chef/**").hasAuthority("ROLE_CHEF")`

Si la request llega con un principal cuyo rol efectivo no es `ROLE_CHEF`, Spring responde `403` antes de ejecutar el controller.

---

## 4) Contrato que espera frontend en esa interfaz
El componente de menús de chef espera que `GET /api/chef/menus` devuelva **200 OK** con **array de menús**.

Campos mínimos usados por la UI de listado:
- `menu_ID` (number) → id para editar/eliminar
- `title` (string)
- `description` (string)
- `pricePerPerson` (number)
- `photoUrl` (string opcional)

### Ejemplo de respuesta válida
```json
[
  {
    "menu_ID": 12,
    "title": "Menú Mediterráneo",
    "description": "Entrante, principal y postre con producto de temporada",
    "pricePerPerson": 45.0,
    "photoUrl": "https://.../menu-12.jpg"
  },
  {
    "menu_ID": 15,
    "title": "Menú Degustación",
    "description": "7 pases con maridaje opcional",
    "pricePerPerson": 65.0,
    "photoUrl": null
  }
]
```

Si no hay menús, debe devolver:
```json
[]
```

No debe devolver `403` para un Chef autenticado correctamente.

---

## 5) Qué debe revisar backend para arreglarlo

### A. Rol real en base de datos
Verificar que el usuario autenticado tenga `role = CHEF` en `users`.

```sql
SELECT user_ID, username, email, role
FROM users
WHERE username = :username OR email = :email;
```

Si sale `DINER`, ese es el origen del 403.

### B. Relación en tabla `chefs`
Verificar que exista fila en `chefs` para ese `user_ID`.

```sql
SELECT c.*
FROM chefs c
JOIN users u ON u.user_ID = c.user_ID
WHERE u.username = :username;
```

Si falta, crearla.

### C. Autoridades que carga Spring Security
`CustomUserDetailsService` debe construir `ROLE_CHEF` para ese usuario.

### D. Token y sesión
Tras corregir rol/relación, forzar nuevo login para regenerar contexto de autenticación y probar de nuevo.

---

## 6) Ejemplos de corrección rápida

### Caso 1: usuario mal clasificado como DINER
```sql
UPDATE users
SET role = 'CHEF'
WHERE username = :username;
```

### Caso 2: usuario CHEF sin registro en tabla `chefs`
```sql
INSERT INTO chefs (user_ID, created_at)
SELECT u.user_ID, NOW()
FROM users u
WHERE u.username = :username
  AND NOT EXISTS (
    SELECT 1 FROM chefs c WHERE c.user_ID = u.user_ID
  );
```

---

## 7) Validación final esperada
Tras el fix backend:
1. Login como chef
2. `GET /api/chef/menus` responde `200`
3. Body = array de menús (o `[]`)
4. La vista `/profile/menus` renderiza tarjetas sin error de permisos

---

## 8) Nota importante para coordinar frontend-backend
Frontend puede mostrar usuario “Chef” por estado local, pero la autorización real la decide backend con:
- rol en BD
- autoridades efectivas en Spring Security
- validez del token

Por eso el origen del problema es de autorización en API, no de maquetación/interfaz.
