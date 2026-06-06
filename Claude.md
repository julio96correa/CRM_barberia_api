# CLAUDE.md — CRM Xclusive Barber

## STACK

Java 17 + Spring Boot 3.3 | PostgreSQL (Supabase) | JWT stateless | Gemini 2.0 Flash | Render

## ARQUITECTURA OBLIGATORIA (CRM-TECH-001)

- Layered estricto: Controller → Service → Repository → Entity
- NUNCA lógica de negocio en Controllers ni Repositories
- NUNCA exponer entidades JPA directamente (siempre DTOs)
- Paquetes: config | controller | service | repository | entity | dto | exception | security | enums | chatbot | utils

## FORMATO ESTÁNDAR DE RESPUESTA (TODOS los endpoints)

```json
{ "success": true, "message": "string", "data": {} }
{ "success": false, "message": "string", "errors": ["campo: detalle"] }
```

Codes HTTP: 200 OK | 201 Created | 400 Bad Request | 401 Unauthorized | 403 Forbidden | 404 Not Found | 409 Conflict | 500 Error

## BASE DE DATOS (CRM-TECH-002) — 7 tablas

- users: id(PK BIGINT), email(UNIQUE), password_hash, role(ENUM: ADMIN|BARBER|CLIENT), active, created_at
- client_profiles: id, user_id(FK), phone(UNIQUE), notes, loyalty_points(DEFAULT 0, CHECK >= 0), tier(ENUM: NEW|REGULAR|VIP), last_completed_at
- barber_profiles: id, user_id(FK), phone, specialty
- barber_schedules: id, barber_id(FK), day_of_week(0-6), start_hour(0-23), end_hour(0-23), break_start_hour, is_available(BOOLEAN)
- services: id, name(UNIQUE), description, points_value(CHECK >= 0), active(DEFAULT true)
- appointments: id, client_id(FK), barber_id(FK), service_id(FK), appointment_date(DATE), start_hour(0-23), status(ENUM: PENDING|COMPLETED|CANCELLED|NO_SHOW), created_at — UNIQUE(barber_id, appointment_date, start_hour)
- loyalty_transactions: id, client_id(FK), appointment_id(FK nullable), points_change(INT), reason, created_at

Índices: idx_appointments_barber_date | idx_appointments_client | idx_client_profiles_tier | idx_appointments_date

## AUTH (CRM-TECH-003)

- BCrypt strength 10 para passwords
- JWT: HMAC-SHA256, expiry 24h (86400000ms), campos: user_id + email + role + exp
- Header: Authorization: Bearer <token>
- Endpoints PÚBLICOS (sin token): POST /auth/login | POST /auth/register/client | POST /chatbot/message | POST /chatbot/confirm
- Solo ADMIN puede crear barberos

## REGLAS DE NEGOCIO CRÍTICAS

- Citas: bloques de 1 hora EXACTA, sin excepciones
- No solapamientos: constraint UNIQUE(barber_id, appointment_date, start_hour) en DB
- Soft delete en servicios: nunca eliminar si tiene citas históricas (campo active=false)
- Puntos: acumular SOLO en status=COMPLETED, nunca negativos
- Tier automático: recalcular tras cada cita COMPLETED
- Inactividad: alerta si last_completed_at > 21 días
- ddl-auto=update en dev | ddl-auto=validate en producción

## ENDPOINTS (CRM-TECH-004)

### /auth

- POST /auth/login → body: {email, password} → res: {token, role, userId}
- POST /auth/register/client → body: {name, email, password, phone}
- POST /auth/register/barber → ADMIN only → body: {name, email, password, phone, specialty}

### /clients (ADMIN/BARBER)

- GET /clients?page&size → paginado
- GET /clients/{id}
- PUT /clients/{id} → body: {notes?, phone?}
- GET /clients/{id}/history?page&size
- GET /clients/inactive → >21 días sin cita

### /services

- GET /services → activos, todos los roles
- GET /services/all → ADMIN, incluye inactivos
- POST /services → ADMIN → body: {name, description, pointsValue}
- PUT /services/{id} → ADMIN
- DELETE /services/{id} → ADMIN, soft delete

### /barbers

- GET /barbers → todos los roles
- GET /barbers/{id}
- PUT /barbers/{id}/schedule → body: [{dayOfWeek, startHour, endHour, breakStartHour, isAvailable}]
- PUT /barbers/{id}/day-off → body: {date: YYYY-MM-DD}
- GET /barbers/{id}/availability?date=YYYY-MM-DD → res: {availableSlots: [9,10,14]}

### /appointments

- POST /appointments → ADMIN|CLIENT → body: {clientId, barberId, serviceId, appointmentDate, startHour}
- GET /appointments?date=YYYY-MM-DD → ADMIN|BARBER
- GET /appointments/week?date=YYYY-MM-DD → ADMIN|BARBER
- GET /appointments/my?page&size → CLIENT
- PUT /appointments/{id}/status → body: {status: COMPLETED|CANCELLED|NO_SHOW}
- DELETE /appointments/{id} → ADMIN|CLIENT (solo PENDING)

### /loyalty

- GET /loyalty/{clientId}/balance
- POST /loyalty/{clientId}/redeem → body: {pointsToRedeem, reason}
- GET /loyalty/{clientId}/transactions?page&size

### /dashboard (ADMIN|BARBER)

- GET /dashboard/summary → KPIs generales
- GET /dashboard/today → citas del día
- GET /dashboard/top-services → ADMIN only
- GET /dashboard/inactive-clients

### /chatbot (PÚBLICO)

- POST /chatbot/message → body: {userMessage, conversationHistory: [{role, content}]} → res: {aiResponse, appointmentSuggestion: {barberId, barberName, date, startHour, serviceId, serviceName, clientName, clientPhone} | null}
- POST /chatbot/confirm → body: {barberId, serviceId, appointmentDate, startHour, clientName, clientPhone} → 409 si slot ocupado

## GEMINI (CRM-TECH-005)

- Modelo: gemini-2.0-flash
- Endpoint: https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent
- API key: variable de entorno GEMINI_API_KEY (nunca hardcoded)
- Timeout: 30 segundos
- Historial completo en cada request (backend stateless)
- Doble validación al confirmar cita

## VARIABLES DE ENTORNO (CRM-TECH-006)

DB_URL | DB_USERNAME | DB_PASSWORD | JWT_SECRET (min 256 bits) | GEMINI_API_KEY | SPRING_PROFILES_ACTIVE

## SWAGGER

- Disponible en /swagger-ui.html
- Documentar TODOS los endpoints con @Operation y @ApiResponse
- Incluir esquemas de seguridad Bearer

---
