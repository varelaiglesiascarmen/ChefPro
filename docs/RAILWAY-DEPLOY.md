# Guía de Despliegue en Railway - ChefPro Backend

Esta guía te explica **paso a paso** cómo desplegar la API de Spring Boot y la base de datos MySQL en [Railway](https://railway.com), una plataforma cloud que facilita el despliegue de aplicaciones.

---

## Índice

1. [¿Qué es Railway?](#1-qué-es-railway)
2. [Requisitos previos](#2-requisitos-previos)
3. [Crear cuenta en Railway](#3-crear-cuenta-en-railway)
4. [Crear el proyecto en Railway](#4-crear-el-proyecto-en-railway)
5. [Añadir la base de datos MySQL](#5-añadir-la-base-de-datos-mysql)
6. [Desplegar el backend Java](#6-desplegar-el-backend-java)
7. [Configurar variables de entorno](#7-configurar-variables-de-entorno)
8. [Conectar GitHub Actions (CI/CD)](#8-conectar-github-actions-cicd)
9. [Verificar el despliegue](#9-verificar-el-despliegue)
10. [Conceptos clave aprendidos](#10-conceptos-clave-aprendidos)
11. [Solución de problemas](#11-solución-de-problemas)

---

## 1. ¿Qué es Railway?

Railway es una **plataforma de despliegue en la nube (PaaS)** que te permite:

- Desplegar aplicaciones directamente desde un repositorio de GitHub
- Crear bases de datos (MySQL, PostgreSQL, Redis...) con un clic
- Gestionar variables de entorno de forma segura
- Escalar automáticamente según la demanda

**¿Por qué Railway para ChefPro?**
- Plan gratuito con $5 de crédito mensual (suficiente para desarrollo/TFG)
- Soporte nativo para Docker, Java, y MySQL
- Despliegue automático al hacer push a GitHub

---

## 2. Requisitos previos

- [x] Cuenta de GitHub con el repositorio ChefPro
- [x] Backend compilando correctamente (`./mvnw clean verify`)
- [x] Los archivos de esta rama (`feat/railway-deploy`):
  - `src/backend/Dockerfile`
  - `railway.toml`
  - `.github/workflows/deploy-backend.yml`

---

## 3. Crear cuenta en Railway

1. Ve a [https://railway.com](https://railway.com)
2. Haz clic en **"Login"** → **"Login with GitHub"**
3. Autoriza la aplicación de Railway en tu cuenta de GitHub
4. Railway te pedirá verificar tu cuenta (puede requerir una tarjeta, pero **NO te cobran** en el plan Trial)

---

## 4. Crear el proyecto en Railway

1. Una vez dentro, haz clic en **"New Project"**
2. Selecciona **"Empty Project"** (lo configuraremos manualmente)
3. Dale un nombre descriptivo: `chefpro-production`

> **💡 Concepto:** Un "Proyecto" en Railway es como un entorno que agrupa todos tus servicios (API, base de datos, etc.)

---

## 5. Añadir la base de datos MySQL

1. Dentro del proyecto, haz clic en **"+ New"** → **"Database"** → **"MySQL"**
2. Railway creará una instancia de MySQL automáticamente
3. Haz clic en el servicio MySQL que acabas de crear
4. Ve a la pestaña **"Variables"** y verás las credenciales generadas:
   - `MYSQL_HOST`
   - `MYSQL_PORT`
   - `MYSQL_DATABASE`
   - `MYSQL_USER`
   - `MYSQL_PASSWORD`
   - `MYSQL_URL` ← esta es la URL JDBC completa

### Importar el esquema y los datos iniciales

1. En el servicio MySQL, ve a la pestaña **"Data"**
2. Haz clic en **"Query"** (o usa la terminal de Railway)
3. Copia y pega el contenido de `src/backend/src/main/resources/database/01-create-schema.sql`
4. Ejecútalo
5. Después copia y pega `src/backend/src/main/resources/database/02-seeds.sql`
6. Ejecútalo

> **⚠️ Nota:** Esto solo hay que hacerlo la PRIMERA VEZ. Después, los datos persisten en Railway.

**Alternativa por terminal (si tienes `mysql` instalado localmente):**

```bash
# Los datos de conexión los sacas de las Variables del servicio MySQL en Railway
mysql -h <MYSQL_HOST> -P <MYSQL_PORT> -u <MYSQL_USER> -p<MYSQL_PASSWORD> < src/backend/src/main/resources/database/01-create-schema.sql
mysql -h <MYSQL_HOST> -P <MYSQL_PORT> -u <MYSQL_USER> -p<MYSQL_PASSWORD> chef_pro < src/backend/src/main/resources/database/02-seeds.sql
```

---

## 6. Desplegar el backend Java

### Opción A: Despliegue desde GitHub (Recomendado)

1. En el proyecto de Railway, haz clic en **"+ New"** → **"GitHub Repo"**
2. Selecciona el repositorio `varelaiglesiascarmen/ChefPro`
3. Railway detectará automáticamente el `railway.toml` y usará el `Dockerfile`
4. El primer despliegue empezará automáticamente

### Opción B: Despliegue con Railway CLI (Manual)

```bash
# 1. Instalar el CLI de Railway
npm install -g @railway/cli

# 2. Iniciar sesión
railway login

# 3. Vincular al proyecto (desde la raíz del repo)
railway link

# 4. Desplegar
railway up
```

---

## 7. Configurar variables de entorno

Este es el paso **MÁS IMPORTANTE**. Las variables de entorno conectan tu backend con la base de datos.

1. En Railway, haz clic en tu servicio del **backend** (no el de MySQL)
2. Ve a la pestaña **"Variables"**
3. Haz clic en **"+ New Variable"** y añade las siguientes:

| Variable                    | Valor                                                                  | Descripción                               |
|-----------------------------|------------------------------------------------------------------------|-------------------------------------------|
| `SPRING_DATASOURCE_URL`    | `jdbc:mysql://${{MySQL.MYSQL_HOST}}:${{MySQL.MYSQL_PORT}}/chef_pro`   | URL de conexión a MySQL (usa referencias) |
| `SPRING_DATASOURCE_USERNAME`| `${{MySQL.MYSQL_USER}}`                                               | Usuario de la BD                          |
| `SPRING_DATASOURCE_PASSWORD`| `${{MySQL.MYSQL_PASSWORD}}`                                           | Contraseña de la BD                       |
| `SPRING_JPA_SHOW_SQL`      | `false`                                                                | Desactivar logs SQL en producción         |
| `LOG_LEVEL_SQL`            | `WARN`                                                                 | Menos logs en producción                  |
| `LOG_LEVEL_BIND`           | `WARN`                                                                 | Menos logs en producción                  |

> **💡 Concepto:** La sintaxis `${{MySQL.VARIABLE}}` es una **referencia entre servicios** de Railway. Así, si la BD cambia de host, tu backend se actualiza automáticamente.

> **⚠️ IMPORTANTE:** Fíjate que en `application.yml` usamos `${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/chef_pro}`. La parte después de los `:` es el valor **por defecto** (para desarrollo local). En Railway, la variable de entorno sobreescribe ese valor.

---

## 8. Conectar GitHub Actions (CI/CD)

Para que el workflow automático funcione, necesitas un **Token de Railway**:

### Obtener el token

1. Ve a [https://railway.com/account/tokens](https://railway.com/account/tokens)
2. Haz clic en **"Create Token"**
3. Nómbralo `github-actions-deploy`
4. Copia el token generado

### Configurar el secreto en GitHub

1. Ve a tu repositorio en GitHub: `github.com/varelaiglesiascarmen/ChefPro`
2. **Settings** → **Secrets and variables** → **Actions**
3. Haz clic en **"New repository secret"**
4. Nombre: `RAILWAY_TOKEN`
5. Valor: *(pega el token que copiaste)*
6. Haz clic en **"Add secret"**

### Cómo funciona el workflow

```
Push a main (con cambios en src/backend/) 
    → GitHub Actions se activa
        → Job 1: Compila y ejecuta tests con Maven
        → Job 2: Si los tests pasan, despliega en Railway
```

El archivo `.github/workflows/deploy-backend.yml` define todo este proceso.

---

## 9. Verificar el despliegue

Una vez desplegado, Railway te asigna un dominio público automáticamente.

1. En Railway, haz clic en tu servicio del backend
2. Ve a **"Settings"** → **"Networking"** → **"Generate Domain"**
3. Railway te dará una URL como: `chefpro-backend-production.up.railway.app`

### Probar que funciona

```bash
# Health check
curl https://TU-DOMINIO.up.railway.app/api/auth/health

# Si devuelve respuesta, ¡tu API está desplegada! 🎉
```

### Probar con Swagger

Abre en el navegador:
```
https://TU-DOMINIO.up.railway.app/swagger-ui/index.html
```

---

## 10. Conceptos clave aprendidos

| Concepto            | Descripción                                                                      |
|---------------------|----------------------------------------------------------------------------------|
| **PaaS**            | Platform as a Service. Railway gestiona servidores por ti.                        |
| **Docker**          | Empaqueta tu app + dependencias en un contenedor reproducible.                   |
| **Multi-stage build** | Dockerfile con dos fases: compilar (pesado) + ejecutar (ligero).              |
| **Variables de entorno** | Configuración externa que cambia entre entornos (local vs producción).     |
| **CI/CD**           | Integración y Despliegue Continuos. Automatiza build + deploy.                   |
| **GitHub Actions**  | Plataforma de CI/CD integrada en GitHub.                                         |
| **Health Check**    | Endpoint que Railway consulta para verificar que la app funciona.                |
| **Secretos**        | Variables sensibles (tokens, passwords) almacenadas de forma segura en GitHub.   |

---

## 11. Solución de problemas

### La app no arranca en Railway

1. En Railway, ve al servicio → pestaña **"Deployments"** → haz clic en el despliegue fallido
2. Revisa los **logs** (pestaña "Logs")
3. Errores comunes:
   - `Communications link failure` → Las variables de conexión a MySQL son incorrectas
   - `Access denied` → Usuario/contraseña de BD incorrectos
   - `Port already in use` → No debes hardcodear el puerto; usa `${PORT:8080}`

### Los tests fallan en GitHub Actions

1. Ve a la pestaña **Actions** en tu repo de GitHub
2. Haz clic en el workflow fallido → revisa los logs del paso que falló
3. Si el test necesita base de datos y falla, es normal (los tests de integración necesitan MySQL)

### No puedo conectar a la BD desde local

Si quieres conectarte a la BD de Railway desde tu máquina (ej. para depurar):
1. En Railway, servicio MySQL → **"Variables"**
2. Copia la `MYSQL_URL` pública
3. Úsala en tu cliente MySQL favorito (DBeaver, DataGrip, MySQL Workbench...)

---

## Arquitectura del despliegue

```
┌─────────────────────────────────────────────────┐
│                   RAILWAY                        │
│                                                  │
│  ┌──────────────────┐   ┌──────────────────┐    │
│  │  Backend (Java)   │──▶│   MySQL 8.0      │    │
│  │  Spring Boot 4.0  │   │   chef_pro DB    │    │
│  │  Puerto: $PORT    │   │   Puerto: 3306   │    │
│  └──────────────────┘   └──────────────────┘    │
│         ▲                                        │
└─────────┼────────────────────────────────────────┘
          │ HTTPS
          │
┌─────────┴──────────┐
│   Frontend Angular  │  (tu navegador / futuro despliegue)
│   localhost:4200    │
└────────────────────┘
```

---

## Archivos creados/modificados en esta rama

| Archivo                                          | Qué hace                                                    |
|--------------------------------------------------|-------------------------------------------------------------|
| `src/backend/Dockerfile`                         | Define cómo construir la imagen Docker del backend          |
| `src/backend/.dockerignore`                      | Excluye archivos innecesarios del build Docker              |
| `railway.toml`                                   | Configuración de Railway (cómo construir y desplegar)       |
| `.github/workflows/deploy-backend.yml`           | Workflow de CI/CD para compilar, testear y desplegar        |
| `src/backend/src/main/resources/application.yml` | Variables de entorno dinámicas (compatible Railway + local)  |
| `docs/RAILWAY-DEPLOY.md`                         | Esta guía que estás leyendo                                 |
