package com.eafit.tutorial.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "products", 
       indexes = {
           @Index(name = "idx_product_name", columnList = "name"),
           @Index(name = "idx_product_category", columnList = "category")
       })
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Precio del producto.
     * Campo obligatorio con precisión de 10 dígitos y 2 decimales.
     * Debe ser mayor a 0.
     */
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Categoría del producto.
     * Campo obligatorio que solo acepta letras y espacios, máximo 50 caracteres.
     */
    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\u00f1\\u00d1\\s]+$", 
             message = "La categoría solo puede contener letras y espacios")
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    /**
     * Cantidad en stock del producto.
     * Campo obligatorio que debe ser mayor o igual a 0.
     */
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "stock", nullable = false)
    private Integer stock;

    /**
     * Estado de actividad del producto.
     * Indica si el producto está activo en el sistema.
     * Por defecto es true.
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * Fecha y hora de creación del producto.
     * Se establece automáticamente al crear el registro.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de última actualización del producto.
     * Se actualiza automáticamente en cada modificación.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor por defecto.
     * Requerido por JPA.
     */
    public Product() {
    }

    /**
     * Constructor con parámetros para crear un nuevo producto.
     * 
     * @param name Nombre del producto
     * @param description Descripción del producto
     * @param price Precio del producto
     * @param category Categoría del producto
     * @param stock Cantidad en stock
     * @param active Estado de actividad
     */
    public Product(String name, String description, BigDecimal price, 
                   String category, Integer stock, Boolean active) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.active = active;
    }

    /**
     * Constructor con parámetros básicos (active = true por defecto).
     * 
     * @param name Nombre del producto
     * @param description Descripción del producto
     * @param price Precio del producto
     * @param category Categoría del producto
     * @param stock Cantidad en stock
     */
    public Product(String name, String description, BigDecimal price, 
                   String category, Integer stock) {
        this(name, description, price, category, stock, true);
    }

    // Getters y Setters

    /**
     * Obtiene el ID del producto.
     * @return ID del producto
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece el ID del producto.
     * @param id ID del producto
     */
    public void setId(Long id) {
        this.id = id;
    }

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

    /**
     * Obtiene el estado de actividad del producto.
     * @return true si está activo, false si no
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * Establece el estado de actividad del producto.
     * @param active Estado de actividad
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Obtiene la fecha de creación del producto.
     * @return Fecha de creación
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Establece la fecha de creación del producto.
     * @param createdAt Fecha de creación
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Obtiene la fecha de última actualización del producto.
     * @return Fecha de actualización
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Establece la fecha de última actualización del producto.
     * @param updatedAt Fecha de actualización
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Compara dos productos por igualdad basándose en el ID.
     * 
     * @param o Objeto a comparar
     * @return true si son iguales, false si no
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    /**
     * Genera el código hash basado en el ID.
     * 
     * @return Código hash del producto
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Representación en cadena del producto.
     * 
     * @return Cadena con información del producto
     */
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", stock=" + stock +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}