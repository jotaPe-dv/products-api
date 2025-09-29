package com.eafit.tutorial.exception;

/**
 * Excepción lanzada cuando se intenta crear o actualizar un producto 
 * con un nombre que ya existe en el sistema.
 * 
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
public class ProductAlreadyExistsException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message Mensaje de error
     */
    public ProductAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa.
     * 
     * @param message Mensaje de error
     * @param cause Causa original de la excepción
     */
    public ProductAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Método estático para crear excepción basada en nombre de producto.
     * 
     * @param productName Nombre del producto que ya existe
     * @return Nueva instancia de ProductAlreadyExistsException
     */
    public static ProductAlreadyExistsException forProductName(String productName) {
        return new ProductAlreadyExistsException("Ya existe un producto con el nombre: " + productName);
    }

    /**
     * Método estático alternativo para crear excepción con nombre de producto.
     * 
     * @param productName Nombre del producto que ya existe
     * @return Nueva instancia de ProductAlreadyExistsException
     */
    public static ProductAlreadyExistsException withProductName(String productName) {
        return forProductName(productName);
    }
}