package com.eafit.tutorial.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Datos para crear un nuevo producto")
public class CreateProductDTO {

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Schema(description = "Nombre del producto", example = "MacBook Pro 16")
    private String name;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Schema(description = "Descripción del producto", example = "Laptop MacBook Pro de 16 pulgadas")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener máximo 8 dígitos enteros y 2 decimales")
    @Schema(description = "Precio del producto", example = "2499.99")
    private BigDecimal price;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\u00f1\\u00d1\\s]+$", message = "La categoría solo puede contener letras y espacios")
    @Schema(description = "Categoría del producto", example = "Electrónicos")
    private String category;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Schema(description = "Cantidad disponible en stock", example = "25")
    private Integer stock;

    public CreateProductDTO() {
    }

    public CreateProductDTO(String name, String description, BigDecimal price, 
                           String category, Integer stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stock = stock;
    }

    // Getters y Setters

    /**
     * Obtiene el nombre del producto.
     * @return Nombre del producto
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
     * @return Descripción del producto
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
     * @return Precio del producto
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
     * @return Categoría del producto
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
     * @return Stock del producto
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
        return "CreateProductDTO{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", stock=" + stock +
                '}';
    }
}