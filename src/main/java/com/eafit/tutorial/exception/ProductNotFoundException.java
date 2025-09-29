package com.eafit.tutorial.exception;

/**
 * Excepción lanzada cuando no se encuentra un producto en el sistema.
 * 
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
public class ProductNotFoundException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message Mensaje de error
     */
    public ProductNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa.
     * 
     * @param message Mensaje de error
     * @param cause Causa original de la excepción
     */
    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor con ID del producto no encontrado.
     * Genera automáticamente un mensaje descriptivo.
     * 
     * @param id ID del producto no encontrado
     */
    public ProductNotFoundException(Long id) {
        super("Producto con ID " + id + " no encontrado");
    }
}