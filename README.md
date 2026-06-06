# Xclusive Barber — CRM API

CRM inteligente para barbería con autenticación JWT, agendamiento de citas, gestión de lealtad e integración con Gemini 2.0 Flash para asistente de chatbot.

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3 |
| Seguridad | Spring Security + JWT (jjwt 0.12.3) |
| Base de datos | PostgreSQL (Supabase) |
| ORM | Spring Data JPA / Hibernate |
| IA | Google Gemini 2.0 Flash |
| Documentación | Springdoc OpenAPI 3 (Swagger) |
| Deploy | Render (backend) · Supabase (DB) · Vercel (frontend) |

## Setup local

### 1. Clonar el repositorio

```bash
git clone https://github.com/julio96correa/CRM_barberia_api.git
cd CRM_barberia_api
```

### 2. Configurar variables de entorno

Copia el archivo de ejemplo y completa los valores:

```bash
cp .env.example .env
```

Variables requeridas en `.env`:

```
DB_URL=jdbc:postgresql://<host>:<port>/<database>
DB_USERNAME=postgres
DB_PASSWORD=tu_password
JWT_SECRET=clave-secreta-minimo-32-caracteres
GEMINI_API_KEY=tu_api_key_de_google
SPRING_PROFILES_ACTIVE=dev
```

### 3. Ejecutar en modo desarrollo

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080/api`  
Swagger UI: `http://localhost:8080/api/swagger-ui.html`

### 4. Ejecutar tests

```bash
mvn test
```

### 5. Schema de base de datos

El archivo `sql/schema.sql` contiene el DDL completo con enums, tablas, constraints, índices y datos semilla. Ejecutarlo en tu instancia de PostgreSQL antes del primer arranque en producción.

## Endpoints principales

### Auth (público)
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/auth/login` | Login → retorna JWT |
| POST | `/api/auth/register/client` | Registro de cliente |
| POST | `/api/auth/register/barber` | Registro de barbero (ADMIN) |

### Citas
| Método | Ruta | Roles | Descripción |
|--------|------|-------|-------------|
| POST | `/api/appointments` | ADMIN, CLIENT | Crear cita |
| GET | `/api/appointments?date=` | ADMIN, BARBER | Citas por fecha |
| GET | `/api/appointments/week?weekStart=` | ADMIN, BARBER | Citas de la semana |
| GET | `/api/appointments/my` | CLIENT | Mis citas pendientes |
| PUT | `/api/appointments/{id}/status` | ADMIN, BARBER | Cambiar estado |
| DELETE | `/api/appointments/{id}` | ADMIN, CLIENT | Cancelar (solo PENDING) |

### Barberos y servicios
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/barbers` | Listar barberos |
| GET | `/api/barbers/{id}/availability?date=` | Slots disponibles |
| GET | `/api/services` | Servicios activos |

### Lealtad
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/loyalty/{clientId}/balance` | Saldo de puntos y tier |
| POST | `/api/loyalty/{clientId}/redeem` | Canjear puntos |
| GET | `/api/loyalty/{clientId}/transactions` | Historial de transacciones |

### Dashboard (ADMIN/BARBER)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/dashboard/summary` | KPIs generales |
| GET | `/api/dashboard/today` | Citas del día |
| GET | `/api/dashboard/top-services` | Top 5 servicios (ADMIN) |
| GET | `/api/dashboard/inactive-clients` | Clientes inactivos +21 días |

### Chatbot (público)
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/chatbot/message` | Enviar mensaje al asistente IA |
| POST | `/api/chatbot/confirm` | Confirmar cita sugerida por IA |

## Estructura del proyecto

```
src/main/java/com/xclusive/barber/
├── chatbot/          # GeminiService + ChatbotController
├── config/           # SecurityConfig, CorsConfig, AppConfig
├── controller/       # REST controllers
├── dto/              # DTOs (request/response)
├── entity/           # JPA entities
├── enums/            # Role, ClientTier, AppointmentStatus
├── exception/        # Custom exceptions
├── repository/       # Spring Data JPA repositories
├── security/         # JWT filter, provider, UserDetailsService
└── service/          # Business logic
```

## Deploy en Render

El archivo `render.yaml` en la raíz configura el despliegue automático. Las variables de entorno deben configurarse manualmente en el dashboard de Render (marcadas como `sync: false`).
