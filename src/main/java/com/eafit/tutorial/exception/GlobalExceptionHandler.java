package com.eafit.tutorial.exception;

import com.eafit.tutorial.dto.ApiResponse;
import com.eafit.tutorial.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la API REST.
 * Captura y maneja todas las excepciones lanzadas por los controladores,
 * proporcionando respuestas consistentes y bien formateadas.
 * 
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja errores de validación de campos en el cuerpo de la petición.
     * Se produce cuando las validaciones de Bean Validation fallan en DTOs.
     * 
     * @param ex Excepción de validación de argumentos del método
     * @return ResponseEntity con código 400 y detalles de errores de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        
        logger.warn("Error de validación de campos: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        
        // Extraer errores de validación de campos
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            logger.warn("Campo '{}': {}", fieldName, errorMessage);
        });
        
        // Extraer errores de validación de objetos
        ex.getBindingResult().getGlobalErrors().forEach(error -> {
            String objectName = error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(objectName, errorMessage);
            logger.warn("Objeto '{}': {}", objectName, errorMessage);
        });
        
        ApiResponse<ErrorResponse> response = ApiResponse.validationError(
            "Errores de validación en los datos enviados", errors);
        
        logger.warn("Retornando respuesta de error 400 con {} errores de validación", errors.size());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja errores de validación de parámetros de métodos.
     * Se produce cuando las validaciones de parámetros individuales fallan.
     * 
     * @param ex Excepción de violación de restricciones
     * @return ResponseEntity con código 400 y detalles de errores de validación
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleConstraintViolation(
            ConstraintViolationException ex) {
        
        logger.warn("Error de validación de parámetros: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        
        // Extraer errores de validación de parámetros
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
            logger.warn("Parámetro '{}': {}", fieldName, errorMessage);
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "CONSTRAINT_VIOLATION", 
            "Errores de validación en parámetros", 
            errors
        );
        
        ApiResponse<ErrorResponse> response = ApiResponse.error(
            "Errores de validación en los parámetros enviados", errorResponse);
        
        logger.warn("Retornando respuesta de error 400 con {} errores de validación de parámetros", 
                   errors.size());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja errores cuando no se encuentra un producto.
     * 
     * @param ex Excepción de producto no encontrado
     * @return ResponseEntity con código 404 y mensaje de error
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleProductNotFound(
            ProductNotFoundException ex) {
        
        logger.warn("Producto no encontrado: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "PRODUCT_NOT_FOUND", 
            ex.getMessage()
        );
        
        ApiResponse<ErrorResponse> response = ApiResponse.error(
            "El producto solicitado no fue encontrado", errorResponse);
        
        logger.warn("Retornando respuesta de error 404: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Maneja errores cuando se intenta crear un producto que ya existe.
     * 
     * @param ex Excepción de producto ya existente
     * @return ResponseEntity con código 409 y mensaje de error
     */
    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleProductAlreadyExists(
            ProductAlreadyExistsException ex) {
        
        logger.warn("Producto ya existe: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "PRODUCT_ALREADY_EXISTS", 
            ex.getMessage()
        );
        
        ApiResponse<ErrorResponse> response = ApiResponse.error(
            "El producto ya existe en el sistema", errorResponse);
        
        logger.warn("Retornando respuesta de error 409: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Maneja errores de validación personalizados.
     * 
     * @param ex Excepción de validación personalizada
     * @return ResponseEntity con código 400 y detalles de errores
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationException(
            ValidationException ex) {
        
        logger.warn("Error de validación personalizada: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "VALIDATION_ERROR", 
            ex.getMessage(),
            ex.getErrors()
        );
        
        ApiResponse<ErrorResponse> response = ApiResponse.error(
            "Errores de validación encontrados", errorResponse);
        
        logger.warn("Retornando respuesta de error 400 con {} errores personalizados", 
                   ex.getErrorCount());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja errores de argumentos ilegales.
     * 
     * @param ex Excepción de argumento ilegal
     * @return ResponseEntity con código 400 y mensaje de error
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalArgument(
            IllegalArgumentException ex) {
        
        logger.warn("Argumento ilegal: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "ILLEGAL_ARGUMENT", 
            ex.getMessage()
        );
        
        ApiResponse<ErrorResponse> response = ApiResponse.error(
            "Parámetro inválido en la solicitud", errorResponse);
        
        logger.warn("Retornando respuesta de error 400: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja errores de tipo de argumento incorrecto en parámetros de URL.
     * Se produce cuando un parámetro no puede ser convertido al tipo esperado.
     * 
     * @param ex Excepción de tipo de argumento incorrecto
     * @return ResponseEntity con código 400 y mensaje de error
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        
        String requiredTypeName = ex.getRequiredType() != null ? 
                ex.getRequiredType().getSimpleName() : "desconocido";
        
        logger.warn("Error de tipo de argumento: parámetro '{}' con valor '{}' no puede ser convertido a {}", 
                   ex.getName(), ex.getValue(), requiredTypeName);
        
        String message = String.format(
            "El parámetro '%s' con valor '%s' no es válido. Se esperaba un tipo %s", 
            ex.getName(), 
            ex.getValue(), 
            requiredTypeName
        );
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "ARGUMENT_TYPE_MISMATCH", 
            message
        );
        
        ApiResponse<ErrorResponse> response = ApiResponse.error(
            "Tipo de parámetro incorrecto", errorResponse);
        
        logger.warn("Retornando respuesta de error 400 por tipo de argumento incorrecto");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja errores de JSON malformado en el cuerpo de la petición.
     * Se produce cuando el JSON enviado no puede ser parseado.
     * 
     * @param ex Excepción de mensaje HTTP no legible
     * @return ResponseEntity con código 400 y mensaje de error
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {
        
        logger.warn("Error de JSON malformado: {}", ex.getMessage());
        
        String message = "El formato del JSON enviado es incorrecto. Verifique la sintaxis y estructura del JSON.";
        
        // Intentar extraer información más específica del error
        if (ex.getCause() != null) {
            String causeMessage = ex.getCause().getMessage();
            if (causeMessage != null) {
                if (causeMessage.contains("Unexpected character")) {
                    message = "JSON contiene caracteres inesperados. Verifique la sintaxis.";
                } else if (causeMessage.contains("Unexpected end-of-input")) {
                    message = "JSON incompleto. El mensaje termina inesperadamente.";
                } else if (causeMessage.contains("Cannot deserialize")) {
                    message = "Error en la estructura del JSON. Verifique los tipos de datos.";
                }
            }
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "JSON_PARSE_ERROR", 
            message
        );
        
        ApiResponse<ErrorResponse> response = ApiResponse.error(
            "Formato de JSON inválido", errorResponse);
        
        logger.warn("Retornando respuesta de error 400 por JSON malformado");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja errores internos del servidor no capturados por otros handlers.
     * 
     * @param ex Excepción general
     * @return ResponseEntity con código 500 y mensaje de error genérico
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleGenericException(Exception ex) {
        
        // Log completo con stack trace para debugging
        logger.error("Error interno del servidor: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "INTERNAL_SERVER_ERROR", 
            "Ha ocurrido un error interno en el servidor"
        );
        
        ApiResponse<ErrorResponse> response = ApiResponse.error(
            "Error interno del servidor", errorResponse);
        
        logger.error("Retornando respuesta de error 500 para excepción: {} - {}", 
                    ex.getClass().getSimpleName(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Maneja errores de tipo RuntimeException no específicos.
     * 
     * @param ex Excepción de tiempo de ejecución
     * @return ResponseEntity con código 500 y mensaje de error
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleRuntimeException(RuntimeException ex) {
        
        // Log completo con stack trace para debugging
        logger.error("Error de tiempo de ejecución: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "RUNTIME_ERROR", 
            "Error de tiempo de ejecución en el servidor"
        );
        
        ApiResponse<ErrorResponse> response = ApiResponse.error(
            "Error de tiempo de ejecución", errorResponse);
        
        logger.error("Retornando respuesta de error 500 para RuntimeException: {} - {}", 
                    ex.getClass().getSimpleName(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}