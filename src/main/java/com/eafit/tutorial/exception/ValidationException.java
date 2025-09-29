package com.eafit.tutorial.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Excepción para errores de validación que pueden contener múltiples errores
 * de campo. Útil para validaciones de formularios y DTOs.
 * 
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
public class ValidationException extends RuntimeException {

    /**
     * Mapa que contiene los errores de validación.
     * La clave es el nombre del campo y el valor es el mensaje de error.
     */
    private final Map<String, String> errors;

    /**
     * Constructor con mensaje y mapa de errores.
     * 
     * @param message Mensaje general de la excepción
     * @param errors Mapa con errores específicos de campos
     */
    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors != null ? new HashMap<>(errors) : new HashMap<>();
    }

    /**
     * Constructor con mensaje solamente.
     * Inicializa un mapa de errores vacío.
     * 
     * @param message Mensaje general de la excepción
     */
    public ValidationException(String message) {
        super(message);
        this.errors = new HashMap<>();
    }

    /**
     * Constructor con mensaje, causa y mapa de errores.
     * 
     * @param message Mensaje general de la excepción
     * @param cause Causa original de la excepción
     * @param errors Mapa con errores específicos de campos
     */
    public ValidationException(String message, Throwable cause, Map<String, String> errors) {
        super(message, cause);
        this.errors = errors != null ? new HashMap<>(errors) : new HashMap<>();
    }

    /**
     * Constructor con solo mapa de errores.
     * Genera un mensaje general automáticamente.
     * 
     * @param errors Mapa con errores específicos de campos
     */
    public ValidationException(Map<String, String> errors) {
        super("Error de validación: se encontraron " + (errors != null ? errors.size() : 0) + " errores");
        this.errors = errors != null ? new HashMap<>(errors) : new HashMap<>();
    }

    /**
     * Obtiene el mapa de errores de validación.
     * 
     * @return Mapa inmutable con los errores de validación
     */
    public Map<String, String> getErrors() {
        return new HashMap<>(errors);
    }

    /**
     * Verifica si hay errores de validación.
     * 
     * @return true si hay errores, false en caso contrario
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Obtiene el número de errores de validación.
     * 
     * @return Número de errores
     */
    public int getErrorCount() {
        return errors.size();
    }

    /**
     * Agrega un error de validación.
     * 
     * @param field Nombre del campo
     * @param error Mensaje de error
     */
    public void addError(String field, String error) {
        this.errors.put(field, error);
    }

    /**
     * Obtiene el error de un campo específico.
     * 
     * @param field Nombre del campo
     * @return Mensaje de error o null si no existe
     */
    public String getError(String field) {
        return errors.get(field);
    }

    /**
     * Verifica si un campo específico tiene error.
     * 
     * @param field Nombre del campo
     * @return true si el campo tiene error, false en caso contrario
     */
    public boolean hasError(String field) {
        return errors.containsKey(field);
    }

    /**
     * Método estático para crear una excepción con un solo error de campo.
     * 
     * @param field Nombre del campo
     * @param error Mensaje de error
     * @return Nueva instancia de ValidationException
     */
    public static ValidationException singleFieldError(String field, String error) {
        Map<String, String> errors = new HashMap<>();
        errors.put(field, error);
        return new ValidationException("Error de validación en el campo: " + field, errors);
    }

    /**
     * Método estático para crear una excepción a partir de un mapa de errores.
     * 
     * @param errors Mapa con errores de validación
     * @return Nueva instancia de ValidationException
     */
    public static ValidationException fromErrors(Map<String, String> errors) {
        return new ValidationException(errors);
    }

    @Override
    public String toString() {
        return "ValidationException{" +
                "message='" + getMessage() + '\'' +
                ", errors=" + errors +
                '}';
    }
}