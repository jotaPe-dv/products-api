package com.eafit.tutorial.config;

import io.swagger.v3.oas.models.examples.Example;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuración de ejemplos globales para la documentación OpenAPI.
 * Proporciona ejemplos reutilizables para diferentes tipos de respuestas
 * que pueden ser utilizados en toda la documentación de la API.
 * 
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
@Configuration
public class OpenApiExamples {

    /**
     * Crea un mapa de ejemplos globales para uso en la documentación OpenAPI.
     * 
     * @return Mapa con ejemplos predefinidos
     */
    @Bean
    public Map<String, Example> globalExamples() {
        Map<String, Example> examples = new HashMap<>();
        
        examples.put("successResponse", createSuccessExample());
        examples.put("errorResponse", createErrorExample());
        examples.put("validationErrorResponse", createValidationErrorExample());
        examples.put("paginatedResponse", createPaginatedExample());
        examples.put("productExample", createProductExample());
        examples.put("createProductExample", createCreateProductExample());
        examples.put("updateProductExample", createUpdateProductExample());
        
        return examples;
    }

    /**
     * Crea ejemplo de respuesta exitosa con un producto.
     */
    private Example createSuccessExample() {
        Example example = new Example();
        example.setSummary("Respuesta exitosa");
        example.setDescription("Ejemplo de respuesta exitosa con datos de producto");
        example.setValue(Map.of(
            "success", true,
            "message", "Producto obtenido exitosamente",
            "data", Map.of(
                "id", 1,
                "name", "Laptop HP Pavilion",
                "description", "Laptop HP Pavilion con procesador Intel i5, 8GB RAM, 256GB SSD",
                "price", 1299.99,
                "category", "Electrónicos",
                "stock", 15,
                "active", true,
                "createdAt", "2025-09-29T10:00:00",
                "updatedAt", "2025-09-29T10:00:00"
            ),
            "timestamp", "2025-09-29T10:30:00",
            "statusCode", 200
        ));
        return example;
    }

    /**
     * Crea ejemplo de respuesta de error.
     */
    private Example createErrorExample() {
        Example example = new Example();
        example.setSummary("Error de negocio");
        example.setDescription("Ejemplo de respuesta cuando ocurre un error de negocio");
        Map<String, Object> errorValue = new HashMap<>();
        errorValue.put("success", false);
        errorValue.put("message", "El producto con ID 999 no fue encontrado");
        errorValue.put("data", null);
        errorValue.put("timestamp", "2025-09-29T10:30:00");
        errorValue.put("statusCode", 404);
        errorValue.put("errorCode", "PRODUCT_NOT_FOUND");
        errorValue.put("details", Map.of());
        example.setValue(errorValue);
        return example;
    }

    /**
     * Crea ejemplo de error de validación.
     */
    private Example createValidationErrorExample() {
        Example example = new Example();
        example.setSummary("Error de validación");
        example.setDescription("Ejemplo de respuesta cuando hay errores de validación en los datos");
        Map<String, Object> validationValue = new HashMap<>();
        validationValue.put("success", false);
        validationValue.put("message", "Errores de validación en los datos enviados");
        validationValue.put("data", null);
        validationValue.put("timestamp", "2025-09-29T10:30:00");
        validationValue.put("statusCode", 400);
        validationValue.put("errors", Map.of(
            "name", "El nombre del producto es obligatorio",
            "price", "El precio debe ser mayor a 0",
            "category", "La categoría es obligatoria",
            "stock", "El stock debe ser mayor o igual a 0"
        ));
        example.setValue(validationValue);
        return example;
    }

    /**
     * Crea ejemplo de respuesta paginada.
     */
    private Example createPaginatedExample() {
        Example example = new Example();
        example.setSummary("Respuesta paginada");
        example.setDescription("Ejemplo de respuesta con datos paginados");
        example.setValue(Map.of(
            "success", true,
            "message", "Productos obtenidos exitosamente",
            "data", Map.of(
                "content", List.of(
                    Map.of(
                        "id", 1,
                        "name", "Laptop HP Pavilion",
                        "description", "Laptop HP Pavilion con procesador Intel i5",
                        "price", 1299.99,
                        "category", "Electrónicos",
                        "stock", 15,
                        "active", true,
                        "createdAt", "2025-09-29T10:00:00",
                        "updatedAt", "2025-09-29T10:00:00"
                    ),
                    Map.of(
                        "id", 2,
                        "name", "Smartphone Samsung Galaxy",
                        "description", "Smartphone Samsung Galaxy con 128GB",
                        "price", 699.99,
                        "category", "Electrónicos",
                        "stock", 25,
                        "active", true,
                        "createdAt", "2025-09-29T09:30:00",
                        "updatedAt", "2025-09-29T09:30:00"
                    )
                ),
                "page", Map.of(
                    "number", 0,
                    "size", 20,
                    "totalElements", 150,
                    "totalPages", 8,
                    "first", true,
                    "last", false,
                    "hasNext", true,
                    "hasPrevious", false
                )
            ),
            "timestamp", "2025-09-29T10:30:00",
            "statusCode", 200
        ));
        return example;
    }

    /**
     * Crea ejemplo de producto individual.
     */
    private Example createProductExample() {
        Example example = new Example();
        example.setSummary("Producto completo");
        example.setDescription("Ejemplo de un producto con todos sus campos");
        example.setValue(Map.of(
            "id", 1,
            "name", "Laptop HP Pavilion 15",
            "description", "Laptop HP Pavilion 15 con procesador Intel Core i5-1235U, 8GB de RAM DDR4, 256GB SSD, pantalla 15.6\" Full HD, Windows 11 Home",
            "price", 1299.99,
            "category", "Electrónicos",
            "stock", 15,
            "active", true,
            "createdAt", "2025-09-29T10:00:00",
            "updatedAt", "2025-09-29T10:00:00"
        ));
        return example;
    }

    /**
     * Crea ejemplo para creación de producto.
     */
    private Example createCreateProductExample() {
        Example example = new Example();
        example.setSummary("Crear producto");
        example.setDescription("Ejemplo de datos para crear un nuevo producto");
        example.setValue(Map.of(
            "name", "Auriculares Sony WH-1000XM4",
            "description", "Auriculares inalámbricos Sony WH-1000XM4 con cancelación de ruido activa, 30 horas de batería, Bluetooth 5.0",
            "price", 299.99,
            "category", "Electrónicos",
            "stock", 50
        ));
        return example;
    }

    /**
     * Crea ejemplo para actualización de producto.
     */
    private Example createUpdateProductExample() {
        Example example = new Example();
        example.setSummary("Actualizar producto");
        example.setDescription("Ejemplo de datos para actualizar un producto existente (campos opcionales)");
        example.setValue(Map.of(
            "name", "Auriculares Sony WH-1000XM5",
            "description", "Auriculares inalámbricos Sony WH-1000XM5 con cancelación de ruido mejorada, 35 horas de batería",
            "price", 349.99,
            "stock", 30
        ));
        return example;
    }
}