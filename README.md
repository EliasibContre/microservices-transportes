# API de órdenes de transporte

API REST para gestionar órdenes de transporte, conductores y asignaciones. La solución usa Java 17, Spring Boot 3.5, PostgreSQL y JWT. Se ejecuta con Docker Compose y separa cada dominio en un servicio.

El examen solicita órdenes, conductores, asignaciones, carga de archivos, seguridad, pruebas, documentación y contenedores. La separación en microservicios, Eureka y el gateway son decisiones de implementación de este proyecto.

## Servicios y puertos

| Servicio | Función | Puerto en el equipo |
| --- | --- | --- |
| `discovery-server` | Registro y descubrimiento con Eureka | 8761 |
| `api-gateway` | Entrada a la API | 8090 |
| `auth-service` | Emisión de tokens JWT | 8081 |
| `order-service` | Órdenes y filtros | 8082 |
| `driver-service` | Conductores | 8083 |
| `assignment-service` | Asignaciones y archivos | 8084 |
| `postgres` | PostgreSQL 17 | 5434 |

Las peticiones de negocio se hacen normalmente a `http://localhost:8090`. Eureka se consulta en `http://localhost:8761`. Los puertos 8081–8084 están publicados también para consultar Swagger o diagnosticar un servicio directamente.

Hay tres bases lógicas, `order_db`, `driver_db` y `assignment_db`, dentro del mismo contenedor PostgreSQL. Cada servicio usa su propia base; las referencias entre servicios se guardan como UUID, sin claves foráneas entre bases.

## Requisitos para ejecutar

- Docker Desktop con Docker Compose disponible y el motor de Docker iniciado.
- Git para clonar el repositorio, si todavía no se tiene una copia local.
- Puertos 5434, 8090, 8761 y 8081–8084 libres en el equipo.

No se requiere instalar Java, Maven ni PostgreSQL para levantar los contenedores: el `Dockerfile` compila cada aplicación con Maven y usa Java 17 para ejecutarla. Para ejecutar las pruebas desde la terminal del equipo sí se necesitan Java 17 y Maven.

## 1. Obtener el proyecto

Clona el repositorio o descarga su contenido y abre una terminal en la **raíz**, donde están `compose.yaml`, `Dockerfile`, `.env.example` y las carpetas de los servicios.

Ejemplo si usas Git:

```bash
git clone <URL_DEL_REPOSITORIO>
cd microservices-transportes
```

Verifica Docker antes de continuar:

```bash
docker --version
docker compose version
```

## 2. Preparar las variables de entorno

Copia `.env.example` como `.env` **en la raíz del proyecto**. En PowerShell:

```powershell
Copy-Item .env.example .env
```

En macOS o Linux:

```bash
cp .env.example .env
```

Abre `.env` y sustituye los valores de ejemplo:

```dotenv
DB_PASSWORD=<CONTRASENA_DE_POSTGRES>
API_USERNAME=admin
API_PASSWORD=<CONTRASENA_PARA_PEDIR_EL_TOKEN>
JWT_SECRET_BASE64=<CLAVE_ALEATORIA_EN_BASE64>
```

`DB_PASSWORD` configura el usuario PostgreSQL `transport_user` y la conexión de los tres servicios de datos. `API_USERNAME` y `API_PASSWORD` son las credenciales Basic Auth de `POST /api/auth/token`; **no son las credenciales de PostgreSQL**. `JWT_SECRET_BASE64` debe contener, después de decodificar Base64, al menos 32 bytes. La misma clave se entrega al emisor y a los servicios que validan tokens.

Para generar una clave de 32 bytes en PowerShell:

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

En macOS o Linux, si tienes OpenSSL:

```bash
openssl rand -base64 32
```

Copia la salida completa en `JWT_SECRET_BASE64`, en una sola línea. Usa contraseñas propias; no conserves los textos `<...>` del ejemplo. `.env` está excluido de Git y no debe subirse al repositorio.

> Para la ejecución con Compose solo hacen falta esas cuatro variables. El `compose.yaml` utiliza los nombres internos `postgres` y `discovery-server`; no añadas las URL `localhost` de una ejecución desde el IDE al `.env` que emplea Compose.

## 3. Construir y levantar todo

Desde la raíz del proyecto:

```bash
docker compose up --build -d
```

La primera compilación puede tardar porque Docker descarga las imágenes y Maven resuelve dependencias. Compose levanta PostgreSQL, Eureka, los cuatro servicios de dominio/autenticación y el gateway.

Comprueba el estado:

```bash
docker compose ps
```

Abre `http://localhost:8761` y confirma que `AUTH-SERVICE`, `ORDER-SERVICE`, `DRIVER-SERVICE`, `ASSIGNMENT-SERVICE` y `API-GATEWAY` aparecen registrados. El registro puede tardar unos segundos después de que un contenedor se marque como iniciado.

Para seguir los logs:

```bash
docker compose logs -f
```

Detén la vista de logs con Ctrl+C; esto no detiene los contenedores. Para un servicio concreto, por ejemplo:

```bash
docker compose logs -f assignment-service
```

Los servicios de negocio y autenticación escriben logs JSON con campos como `event`, identificadores y estado. No publiques tokens ni contraseñas en logs o capturas.

## 4. Inicialización de PostgreSQL

Con un volumen **nuevo**, PostgreSQL ejecuta automáticamente los archivos de `database/init/`: crea las tres bases y sus tablas. No se usa Flyway ni hace falta ejecutar `tables.sql` manualmente en el arranque normal. Las aplicaciones usan `spring.jpa.hibernate.ddl-auto=validate`: comprueban que las tablas existan, pero no las crean ni las actualizan.

Para comprobar las tablas desde la raíz:

```bash
docker compose exec postgres psql -U transport_user -d order_db -c '\dt'
docker compose exec postgres psql -U transport_user -d driver_db -c '\dt'
docker compose exec postgres psql -U transport_user -d assignment_db -c '\dt'
```

Se esperan `orders` en `order_db`, `drivers` en `driver_db` y `assignments` y `assignment_files` en `assignment_db`.

Los scripts de inicialización solo se ejecutan cuando PostgreSQL crea su directorio de datos por primera vez. Si ya existe el volumen, editar los SQL y reiniciar los contenedores **no** vuelve a aplicarlos.

## 5. Obtener un token en Postman

1. Crea una petición **POST** a `http://localhost:8090/api/auth/token`.
2. En **Authorization**, selecciona **Basic Auth**. Usa `API_USERNAME` y `API_PASSWORD` de `.env`.
3. Envía la petición. No necesita cuerpo JSON.
4. Copia el valor `accessToken` de la respuesta:

```json
{
  "accessToken": "<TOKEN_JWT>"
}
```

El token dura una hora. Para el resto de las peticiones, selecciona **Bearer Token** en Postman y pega únicamente el valor de `accessToken`. Equivale a enviar `Authorization: Bearer <TOKEN_JWT>`. El usuario configurado tiene el rol `ADMIN`.

Al escribir una URL en el navegador se realiza una petición GET; la ruta del token requiere **POST** y Basic Auth, por lo que debes usar Postman u otro cliente HTTP.

## 6. Probar el flujo completo

Usa siempre el gateway en `http://localhost:8090` y el Bearer Token obtenido antes. Para las peticiones con JSON, elige **Body → raw → JSON** en Postman.

### Crear y consultar una orden

**POST** `http://localhost:8090/api/orders`

```json
{
  "origin": "Ciudad de México",
  "destination": "Puebla"
}
```

La respuesta `201 Created` contiene el `id` de la orden y el estado inicial `CREATED`. Guarda ese UUID como `ORDER_ID`.

**GET** `http://localhost:8090/api/orders/<ORDER_ID>` consulta esa orden.

**GET** `http://localhost:8090/api/orders` lista todas. Los filtros opcionales son `status`, `date` (formato `YYYY-MM-DD`), `origin` y `destination`. Puedes combinarlos, por ejemplo:

```text
http://localhost:8090/api/orders?status=CREATED&date=2026-10-01
```

El filtro de fecha usa la fecha de creación en UTC. `origin` y `destination` comparan el texto completo sin distinguir mayúsculas; si contienen espacios o caracteres especiales, deja que Postman codifique los parámetros.

Para cambiar el estado, usa **PATCH** `http://localhost:8090/api/orders/<ORDER_ID>/status`:

```json
{
  "status": "IN_TRANSIT"
}
```

Las transiciones implementadas son `CREATED → IN_TRANSIT`, `CREATED → CANCELLED`, `IN_TRANSIT → DELIVERED` e `IN_TRANSIT → CANCELLED`. `DELIVERED` y `CANCELLED` son terminales. Si quieres probar la asignación, **mantén la orden en `CREATED`** hasta terminar esa prueba.

### Crear y consultar conductores

**POST** `http://localhost:8090/api/drivers`

```json
{
  "name": "Ana López",
  "licenseNumber": "LIC-12345"
}
```

La respuesta `201 Created` contiene el `id` y `active: true`. Guarda el UUID como `DRIVER_ID`.

**GET** `http://localhost:8090/api/drivers/active` lista conductores activos. **GET** `http://localhost:8090/api/drivers/<DRIVER_ID>` consulta un conductor por UUID. El proyecto actual no expone un endpoint para cambiar `active`.

### Asignar un conductor

**POST** `http://localhost:8090/api/assignments`

```json
{
  "orderId": "<ORDER_ID>",
  "driverId": "<DRIVER_ID>"
}
```

Sustituye los marcadores por UUID reales, conservando las comillas. La respuesta `201 Created` incluye el `id` de la asignación: guárdalo como `ASSIGNMENT_ID`. Solo se acepta un conductor activo y una orden en `CREATED`; una orden no puede tener más de una asignación.

### Adjuntar PDF o imagen

**POST** `http://localhost:8090/api/assignments/<ASSIGNMENT_ID>/files`

En Postman, abre **Body → form-data**. Agrega una fila con clave `file`, cambia su tipo de **Text** a **File** y selecciona un archivo. No escribas manualmente el `Content-Type` de la petición: Postman debe construir el `multipart/form-data` con su límite.

Se admiten PDF (`.pdf`), PNG (`.png`) y JPEG (`.jpg` o `.jpeg`). El servicio comprueba el nombre, el tipo MIME y la cabecera real del archivo. La respuesta `201 Created` devuelve los metadatos del archivo, incluido su `id`. El contenido se guarda como `BYTEA` en `assignment_db`. Actualmente no hay endpoint para descargarlo.

## Swagger / OpenAPI

Cada servicio expone su propio Swagger UI directamente:

- Autenticación: `http://localhost:8081/swagger-ui/index.html`
- Órdenes: `http://localhost:8082/swagger-ui/index.html`
- Conductores: `http://localhost:8083/swagger-ui/index.html`
- Asignaciones: `http://localhost:8084/swagger-ui/index.html`

El gateway actual enruta `/api/auth/**`, `/api/orders/**`, `/api/drivers/**` y `/api/assignments/**`; **no** enruta las páginas de Swagger. En Swagger de órdenes, conductores y asignaciones, pulsa **Authorize** y pega el JWT. Para obtenerlo desde Swagger de autenticación, usa Basic Auth en el botón **Authorize** y ejecuta `POST /api/auth/token`.

## Pruebas unitarias

Con Java 17 y Maven instalados en el equipo, ejecuta desde la raíz:

```bash
mvn -f auth-service/pom.xml test
mvn -f order-service/pom.xml test
mvn -f driver-service/pom.xml test
mvn -f assignment-service/pom.xml test
```

Las pruebas usan JUnit 5 y Mockito. El `Dockerfile` construye las imágenes con `-DskipTests`; ejecuta las pruebas por separado cuando quieras validarlas.

## Detener y reiniciar

Para detener y retirar los contenedores sin borrar la información de PostgreSQL:

```bash
docker compose down
```

Para volver a iniciarlos:

```bash
docker compose up -d
```

Si cambias código Java y quieres reconstruir las imágenes:

```bash
docker compose up --build -d
```

**Solo si deseas borrar todos los datos y recrear las bases desde los scripts SQL**, usa:

```bash
docker compose down -v
docker compose up --build -d
```

`down -v` elimina el volumen de PostgreSQL de este Compose y todos los registros guardados allí. No lo uses para una parada habitual.

## Problemas frecuentes

| Síntoma | Qué comprobar |
| --- | --- |
| `docker compose` indica que falta una variable | Existe `.env` en la raíz y las cuatro variables tienen valores reales. |
| Un puerto ya está ocupado | Revisa `docker compose ps` y otros procesos que usen 5434, 8090, 8761 o 8081–8084. |
| `503 Unable to find instance` | Confirma en Eureka que el servicio de destino está registrado; espera unos segundos y consulta sus logs. |
| `401 Unauthorized` en órdenes, conductores o asignaciones | Envía `Authorization: Bearer <TOKEN_JWT>`; comprueba que no haya expirado. |
| `403 Forbidden` al pedir el token | Usa POST, Basic Auth y las credenciales `API_USERNAME`/`API_PASSWORD`. |
| Falla la conexión a PostgreSQL | Confirma que `postgres` está activo y saludable; revisa `docker compose logs postgres` y que `DB_PASSWORD` sea el mismo con el que se creó el volumen. |
| Faltan tablas | Los scripts solo se ejecutan al crear un volumen nuevo. Revisa los logs de `postgres` antes de considerar un reinicio con `down -v`. |
| Un archivo es rechazado | Comprueba que la clave multipart sea `file` y que extensión, MIME y contenido correspondan a PDF, PNG o JPEG. |

Los errores de validación y negocio se responden con detalles HTTP; los servicios registran eventos estructurados para ayudar a localizar el problema sin imprimir los archivos ni el JWT.

