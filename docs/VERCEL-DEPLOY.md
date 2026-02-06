# Guía de Despliegue en Vercel - ChefPro Frontend

Esta guía te explica **paso a paso** cómo desplegar la aplicación Angular en [Vercel](https://vercel.com), una plataforma cloud optimizada para frontends y aplicaciones estáticas.

---

## Índice

1. [¿Qué es Vercel?](#1-qué-es-vercel)
2. [Requisitos previos](#2-requisitos-previos)
3. [Crear cuenta en Vercel](#3-crear-cuenta-en-vercel)
4. [Vincular el proyecto con Vercel CLI](#4-vincular-el-proyecto-con-vercel-cli)
5. [Configurar secretos en GitHub](#5-configurar-secretos-en-github)
6. [Configurar la variable de API](#6-configurar-la-variable-de-api)
7. [Cómo funciona el despliegue automático](#7-cómo-funciona-el-despliegue-automático)
8. [Verificar el despliegue](#8-verificar-el-despliegue)
9. [Archivos creados/modificados en esta rama](#9-archivos-creadosmodificados-en-esta-rama)
10. [Conceptos clave aprendidos](#10-conceptos-clave-aprendidos)
11. [Solución de problemas](#11-solución-de-problemas)

---

## 1. ¿Qué es Vercel?

Vercel es una **plataforma de despliegue en la nube** especializada en aplicaciones frontend. Te permite:

- Desplegar SPAs (Single Page Applications) como Angular con un comando
- Obtener URLs de preview automáticas para cada Pull Request
- CDN global: tu app se sirve desde el servidor más cercano al usuario
- Certificado HTTPS automático
- Despliegues atómicos: si algo falla, la versión anterior sigue activa

**¿Por qué Vercel para el frontend de ChefPro?**
- Plan gratuito (Hobby) con builds ilimitados
- Optimizado para frameworks frontend (Angular, React, Vue...)
- Deploy de preview en cada PR → el equipo puede probar cambios antes de mergear
- Configuración mínima: detecta Angular automáticamente

> **💡 Diferencia con Railway:** Railway hospeda el **backend** (Java + MySQL, servicios que necesitan un servidor ejecutándose constantemente). Vercel hospeda el **frontend** (archivos estáticos HTML/CSS/JS que se sirven al navegador del usuario).

---

## 2. Requisitos previos

- [x] Cuenta de GitHub con el repositorio ChefPro
- [x] Node.js instalado localmente (v20 o superior)
- [x] Los archivos de esta rama (`feat/vercel-deploy`):
  - `src/frontend/vercel.json`
  - `src/frontend/src/environments/environment.prod.ts`
  - `.github/workflows/deploy-frontend.yml`

---

## 3. Crear cuenta en Vercel

1. Ve a [https://vercel.com](https://vercel.com)
2. Haz clic en **"Sign Up"** → **"Continue with GitHub"**
3. Autoriza la aplicación de Vercel en tu cuenta de GitHub
4. Selecciona el plan **"Hobby"** (gratuito, perfecto para proyectos personales/TFG)

> **💡 Concepto:** Vercel organiza tu trabajo en una **organización** (tu usuario personal) que contiene **proyectos**. Cada proyecto corresponde a una app desplegada.

---

## 4. Vincular el proyecto con Vercel CLI

La CLI de Vercel genera las credenciales que necesitamos para el despliegue automático.

### Instalar la CLI

```bash
npm install -g vercel
```

### Iniciar sesión

```bash
vercel login
```

Se abrirá el navegador para autenticarte. Sigue las instrucciones.

### Vincular el repositorio

```bash
# Desde la RAÍZ del repositorio ChefPro
cd /ruta/a/ChefPro

vercel link
```

Vercel te hará unas preguntas:

| Pregunta | Respuesta |
|----------|-----------|
| Set up project? | **Yes** |
| Which scope? | *(tu usuario de Vercel)* |
| Link to existing project? | **No** (crear uno nuevo) |
| Project name? | `chefpro-frontend` |
| Directory with source code? | `src/frontend` |

Esto crea un directorio `.vercel/` con un archivo `project.json` que contiene:

```json
{
  "orgId": "team_xxxxxxxxxxxx",
  "projectId": "prj_xxxxxxxxxxxx"
}
```

> **⚠️ Importante:** Necesitas estos dos valores (`orgId` y `projectId`) para el paso siguiente. **No subas `.vercel/` a Git** (ya debería estar en `.gitignore`).

### Obtener el Token de API

1. Ve a [https://vercel.com/account/tokens](https://vercel.com/account/tokens)
2. Haz clic en **"Create"**
3. Nómbralo: `github-actions-deploy`
4. Scope: **Full Account**
5. Expiration: elige la que prefieras (o "No Expiration" para el TFG)
6. Copia el token generado — **solo se muestra una vez**

---

## 5. Configurar secretos en GitHub

Los secretos permiten que GitHub Actions se autentique con Vercel sin exponer credenciales.

1. Ve a tu repositorio en GitHub: `github.com/varelaiglesiascarmen/ChefPro`
2. **Settings** → **Secrets and variables** → **Actions**
3. Pestaña **"Secrets"** → haz clic en **"New repository secret"** para cada uno:

| Nombre del secreto | Valor | De dónde lo sacas |
|---------------------|-------|-------------------|
| `VERCEL_TOKEN` | El token de API | Paso 4 (sección "Obtener el Token de API") |
| `VERCEL_ORG_ID` | `team_xxxxxxxxxxxx` | Archivo `.vercel/project.json` → campo `orgId` |
| `VERCEL_PROJECT_ID` | `prj_xxxxxxxxxxxx` | Archivo `.vercel/project.json` → campo `projectId` |

> **💡 Concepto:** Los **secretos de GitHub** son variables cifradas que solo se descifran durante la ejecución de un workflow. Ni siquiera los administradores del repo pueden ver su valor una vez guardados — esto los hace ideales para tokens y contraseñas.

---

## 6. Configurar la variable de API

Cuando el backend esté desplegado en Railway, necesitarás decirle al frontend dónde hacer las peticiones HTTP.

### Opción A: Variable de GitHub (recomendada)

1. En GitHub → **Settings** → **Secrets and variables** → **Actions**
2. Pestaña **"Variables"** (no Secrets) → **"New repository variable"**
3. Nombre: `API_URL`
4. Valor: `https://chefpro-production.up.railway.app`

El workflow sustituirá automáticamente el placeholder `{API_URL}` en `environment.prod.ts` por este valor durante el build.

### Opción B: Dejarlo por defecto

Si no defines `API_URL`, el workflow usará `/api` como fallback. Esto es útil para pruebas pero **no funcionará en producción** porque Vercel no tiene un proxy a tu backend.

> **📝 ¿Cómo funciona?** Angular es una aplicación estática — una vez compilada, son archivos HTML/CSS/JS que el navegador descarga y ejecuta. Por eso la URL de la API debe estar "horneada" (baked-in) en el código durante la compilación. No se puede cambiar después sin recompilar.
>
> El flujo es:
> ```
> environment.prod.ts tiene '{API_URL}' como placeholder
>     ↓ (durante el build en GitHub Actions)
> sed reemplaza '{API_URL}' por el valor real
>     ↓ (Angular compila con --configuration production)
> fileReplacements sustituye environment.ts → environment.prod.ts
>     ↓
> El bundle final contiene la URL real de la API
> ```

---

## 7. Cómo funciona el despliegue automático

El archivo `.github/workflows/deploy-frontend.yml` define dos flujos:

### Deploy de producción

```
Push a main (con cambios en src/frontend/)
    → GitHub Actions se activa
        → Instala dependencias (npm ci)
        → Inyecta API_URL en environment.prod.ts
        → Compila con Vercel CLI (vercel build --prod)
        → Despliega a producción (vercel deploy --prebuilt --prod)
```

La URL de producción es fija (ej: `chefpro-frontend.vercel.app`).

### Deploy de preview

```
Pull Request contra main (con cambios en src/frontend/)
    → GitHub Actions se activa
        → Mismos pasos pero sin --prod
        → Genera una URL de preview única para ese PR
```

Cada PR obtiene su propia URL temporal (ej: `chefpro-frontend-abc123.vercel.app`). Esto es muy útil para que el equipo revise cambios visualmente antes de aprobar el PR.

> **💡 Concepto:** El workflow **solo se activa** cuando hay cambios dentro de `src/frontend/**`. Así, commits que solo tocan el backend no desperdician ejecuciones de CI/CD.

---

## 8. Verificar el despliegue

### Desde GitHub

1. Ve a la pestaña **"Actions"** de tu repositorio
2. Busca la ejecución del workflow **"Deploy Frontend to Vercel"**
3. Si tiene un ✅ verde, el despliegue fue exitoso
4. En los logs del paso "Deploy to production", verás la URL desplegada

### Desde Vercel

1. Ve a [vercel.com/dashboard](https://vercel.com/dashboard)
2. Haz clic en tu proyecto `chefpro-frontend`
3. Verás todos los despliegues con su estado y URL

### Pruebas manuales

1. Abre la URL de producción en el navegador
2. Verifica que la página principal se carga
3. Navega a diferentes rutas (`/login`, `/about`, `/contact`)
4. **Recarga la página** en una ruta que no sea `/` — debe seguir funcionando (gracias a la regla de rewrite en `vercel.json`)
5. Abre DevTools → Network y verifica que las peticiones a la API apuntan a la URL correcta

---

## 9. Archivos creados/modificados en esta rama

| Archivo | Qué hace |
|---------|----------|
| `src/frontend/vercel.json` | Configura Vercel: comando de build, directorio de salida, y regla de rewrite para SPA routing |
| `src/frontend/src/environments/environment.prod.ts` | Entorno de producción con placeholder `{API_URL}` que se inyecta en CI/CD |
| `src/frontend/angular.json` | Añadido `fileReplacements` para usar `environment.prod.ts` en builds de producción |
| `src/frontend/src/app/services/search-results.service.ts` | Cambiado URL hardcodeada por `environment.apiUrl` |
| `.github/workflows/deploy-frontend.yml` | Workflow de CI/CD para compilar y desplegar en Vercel |
| `docs/VERCEL-DEPLOY.md` | Esta guía que estás leyendo |

### ¿Qué hace cada archivo de configuración?

**`vercel.json`** — Le dice a Vercel cómo tratar tu proyecto:
```json
{
  "buildCommand": "npm run build",
  "outputDirectory": "dist/frontend/browser",
  "framework": "angular",
  "rewrites": [
    { "source": "/(.*)", "destination": "/index.html" }
  ]
}
```

- `buildCommand`: qué comando ejecutar para compilar
- `outputDirectory`: dónde quedan los archivos compilados
- `framework`: Vercel aplica optimizaciones específicas para Angular
- `rewrites`: **crucial para SPAs** — redirige todas las rutas a `index.html` para que Angular Router las gestione en el navegador. Sin esto, acceder directamente a `/login` devolvería un 404.

**`environment.prod.ts`** — Configuración específica de producción:
```typescript
export const environment = {
  production: true,
  apiUrl: '{API_URL}'  // Se reemplaza por el valor real durante el build
};
```

**`fileReplacements` en `angular.json`** — Cuando compilas con `--configuration production` (comportamiento por defecto de `ng build`), Angular reemplaza `environment.ts` por `environment.prod.ts`. En desarrollo (`ng serve`), se usa el `environment.ts` original con `apiUrl: '/api'`.

---

## 10. Conceptos clave aprendidos

| Concepto | Descripción |
|----------|-------------|
| **SPA (Single Page Application)** | Angular genera una sola página HTML. La navegación ocurre en el navegador sin recargar. |
| **Static Hosting** | Vercel sirve archivos estáticos (HTML/CSS/JS) — no ejecuta código de servidor. |
| **CDN** | Content Delivery Network. Tus archivos se replican en servidores por todo el mundo. |
| **Preview Deployments** | Cada PR obtiene una URL temporal para probar cambios antes de mergear. |
| **Environment Files** | Angular usa archivos de entorno (`environment.ts`, `environment.prod.ts`) para separar configuración por entorno. |
| **File Replacements** | Mecanismo de Angular para intercambiar archivos según la configuración de build. |
| **SPA Rewrites** | Regla del servidor que redirige todas las rutas a `index.html` para que el router del cliente funcione. |
| **CI/CD** | Integración y Despliegue Continuos. Automatiza el build + deploy. |
| **GitHub Secrets** | Variables cifradas accesibles solo durante la ejecución de workflows. |
| **Build-time injection** | Inyectar valores de configuración durante la compilación (no en runtime). |

---

## 11. Solución de problemas

### El workflow falla en "Install frontend dependencies"

- Verifica que `package-lock.json` existe en `src/frontend/` y está commiteado
- `npm ci` es estricto: requiere que `package-lock.json` coincida con `package.json`

### El workflow falla en "Pull Vercel configuration"

- Verifica que los 3 secretos están configurados en GitHub:
  - `VERCEL_TOKEN`, `VERCEL_ORG_ID`, `VERCEL_PROJECT_ID`
- El token puede haber expirado → crea uno nuevo en Vercel Dashboard

### La app se despliega pero las rutas dan 404

- Verifica que `vercel.json` contiene la regla de `rewrites`
- Asegúrate de que `vercel.json` está en `src/frontend/` (junto a `angular.json`)

### La app carga pero las peticiones a la API fallan

- Abre DevTools → Network y revisa a qué URL se están enviando las peticiones
- Si apuntan a `/api` en vez de a tu backend en Railway:
  1. Define la variable `API_URL` en GitHub (Settings → Variables → Actions)
  2. Haz un nuevo push para que el workflow reconstruya con la URL correcta
- Si apuntan a la URL correcta pero dan error CORS:
  - Necesitas configurar CORS en tu backend Spring Boot para permitir el dominio de Vercel

### El build local funciona pero el de Vercel falla

- Vercel usa Node.js 20 — asegúrate de que tu proyecto es compatible
- Revisa los logs del despliegue en Vercel Dashboard → Deployments → haz clic en el fallido

### Quiero probar el build de producción en local

```bash
cd src/frontend

# Compilar en modo producción
npx ng build --configuration production

# Los archivos se generan en dist/frontend/browser/
# Puedes servirlos con cualquier servidor estático:
npx serve dist/frontend/browser
```

> **⚠️ Recuerda:** En local, `environment.prod.ts` tiene `{API_URL}` como placeholder. Para probarlo correctamente, sustituye ese valor manualmente o usa `ng serve` (que usa `environment.ts` con `/api` + proxy).

---

## Arquitectura del despliegue

```
┌─────────────────────────────────────────────────┐
│                   VERCEL (CDN)                   │
│                                                  │
│  ┌──────────────────────────────────────────┐   │
│  │  Frontend Angular (archivos estáticos)    │   │
│  │  HTML + CSS + JS compilados               │   │
│  │  URL: chefpro-frontend.vercel.app         │   │
│  └──────────────────────────────────────────┘   │
│         │                                        │
└─────────┼────────────────────────────────────────┘
          │ HTTPS (peticiones API)
          ▼
┌─────────────────────────────────────────────────┐
│                   RAILWAY                        │
│                                                  │
│  ┌──────────────────┐   ┌──────────────────┐    │
│  │  Backend (Java)   │──▶│   MySQL 8.0      │    │
│  │  Spring Boot 4.0  │   │   chef_pro DB    │    │
│  │  Puerto: $PORT    │   │   Puerto: 3306   │    │
│  └──────────────────┘   └──────────────────┘    │
│                                                  │
└──────────────────────────────────────────────────┘
```

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│  Developer    │  push   │   GitHub     │  deploy │   Vercel     │
│  (VS Code)   │ ──────▶ │   Actions    │ ──────▶ │   (CDN)      │
│              │         │  build+test  │         │  static host │
└──────────────┘         └──────────────┘         └──────────────┘
```
