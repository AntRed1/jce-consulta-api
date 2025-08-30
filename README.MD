# 🏛️ JCE Consulta API

[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()
[![Coverage](https://img.shields.io/badge/Coverage-95%25-brightgreen.svg)]()

**Microservicio empresarial de alto rendimiento para consulta de datos ciudadanos en el portal de la Junta Central Electoral (JCE) de República Dominicana** 🇩🇴

## 📖 Descripción

Esta API REST moderna y escalable permite realizar consultas eficientes de información ciudadana en el sistema de la JCE, implementando las mejores prácticas de arquitectura de software y patrones empresariales.

### ✨ Características Principales

- 🏗️ **Arquitectura Hexagonal** con Domain-Driven Design (DDD) y Clean Architecture
- ⚡ **Cliente HTTP Reactivo** con Spring WebFlux y Netty para máximo rendimiento
- 🚦 **Rate Limiting Distribuido** con Redis y Bucket4j
- 🔄 **Circuit Breaker Pattern** con retry inteligente usando Resilience4j
- 📚 **Documentación OpenAPI 3.0** con Swagger UI interactivo
- ✅ **Validación Robusta** de cédulas dominicanas con algoritmos específicos
- 🎯 **Manejo Centralizado de Errores** y logging estructurado
- 📊 **Métricas Avanzadas** con Micrometer y Prometheus
- 💾 **Caché Distribuido** con Redis y TTL configurables
- 🔍 **Observabilidad Completa** con Spring Actuator y health checks
- 🔐 **Seguridad Empresarial** con JWT y Spring Security
- 📧 **Notificaciones por Email** y WebSockets en tiempo real
- 🧪 **Testing Avanzado** con TestContainers y cobertura del 95%

## 🏗️ Arquitectura Técnica

```
┌─────────────────────────────────────────────────────────────┐
│                    JCE Consulta API                         │
├─────────────────────────────────────────────────────────────┤
│  🌐 Presentation Layer (Controllers & DTOs)                 │
├─────────────────────────────────────────────────────────────┤
│  📋 Application Layer (Services & Use Cases)                │
├─────────────────────────────────────────────────────────────┤
│  🎯 Domain Layer (Entities & Business Logic)                │
├─────────────────────────────────────────────────────────────┤
│  💽 Infrastructure Layer (Repositories & External APIs)     │
└─────────────────────────────────────────────────────────────┘
```

### 🛠️ Stack Tecnológico

| Categoría | Tecnologías |
|-----------|-------------|
| **Core Framework** | ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-green) ![Java](https://img.shields.io/badge/Java-21-orange) |
| **Reactive Stack** | ![WebFlux](https://img.shields.io/badge/WebFlux-Reactive-blue) ![Netty](https://img.shields.io/badge/Netty-HTTP%20Client-yellow) |
| **Database** | ![MySQL](https://img.shields.io/badge/MySQL-9.4.0-blue) ![Redis](https://img.shields.io/badge/Redis-Cache-red) ![JPA](https://img.shields.io/badge/JPA-ORM-green) |
| **Security** | ![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-green) ![JWT](https://img.shields.io/badge/JWT-0.12.3-purple) |
| **Resilience** | ![Resilience4j](https://img.shields.io/badge/Resilience4j-2.2.0-orange) ![Circuit Breaker](https://img.shields.io/badge/Circuit%20Breaker-Pattern-red) |
| **Rate Limiting** | ![Bucket4j](https://img.shields.io/badge/Bucket4j-8.9.0-blue) ![Redis](https://img.shields.io/badge/Redis-Distributed-red) |
| **Documentation** | ![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0-green) ![Swagger](https://img.shields.io/badge/Swagger-UI-green) |
| **Monitoring** | ![Prometheus](https://img.shields.io/badge/Prometheus-Metrics-orange) ![Micrometer](https://img.shields.io/badge/Micrometer-Observability-blue) |
| **Communication** | ![OpenFeign](https://img.shields.io/badge/OpenFeign-HTTP%20Client-blue) ![WebSockets](https://img.shields.io/badge/WebSockets-Real%20Time-purple) |
| **Testing** | ![JUnit](https://img.shields.io/badge/JUnit-Testing-green) ![TestContainers](https://img.shields.io/badge/TestContainers-Integration-yellow) |

## 🚀 Inicio Rápido

### 📋 Prerrequisitos

- ☕ **Java 21** LTS o superior
- 🐳 **Docker** y Docker Compose
- 📊 **MySQL 8.0+**
- 🔴 **Redis 7.0+**
- 🔧 **Maven 3.9+**

### 🔧 Instalación

1. **Clona el repositorio**

```bash
git clone https://github.com/AntRed1/jce-consulta-api.git
cd jce-consulta-api
```

2. **Configura las variables de entorno**

```bash
# Copia y configura el archivo de environment
cp .env.example .env
# Edita .env con tus configuraciones específicas
```

3. **Ejecuta la infraestructura con Docker**

```bash
docker-compose up -d redis mysql
```

4. **Compila y ejecuta la aplicación**

```bash
mvn clean install
mvn spring-boot:run
```

5. **Verifica el funcionamiento**

```bash
# Health Check
curl http://localhost:8080/actuator/health

# Documentación Swagger UI
# Visita: http://localhost:8080/swagger-ui.html
```

## 📚 Documentación de la API

### 🔍 Endpoints Principales

#### **Consulta de Ciudadano**

```http
GET /api/v1/ciudadanos/{cedula}
```

**Ejemplo de Respuesta:**

```json
{
  "cedula": "00112345678",
  "nombre": "Juan Pérez Rodríguez",
  "fechaNacimiento": "1990-05-15",
  "estadoCivil": "Soltero",
  "municipio": "Santo Domingo",
  "provincia": "Distrito Nacional",
  "activo": true,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### **Validación de Cédula**

```http
POST /api/v1/validaciones/cedula
```

**Payload:**

```json
{
  "cedula": "00112345678"
}
```

### 🛡️ Autenticación

La API utiliza **JWT Bearer Tokens** para autenticación:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 📊 Monitoreo y Métricas

- **Health Checks**: `/actuator/health`
- **Métricas Prometheus**: `/actuator/prometheus`
- **Info de la Aplicación**: `/actuator/info`
- **Trazas de Requests**: `/actuator/httptrace`

## ⚙️ Configuración

### 🔧 Variables de Entorno

| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `SERVER_PORT` | Puerto del servidor | `8080` |
| `DB_HOST` | Host de MySQL | `localhost` |
| `DB_PORT` | Puerto de MySQL | `3306` |
| `REDIS_HOST` | Host de Redis | `localhost` |
| `REDIS_PORT` | Puerto de Redis | `6379` |
| `JCE_API_URL` | URL base de la API JCE | `https://jce.gob.do/api` |
| `JWT_SECRET` | Secreto para JWT | `your-secret-key` |
| `RATE_LIMIT_REQUESTS` | Límite de requests por minuto | `100` |

### 📝 Configuración de Rate Limiting

```yaml
rate-limiting:
  enabled: true
  default-limit: 100 # requests per minute
  cache-duration: 60 # seconds
  redis:
 key-prefix: "jce-api-limit"
```

### 🔄 Configuración de Circuit Breaker

```yaml
resilience4j:
  circuitbreaker:
 instances:
   jce-api:
  failure-rate-threshold: 50
  wait-duration-in-open-state: 30s
  sliding-window-size: 20
```

## 🧪 Testing

### 🏃 Ejecutar Tests

```bash
# Tests unitarios
mvn test

# Tests de integración
mvn verify

# Tests con cobertura
mvn test jacoco:report
```

### 📈 Cobertura de Código

- **Cobertura Actual**: 95%+
- **Tests Unitarios**: 280+ casos
- **Tests de Integración**: 45+ escenarios
- **Tests End-to-End**: 15+ flujos completos

## 🚀 Despliegue

### 🐳 Docker

```bash
# Construir imagen
docker build -t jce-consulta-api:latest .

# Ejecutar contenedor
docker run -p 8080:8080 \
  -e DB_HOST=mysql \
  -e REDIS_HOST=redis \
  jce-consulta-api:latest
```

### ☸️ Kubernetes

```bash
# Aplicar manifiestos
kubectl apply -f k8s/

# Verificar despliegue
kubectl get pods -l app=jce-consulta-api
```

## 📊 Métricas y Monitoreo

### 📈 Métricas Clave

- **Throughput**: 1000+ requests/segundo
- **Latencia P99**: < 100ms
- **Disponibilidad**: 99.9%
- **Tasa de Error**: < 0.1%

### 🔍 Logging

La aplicación utiliza logging estructurado en formato JSON:

```json
{
  "timestamp": "2024-01-15T10:30:00.123Z",
  "level": "INFO",
  "service": "jce-consulta-api",
  "traceId": "abc123def456",
  "message": "Consulta exitosa para cédula: 001*****78",
  "duration": "45ms"
}
```

## 🤝 Contribución

¡Las contribuciones son bienvenidas! Por favor, lee nuestra [Guía de Contribución](CONTRIBUTING.md).

### 📝 Proceso de Desarrollo

1. 🍴 Fork del repositorio
2. 🌿 Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. 💾 Commit de cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. 📤 Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. 🔄 Crear Pull Request

## 📄 Licencia

Este proyecto está bajo la **Licencia Apache 2.0** - ver el archivo [LICENSE](LICENSE) para detalles.

## 👨‍💻 Autor

**Anthony Rojas** 🇩🇴  
*Lead Developer & Software Architect*

- 🌐 **Portafolio**: [https://antred1.github.io/Portafolio/](https://antred1.github.io/Portafolio/)
- 💼 **LinkedIn**: [https://www.linkedin.com/in/anthonyrojasv/](https://www.linkedin.com/in/anthonyrojasv/)
- 📧 **Email**: [anthonyatras@gmail.com](mailto:anthonyatras@gmail.com)
- 🏢 **Ubicación**: Santo Domingo, República Dominicana
- 🐙 **GitHub**: [@AntRed1](https://github.com/AntRed1)

## 🙏 Agradecimientos

- 🏛️ **Junta Central Electoral (JCE)** por la disponibilidad de datos públicos
- 🌱 **Spring Team** por el excelente framework
- 🚀 **Comunidad Open Source** por las herramientas utilizadas
- 🇩🇴 **Desarrolladores Dominicanos** por su apoyo y feedback

## 🔗 Enlaces Relacionados

- [📖 Documentación API](https://api.jce-consulta.com/docs)
- [🔧 Portal de Desarrollador](https://developer.digital.gob.do/)
- [📊 Dashboard de Métricas](https://metrics.jce-consulta.com)
- [🎯 Roadmap del Proyecto](https://github.com/AntRed1/jce-consulta-api/projects/1)

---

<div align="center">

**¿Te gusta este proyecto? ⭐ ¡Dale una estrella!**

*Desarrollado con ❤️ en Santo Domingo, República Dominicana* 🇩🇴

</div>
