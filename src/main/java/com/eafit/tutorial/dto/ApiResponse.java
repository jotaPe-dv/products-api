package com.eafit.tutorial.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Respuesta estándar de la API que envuelve todos los datos de respuesta.
 * Proporciona un formato consistente para todas las respuestas de la API.
 * 
 * @param <T> Tipo de datos contenidos en la respuesta
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
@Schema(description = "Respuesta estándar de la API")
public class ApiResponse<T> {

    @Schema(description = "Indica si la operación fue exitosa", 
            example = "true")
    private boolean success;

    @Schema(description = "Mensaje descriptivo de la respuesta", 
            example = "Operación completada exitosamente")
    private String message;

    @Schema(description = "Datos de la respuesta")
    private T data;

    @Schema(description = "Información de error (solo presente si success = false)")
    private ErrorResponse error;

    @Schema(description = "Errores de validación (solo presente en errores de validación)")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> errors;

    @Schema(description = "Fecha y hora de la respuesta", 
            example = "2025-09-29 14:30:15")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "Código de estado HTTP", 
            example = "200")
    private int statusCode;

    /**
     * Constructor por defecto.
     */
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor completo.
     * 
     * @param success Indica si la operación fue exitosa
     * @param message Mensaje descriptivo
     * @param data Datos de la respuesta
     * @param error Información de error
     */
    public ApiResponse(boolean success, String message, T data, ErrorResponse error) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
        this.errors = null;
        this.timestamp = LocalDateTime.now();
        this.statusCode = success ? 200 : 500;
    }

    /**
     * Constructor para errores de validación.
     * 
     * @param success Indica si la operación fue exitosa
     * @param message Mensaje descriptivo
     * @param data Datos de la respuesta
     * @param error Información de error
     * @param errors Errores de validación
     */
    public ApiResponse(boolean success, String message, T data, ErrorResponse error, Map<String, String> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
        this.statusCode = success ? 200 : 500;
    }

    // Métodos estáticos para crear respuestas

    /**
     * Crea una respuesta exitosa con datos.
     * 
     * @param data Datos de la respuesta
     * @param <T> Tipo de datos
     * @return ApiResponse exitosa
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operación completada exitosamente", data, null);
    }

    /**
     * Crea una respuesta exitosa con datos y mensaje personalizado.
     * 
     * @param data Datos de la respuesta
     * @param message Mensaje personalizado
     * @param <T> Tipo de datos
     * @return ApiResponse exitosa
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, null);
    }

    /**
     * Crea una respuesta de error.
     * 
     * @param error Información del error
     * @param <T> Tipo de datos
     * @return ApiResponse de error
     */
    public static <T> ApiResponse<T> error(ErrorResponse error) {
        return new ApiResponse<>(false, error.getMessage(), null, error);
    }

    /**
     * Crea una respuesta de error con mensaje personalizado.
     * 
     * @param message Mensaje de error
     * @param error Información del error
     * @param <T> Tipo de datos
     * @return ApiResponse de error
     */
    public static <T> ApiResponse<T> error(String message, ErrorResponse error) {
        return new ApiResponse<>(false, message, null, error);
    }

    /**
     * Crea una respuesta de error solo con mensaje.
     * 
     * @param message Mensaje de error
     * @param <T> Tipo de datos
     * @return ApiResponse de error
     */
    public static <T> ApiResponse<T> error(String message) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(message);
        errorResponse.setTimestamp(LocalDateTime.now());
        ApiResponse<T> response = new ApiResponse<>(false, message, null, errorResponse);
        response.setStatusCode(500);
        return response;
    }

    /**
     * Crea una respuesta de error con mensaje y código de estado.
     * 
     * @param message Mensaje de error
     * @param statusCode Código de estado HTTP
     * @param <T> Tipo de datos
     * @return ApiResponse de error
     */
    public static <T> ApiResponse<T> error(String message, int statusCode) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(message);
        errorResponse.setTimestamp(LocalDateTime.now());
        ApiResponse<T> response = new ApiResponse<>(false, message, null, errorResponse);
        response.setStatusCode(statusCode);
        return response;
    }

    /**
     * Crea una respuesta de error de validación con errores de campo.
     * 
     * @param message Mensaje de error
     * @param errors Mapa de errores de validación por campo
     * @param <T> Tipo de datos
     * @return ApiResponse de error de validación
     */
    public static <T> ApiResponse<T> validationError(String message, Map<String, String> errors) {
        ErrorResponse errorResponse = ErrorResponse.validationError(errors);
        ApiResponse<T> response = new ApiResponse<>(false, message, null, errorResponse, errors);
        response.setStatusCode(400);
        return response;
    }

    // Getters y Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ErrorResponse getError() {
        return error;
    }

    public void setError(ErrorResponse error) {
        this.error = error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", error=" + error +
                ", errors=" + errors +
                ", timestamp=" + timestamp +
                ", statusCode=" + statusCode +
                '}';
    }
}