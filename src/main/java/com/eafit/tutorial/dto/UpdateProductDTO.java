package com.eafit.tutorial.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO para la actualización parcial de productos.
 * Todos los campos son opcionales para permitir actualizaciones parciales.
 * Solo se validarán los campos que no sean null.
 * 
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
@Schema(description = "Datos para actualización parcial de un producto. Todos los campos son opcionales.")
public class UpdateProductDTO {

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Schema(description = "Nombre del producto (opcional para actualización)", 
            example = "iPhone 15 Pro Max", 
            maxLength = 100)
    private String name;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Schema(description = "Descripción detallada del producto (opcional para actualización)", 
            example = "Smartphone Apple iPhone 15 Pro Max con pantalla de 6.7 pulgadas", 
            maxLength = 500)
    private String description;

    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener máximo 8 dígitos enteros y 2 decimales")
    @Schema(description = "Precio del producto en USD (opcional para actualización)", 
            example = "1199.99", 
            minimum = "0.01")
    private BigDecimal price;

    @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\u00f1\\u00d1\\s]+$", 
             message = "La categoría solo puede contener letras y espacios")
    @Schema(description = "Categoría del producto (opcional para actualización)", 
            example = "Smartphones", 
            maxLength = 50,
            pattern = "^[a-zA-ZÀ-ÿ\\u00f1\\u00d1\\s]+$",
            allowableValues = {"Electrónicos", "Libros", "Ropa", "Hogar", "Deportes", "Salud", "Belleza", "Automotriz", "Juguetes", "Música", "Películas", "Software", "Instrumentos", "Oficina", "Jardín"})
    private String category;

    @Min(value = 0, message = "El stock no puede ser negativo")
    @Schema(description = "Cantidad disponible en stock (opcional para actualización)", 
            example = "75", 
            minimum = "0")
    private Integer stock;

    /**
     * Constructor por defecto.
     */
    public UpdateProductDTO() {
    }

    /**
     * Constructor con todos los parámetros.
     * 
     * @param name Nombre del producto
     * @param description Descripción del producto
     * @param price Precio del producto
     * @param category Categoría del producto
     * @param stock Stock disponible
     */
    public UpdateProductDTO(String name, String description, BigDecimal price, 
                           String category, Integer stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stock = stock;
    }

    /**
     * Verifica si hay al menos un campo con valor para actualizar.
     * 
     * @return true si al menos un campo no es null, false si todos los campos son null
     */
    public boolean hasUpdates() {
        return name != null || 
               description != null || 
               price != null || 
               category != null || 
               stock != null;
    }

    /**
     * Verifica si el nombre será actualizado.
     * @return true si name no es null
     */
    public boolean hasNameUpdate() {
        return name != null;
    }

    /**
     * Verifica si la descripción será actualizada.
     * @return true si description no es null
     */
    public boolean hasDescriptionUpdate() {
        return description != null;
    }

    /**
     * Verifica si el precio será actualizado.
     * @return true si price no es null
     */
    public boolean hasPriceUpdate() {
        return price != null;
    }

    /**
     * Verifica si la categoría será actualizada.
     * @return true si category no es null
     */
    public boolean hasCategoryUpdate() {
        return category != null;
    }

    /**
     * Verifica si el stock será actualizado.
     * @return true si stock no es null
     */
    public boolean hasStockUpdate() {
        return stock != null;
    }

    // Getters y Setters

    /**
     * Obtiene el nombre del producto.
     * @return Nombre del producto o null si no se va a actualizar
     */
    public String getName() {
        return name;
    }

    /**
     * Establece el nombre del producto.
     * @param name Nombre del producto
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtiene la descripción del producto.
     * @return Descripción del producto o null si no se va a actualizar
     */
    public String getDescription() {
        return description;
    }

    /**
     * Establece la descripción del producto.
     * @param description Descripción del producto
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Obtiene el precio del producto.
     * @return Precio del producto o null si no se va a actualizar
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Establece el precio del producto.
     * @param price Precio del producto
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Obtiene la categoría del producto.
     * @return Categoría del producto o null si no se va a actualizar
     */
    public String getCategory() {
        return category;
    }

    /**
     * Establece la categoría del producto.
     * @param category Categoría del producto
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Obtiene el stock del producto.
     * @return Stock del producto o null si no se va a actualizar
     */
    public Integer getStock() {
        return stock;
    }

    /**
     * Establece el stock del producto.
     * @param stock Stock del producto
     */
    public void setStock(Integer stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "UpdateProductDTO{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", stock=" + stock +
                '}';
    }
}