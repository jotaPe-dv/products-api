package com.eafit.tutorial.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Products API",
        version = "1.0.0",
        description = """
            # API REST de Gestión de Productos
            
            Esta API proporciona un conjunto completo de endpoints para la gestión de productos 
            en un sistema de e-commerce o inventario.
            
            ## Características Principales
            
            * **CRUD Completo**: Crear, leer, actualizar y eliminar productos
            * **Búsquedas Avanzadas**: Por nombre, categoría, rango de precios
            * **Gestión de Inventario**: Control de stock y productos con stock bajo
            * **Paginación**: Soporte completo para consultas paginadas
            * **Validaciones**: Validaciones de negocio y técnicas robustas
            * **Documentación**: Documentación interactiva con Swagger UI
            * **Manejo de Errores**: Respuestas de error estandarizadas
            
            ## Cómo Usar
            
            1. **Explorar Endpoints**: Use esta interfaz Swagger para explorar todos los endpoints disponibles
            2. **Probar Directamente**: Puede probar los endpoints directamente desde esta interfaz
            3. **Ver Ejemplos**: Cada endpoint incluye ejemplos de request y response
            4. **Manejo de Errores**: Revise los códigos de estado y mensajes de error
            
            ## Recursos Útiles
            
            * **Base de Datos H2**: [/h2-console](http://localhost:8081/h2-console) 
              (usuario: `sa`, contraseña: vacía)
            * **Documentación JSON**: [/v3/api-docs](http://localhost:8081/v3/api-docs)
            
            ## Notas Técnicas
            
            * Todos los timestamps están en formato ISO-8601
            * Los precios usan BigDecimal para precisión
            * La paginación es base-0 (primera página = 0)
            * Las búsquedas son case-insensitive
            """,
        contact = @Contact(
            name = "Juan Pablo Rua",
            email = "jpruac@eafit.edu.co"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            description = "Desarrollo Local",
            url = "http://localhost:8081"
        )
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "Autenticación JWT Bearer Token. Formato: 'Bearer {token}'"
)
@SecurityScheme(
    name = "apiKeyAuth",
    type = SecuritySchemeType.APIKEY,
    description = "Autenticación por API Key en header 'X-API-Key'"
)
public class OpenApiConfig {

    /**
     * Configuración personalizada de OpenAPI con componentes reutilizables.
     * 
     * @return Configuración completa de OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .components(new Components()
                // Respuestas reutilizables
                .addResponses("NotFound", createNotFoundResponse())
                .addResponses("BadRequest", createBadRequestResponse())
                .addResponses("InternalServerError", createInternalServerErrorResponse())
                .addResponses("Unauthorized", createUnauthorizedResponse())
                .addResponses("Forbidden", createForbiddenResponse())
                
                // Esquemas reutilizables
                .addSchemas("ValidationError", createValidationErrorSchema())
                .addSchemas("BusinessError", createBusinessErrorSchema())
            );
    }

    /**
     * Crea respuesta estándar para recursos no encontrados (404).
     */
    private ApiResponse createNotFoundResponse() {
        return new ApiResponse()
            .description("Recurso no encontrado")
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/BusinessError"))
                    .example(createErrorExample(
                        "PRODUCT_NOT_FOUND",
                        "El producto con ID 999 no fue encontrado",
                        null
                    ))
                )
            );
    }

    /**
     * Crea respuesta estándar para solicitudes incorrectas (400).
     */
    private ApiResponse createBadRequestResponse() {
        return new ApiResponse()
            .description("Solicitud incorrecta o datos inválidos")
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/ValidationError"))
                    .example(createValidationErrorExample())
                )
            );
    }

    /**
     * Crea respuesta estándar para errores internos del servidor (500).
     */
    private ApiResponse createInternalServerErrorResponse() {
        return new ApiResponse()
            .description("Error interno del servidor")
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/BusinessError"))
                    .example(createErrorExample(
                        "INTERNAL_SERVER_ERROR",
                        "Ha ocurrido un error interno. Por favor contacte al administrador.",
                        null
                    ))
                )
            );
    }

    /**
     * Crea respuesta estándar para acceso no autorizado (401).
     */
    private ApiResponse createUnauthorizedResponse() {
        return new ApiResponse()
            .description("No autorizado - Token inválido o faltante")
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/BusinessError"))
                    .example(createErrorExample(
                        "UNAUTHORIZED",
                        "Token de autenticación inválido o faltante",
                        null
                    ))
                )
            );
    }

    /**
     * Crea respuesta estándar para acceso prohibido (403).
     */
    private ApiResponse createForbiddenResponse() {
        return new ApiResponse()
            .description("Prohibido - Sin permisos suficientes")
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/BusinessError"))
                    .example(createErrorExample(
                        "FORBIDDEN",
                        "No tiene permisos suficientes para realizar esta operación",
                        null
                    ))
                )
            );
    }

    /**
     * Crea esquema para errores de validación.
     */
    private Schema<?> createValidationErrorSchema() {
        return new Schema<>()
            .type("object")
            .addProperty("success", new Schema<>().type("boolean").example(false))
            .addProperty("message", new Schema<>().type("string"))
            .addProperty("data", new Schema<>().type("object").nullable(true))
            .addProperty("timestamp", new Schema<>().type("string").format("date-time"))
            .addProperty("statusCode", new Schema<>().type("integer").example(400))
            .addProperty("errors", new Schema<>()
                .type("object")
                .additionalProperties(new Schema<>().type("string"))
                .description("Mapa de errores de validación por campo")
            );
    }

    /**
     * Crea esquema para errores de negocio.
     */
    private Schema<?> createBusinessErrorSchema() {
        return new Schema<>()
            .type("object")
            .addProperty("success", new Schema<>().type("boolean").example(false))
            .addProperty("message", new Schema<>().type("string"))
            .addProperty("data", new Schema<>().type("object").nullable(true))
            .addProperty("timestamp", new Schema<>().type("string").format("date-time"))
            .addProperty("statusCode", new Schema<>().type("integer"))
            .addProperty("errorCode", new Schema<>().type("string"))
            .addProperty("details", new Schema<>().type("object").nullable(true));
    }

    /**
     * Crea ejemplo de error genérico.
     */
    private Object createErrorExample(String errorCode, String message, Map<String, String> details) {
        Map<String, Object> example = new HashMap<>();
        example.put("success", false);
        example.put("message", message != null ? message : "Error no especificado");
        example.put("data", null);
        example.put("timestamp", "2025-09-29T10:30:00");
        example.put("statusCode", 404);
        example.put("errorCode", errorCode != null ? errorCode : "UNKNOWN_ERROR");
        example.put("details", details != null ? details : Map.of());
        return example;
    }

    /**
     * Crea ejemplo de error de validación.
     */
    private Object createValidationErrorExample() {
        Map<String, Object> example = new HashMap<>();
        example.put("success", false);
        example.put("message", "Errores de validación en los datos enviados");
        example.put("data", null);
        example.put("timestamp", "2025-09-29T10:30:00");
        example.put("statusCode", 400);
        example.put("errors", Map.of(
            "name", "El nombre del producto es obligatorio",
            "price", "El precio debe ser mayor a 0",
            "category", "La categoría es obligatoria"
        ));
        return example;
    }
}