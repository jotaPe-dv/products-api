package com.eafit.tutorial.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO para respuestas de error estándar de la API.
 * Proporciona un formato consistente para todos los errores devueltos por la API.
 * 
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
@Schema(description = "Respuesta estándar para errores de la API")
public class ErrorResponse {

    @Schema(description = "Código de error específico para identificar el tipo de error", 
            example = "PRODUCT_NOT_FOUND", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String errorCode;

    @Schema(description = "Mensaje descriptivo del error", 
            example = "Producto con ID 123 no encontrado", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(description = "Detalles adicionales del error, como errores de validación por campo", 
            example = "{\"name\": \"El nombre es obligatorio\", \"price\": \"El precio debe ser positivo\"}")
    private Map<String, String> details;

    @Schema(description = "Fecha y hora cuando ocurrió el error", 
            example = "2025-09-29 14:30:15", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Constructor por defecto.
     * Inicializa timestamp con la hora actual.
     */
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
        this.details = new HashMap<>();
    }

    /**
     * Constructor completo con todos los parámetros.
     * 
     * @param errorCode Código específico del error
     * @param message Mensaje descriptivo del error
     * @param details Mapa con detalles adicionales del error
     * @param timestamp Fecha y hora del error
     */
    public ErrorResponse(String errorCode, String message, Map<String, String> details, LocalDateTime timestamp) {
        this.errorCode = errorCode;
        this.message = message;
        this.details = details != null ? new HashMap<>(details) : new HashMap<>();
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    /**
     * Constructor con código de error y mensaje.
     * Inicializa timestamp con la hora actual y details como mapa vacío.
     * 
     * @param errorCode Código específico del error
     * @param message Mensaje descriptivo del error
     */
    public ErrorResponse(String errorCode, String message) {
        this(errorCode, message, null, LocalDateTime.now());
    }

    /**
     * Constructor con código de error, mensaje y detalles.
     * Inicializa timestamp con la hora actual.
     * 
     * @param errorCode Código específico del error
     * @param message Mensaje descriptivo del error
     * @param details Mapa con detalles adicionales del error
     */
    public ErrorResponse(String errorCode, String message, Map<String, String> details) {
        this(errorCode, message, details, LocalDateTime.now());
    }

    // Getters y Setters

    /**
     * Obtiene el código de error.
     * @return Código de error
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Establece el código de error.
     * @param errorCode Código de error
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Obtiene el mensaje de error.
     * @return Mensaje de error
     */
    public String getMessage() {
        return message;
    }

    /**
     * Establece el mensaje de error.
     * @param message Mensaje de error
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Obtiene los detalles adicionales del error.
     * @return Mapa con detalles del error
     */
    public Map<String, String> getDetails() {
        return details;
    }

    /**
     * Establece los detalles adicionales del error.
     * @param details Mapa con detalles del error
     */
    public void setDetails(Map<String, String> details) {
        this.details = details != null ? new HashMap<>(details) : new HashMap<>();
    }

    /**
     * Obtiene la fecha y hora del error.
     * @return Timestamp del error
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Establece la fecha y hora del error.
     * @param timestamp Timestamp del error
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Agrega un detalle específico al mapa de detalles.
     * 
     * @param key Clave del detalle
     * @param value Valor del detalle
     */
    public void addDetail(String key, String value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
    }

    /**
     * Verifica si hay detalles adicionales.
     * 
     * @return true si hay detalles, false en caso contrario
     */
    public boolean hasDetails() {
        return details != null && !details.isEmpty();
    }

    /**
     * Obtiene el número de detalles adicionales.
     * 
     * @return Número de detalles
     */
    public int getDetailsCount() {
        return details != null ? details.size() : 0;
    }

    /**
     * Método estático para crear una respuesta de error simple.
     * 
     * @param errorCode Código de error
     * @param message Mensaje de error
     * @return Nueva instancia de ErrorResponse
     */
    public static ErrorResponse of(String errorCode, String message) {
        return new ErrorResponse(errorCode, message);
    }

    /**
     * Método estático para crear una respuesta de error con detalles.
     * 
     * @param errorCode Código de error
     * @param message Mensaje de error
     * @param details Detalles adicionales
     * @return Nueva instancia de ErrorResponse
     */
    public static ErrorResponse of(String errorCode, String message, Map<String, String> details) {
        return new ErrorResponse(errorCode, message, details);
    }

    /**
     * Método estático para crear una respuesta de error para validaciones.
     * 
     * @param validationErrors Mapa con errores de validación
     * @return Nueva instancia de ErrorResponse
     */
    public static ErrorResponse validationError(Map<String, String> validationErrors) {
        return new ErrorResponse("VALIDATION_ERROR", "Errores de validación encontrados", validationErrors);
    }

    /**
     * Método estático para crear una respuesta de error para producto no encontrado.
     * 
     * @param productId ID del producto no encontrado
     * @return Nueva instancia de ErrorResponse
     */
    public static ErrorResponse productNotFound(Long productId) {
        return new ErrorResponse("PRODUCT_NOT_FOUND", "Producto con ID " + productId + " no encontrado");
    }

    /**
     * Método estático para crear una respuesta de error para producto duplicado.
     * 
     * @param productName Nombre del producto que ya existe
     * @return Nueva instancia de ErrorResponse
     */
    public static ErrorResponse productAlreadyExists(String productName) {
        return new ErrorResponse("PRODUCT_ALREADY_EXISTS", "Ya existe un producto con el nombre: " + productName);
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "errorCode='" + errorCode + '\'' +
                ", message='" + message + '\'' +
                ", details=" + details +
                ", timestamp=" + timestamp +
                '}';
    }
}