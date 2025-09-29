# Products API

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?style=for-the-badge&logo=spring)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue?style=for-the-badge&logo=apache-maven)
![H2 Database](https://img.shields.io/badge/H2-Database-yellow?style=for-the-badge&logo=h2)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0-green?style=for-the-badge&logo=swagger)

## Descripción

API REST completa para la gestión de productos. Esta aplicación implementa todas las mejores prácticas de desarrollo de APIs modernas usando Spring Boot, incluyendo operaciones CRUD, búsquedas avanzadas, paginación, validaciones robustas y documentación interactiva.

## Características Principales

- **CRUD Completo**: Crear, leer, actualizar y eliminar productos
- **Búsquedas Avanzadas**: Por nombre, categoría, rango de precios y stock bajo
- **Paginación**: Soporte completo para consultas paginadas con ordenamiento
- **Validaciones**: Validaciones técnicas y de negocio robustas
- **Documentación**: Documentación interactiva con Swagger UI personalizada
- **Operaciones Batch**: Creación y actualización de múltiples productos
- **Reportes**: Estadísticas y conteos por categoría
- **Manejo de Errores**: Respuestas de error estandarizadas y descriptivas
- **Testing**: Suite completa de tests unitarios e integración
- **Datos de Prueba**: Carga automática de productos de ejemplo

## Prerrequisitos

- **Java 17** o superior
- **Maven 3.8+**
- **IDE** recomendado: IntelliJ IDEA, Eclipse o VS Code

## Instalación y Ejecución

### 1. Clonar el Repositorio
```bash
git clone https://github.com/tu-usuario/products-api.git
cd products-api
```

### 2. Compilar el Proyecto
```bash
./mvnw clean compile
```

### 3. Ejecutar los Tests
```bash
./mvnw test
```

### 4. Ejecutar la Aplicación
```bash
./mvnw spring-boot:run
```

### 5. Acceder a la Aplicación
- **API Base URL**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **H2 Console**: http://localhost:8081/h2-console
  - JDBC URL: `jdbc:h2:mem:productsdb`
  - Usuario: `sa`
  - Contraseña: (vacía)

## Endpoints Disponibles

### Gestión de Productos
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/products` | Obtener todos los productos (con paginación opcional) |
| `GET` | `/api/v1/products/{id}` | Obtener producto por ID |
| `POST` | `/api/v1/products` | Crear nuevo producto |
| `PUT` | `/api/v1/products/{id}` | Actualizar producto completo |
| `PATCH` | `/api/v1/products/{id}` | Actualizar producto parcial |
| `DELETE` | `/api/v1/products/{id}` | Eliminar producto (soft delete) |

### Búsquedas
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/products/category/{category}` | Buscar por categoría |
| `GET` | `/api/v1/products/search?name={name}` | Buscar por nombre |
| `GET` | `/api/v1/products/price-range?minPrice={min}&maxPrice={max}` | Buscar por rango de precios |
| `GET` | `/api/v1/products/search/exact-name?name={name}` | Búsqueda exacta por nombre |
| `GET` | `/api/v1/products/categories/multiple?categories={cat1,cat2}` | Buscar en múltiples categorías |

### Inventario
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/products/low-stock?minStock={stock}` | Productos con stock bajo |
| `PATCH` | `/api/v1/products/{id}/stock?stock={newStock}` | Actualizar stock |
| `POST` | `/api/v1/products/{id}/restock` | Restock de producto |
| `GET` | `/api/v1/products/out-of-stock` | Productos agotados |

### Operaciones Batch
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/products/batch` | Crear múltiples productos |
| `PUT` | `/api/v1/products/batch` | Actualizar múltiples productos |
| `DELETE` | `/api/v1/products/batch?ids={id1,id2}` | Eliminar múltiples productos |

### Reportes y Estadísticas
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/products/count` | Contar productos totales |
| `GET` | `/api/v1/products/count/category/{category}` | Contar por categoría |
| `GET` | `/api/v1/products/categories` | Listar todas las categorías |
| `GET` | `/api/v1/products/statistics` | Estadísticas generales |
| `GET` | `/api/v1/products/price/average` | Precio promedio |

### Validación
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/products/validate/name/{name}` | Validar disponibilidad de nombre |
| `POST` | `/api/v1/products/validate` | Validar datos de producto |

## 🧪 Ejemplos de Uso

### Crear un Producto
```bash
curl -X POST http://localhost:8081/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Dell XPS 13",
    "description": "Laptop ultrabook Dell XPS 13 con Intel i7",
    "price": 1299.99,
    "category": "Electrónicos",
    "stock": 10
  }'
```

### Buscar Productos por Categoría
```bash
curl "http://localhost:8081/api/v1/products/category/Electrónicos"
```

### Obtener Productos con Paginación
```bash
curl "http://localhost:8081/api/v1/products?page=0&size=10&sort=name&direction=asc"
```

### Buscar por Rango de Precios
```bash
curl "http://localhost:8081/api/v1/products/price-range?minPrice=100&maxPrice=500"
```

### Actualizar Stock
```bash
curl -X PATCH "http://localhost:8081/api/v1/products/1/stock?stock=25"
```

## 🏗️ Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/api/tutorial/
│   │   ├── config/           # Configuraciones (OpenAPI, DataLoader)
│   │   ├── controller/       # Controladores REST
│   │   ├── dto/             # Objetos de transferencia de datos
│   │   ├── exception/       # Excepciones personalizadas y manejo global
│   │   ├── model/           # Entidades JPA
│   │   ├── repository/      # Repositorios de datos
│   │   ├── service/         # Lógica de negocio
│   │   └── util/            # Utilidades (Mappers, Validators)
│   └── resources/
│       ├── application.yml  # Configuración de la aplicación
│       └── static/          # Recursos estáticos (CSS personalizado)
└── test/
    └── java/com/api/tutorial/  # Tests unitarios e integración
```

## 🧪 Testing

### Ejecutar Todos los Tests
```bash
./mvnw test
```

### Ejecutar Tests por Categoría
```bash
# Tests unitarios
./mvnw test -Dtest="*Test"

# Tests de integración
./mvnw test -Dtest="*IntegrationTest"

# Tests de repositorio
./mvnw test -Dtest="*RepositoryTest"
```

### Reporte de Cobertura
```bash
./mvnw jacoco:report
```

## 🔧 Tecnologías Utilizadas

### Backend
- **Spring Boot 3.5.6**: Framework principal
- **Spring Data JPA**: Persistencia de datos
- **Spring Validation**: Validaciones
- **H2 Database**: Base de datos en memoria
- **Maven**: Gestión de dependencias

### Documentación
- **SpringDoc OpenAPI 3**: Documentación automática
- **Swagger UI**: Interfaz interactiva personalizada

### Testing
- **JUnit 5**: Framework de testing
- **Mockito**: Mocking para tests unitarios
- **Spring Boot Test**: Tests de integración
- **TestContainers**: Tests con contenedores (opcional)

### Herramientas de Desarrollo
- **Lombok**: Reducción de boilerplate (opcional)
- **MapStruct**: Mapeo de objetos (futuro)
- **SLF4J + Logback**: Logging

## 📊 Métricas y Monitoreo

### Endpoints de Actuator (si está habilitado)
- **Health Check**: `/actuator/health`
- **Métricas**: `/actuator/metrics`
- **Info**: `/actuator/info`

## 🔒 Seguridad (Futuras Mejoras)

- Autenticación JWT
- Autorización basada en roles
- Rate limiting
- CORS configurado
- Validación de entrada robusta

## 🚀 Deployment (Futuras Mejoras)

### Docker
```dockerfile
FROM openjdk:21-jre-slim
COPY target/products-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: products-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: products-api
  template:
    metadata:
      labels:
        app: products-api
    spec:
      containers:
      - name: products-api
        image: products-api:latest
        ports:
        - containerPort: 8081
```

## 🤝 Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## Autor

**Juan Pablo Rua**
- Email: jpruac@eafit.edu.co

## Reconocimientos

- Comunidad Spring Boot por la excelente documentación
- Swagger/OpenAPI por las herramientas de documentación

---

**¡No olvides dar una estrella al proyecto si te fue útil!**