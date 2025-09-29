package com.eafit.tutorial.util;

import com.eafit.tutorial.dto.CreateProductDTO;
import com.eafit.tutorial.dto.ProductDTO;
import com.eafit.tutorial.dto.UpdateProductDTO;
import com.eafit.tutorial.model.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversiones entre entidades Product y DTOs.
 * Proporciona métodos para convertir entre las diferentes representaciones
 * de datos de productos en las distintas capas de la aplicación.
 * 
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
@Component
public class ProductMapper {

    /**
     * Convierte una entidad Product a ProductDTO.
     * 
     * @param product Entidad Product a convertir
     * @return ProductDTO con todos los datos de la entidad, o null si el parámetro es null
     * @throws IllegalArgumentException si product es null
     */
    public ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategory(product.getCategory());
        dto.setStock(product.getStock());
        dto.setActive(product.getActive());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        return dto;
    }

    /**
     * Convierte una lista de entidades Product a una lista de ProductDTO.
     * Utiliza streams para procesamiento eficiente y filtrado de elementos null.
     * 
     * @param products Lista de entidades Product a convertir
     * @return Lista de ProductDTO correspondientes, o lista vacía si el parámetro es null
     */
    public List<ProductDTO> toDTOList(List<Product> products) {
        if (products == null) {
            return List.of(); // Retorna lista vacía inmutable
        }

        return products.stream()
                .filter(product -> product != null) // Filtrar elementos null
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un CreateProductDTO a una entidad Product.
     * Establece active=true por defecto y no asigna ID ni timestamps
     * (estos se manejan automáticamente por JPA).
     * 
     * @param createDTO DTO con datos para crear un nuevo producto
     * @return Nueva entidad Product con los datos del DTO, o null si el parámetro es null
     * @throws IllegalArgumentException si createDTO es null
     */
    public Product toEntity(CreateProductDTO createDTO) {
        if (createDTO == null) {
            return null;
        }

        Product product = new Product();
        product.setName(createDTO.getName());
        product.setDescription(createDTO.getDescription());
        product.setPrice(createDTO.getPrice());
        product.setCategory(createDTO.getCategory());
        product.setStock(createDTO.getStock());
        product.setActive(true); // Por defecto activo

        // ID, createdAt y updatedAt se manejan automáticamente por JPA
        return product;
    }

    /**
     * Actualiza una entidad Product con los datos de un UpdateProductDTO.
     * Solo actualiza los campos que no son null en el DTO, permitiendo
     * actualizaciones parciales de la entidad.
     * 
     * @param product Entidad Product a actualizar
     * @param updateDTO DTO con los datos de actualización
     * @throws IllegalArgumentException si product es null
     */
    public void updateEntity(Product product, UpdateProductDTO updateDTO) {
        if (product == null) {
            throw new IllegalArgumentException("La entidad Product no puede ser null");
        }

        if (updateDTO == null) {
            return; // No hay nada que actualizar
        }

        // Solo actualizar campos que no son null en el DTO
        if (updateDTO.hasNameUpdate()) {
            product.setName(updateDTO.getName());
        }

        if (updateDTO.hasDescriptionUpdate()) {
            product.setDescription(updateDTO.getDescription());
        }

        if (updateDTO.hasPriceUpdate()) {
            product.setPrice(updateDTO.getPrice());
        }

        if (updateDTO.hasCategoryUpdate()) {
            product.setCategory(updateDTO.getCategory());
        }

        if (updateDTO.hasStockUpdate()) {
            product.setStock(updateDTO.getStock());
        }

        // updatedAt se maneja automáticamente por JPA (@UpdateTimestamp)
    }

    /**
     * Convierte una entidad Product a ProductDTO de forma segura.
     * Versión alternativa que maneja explícitamente casos null.
     * 
     * @param product Entidad Product a convertir
     * @return ProductDTO con todos los datos de la entidad
     * @throws IllegalArgumentException si product es null
     */
    public ProductDTO toDTOSafe(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("La entidad Product no puede ser null");
        }

        return toDTO(product);
    }

    /**
     * Crea una nueva entidad Product a partir de un CreateProductDTO con validaciones.
     * 
     * @param createDTO DTO con datos para crear un nuevo producto
     * @return Nueva entidad Product con los datos del DTO
     * @throws IllegalArgumentException si createDTO es null o tiene datos inválidos
     */
    public Product toEntitySafe(CreateProductDTO createDTO) {
        if (createDTO == null) {
            throw new IllegalArgumentException("CreateProductDTO no puede ser null");
        }

        // Validaciones adicionales opcionales
        if (createDTO.getName() == null || createDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }

        if (createDTO.getPrice() == null) {
            throw new IllegalArgumentException("El precio del producto es obligatorio");
        }

        if (createDTO.getCategory() == null || createDTO.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría del producto es obligatoria");
        }

        if (createDTO.getStock() == null) {
            throw new IllegalArgumentException("El stock del producto es obligatorio");
        }

        return toEntity(createDTO);
    }

    /**
     * Convierte un UpdateProductDTO a una entidad Product.
     * Este método crea una nueva entidad Product con solo los campos 
     * no nulos del UpdateProductDTO. Se usa para actualizaciones.
     * 
     * @param updateDTO DTO con datos de actualización
     * @return Nueva entidad Product con los datos no nulos del DTO, o null si updateDTO es null
     */
    public Product toEntity(UpdateProductDTO updateDTO) {
        if (updateDTO == null) {
            return null;
        }

        Product product = new Product();
        
        // Solo asignar campos que no son null
        if (updateDTO.getName() != null) {
            product.setName(updateDTO.getName());
        }
        
        if (updateDTO.getDescription() != null) {
            product.setDescription(updateDTO.getDescription());
        }
        
        if (updateDTO.getPrice() != null) {
            product.setPrice(updateDTO.getPrice());
        }
        
        if (updateDTO.getCategory() != null) {
            product.setCategory(updateDTO.getCategory());
        }
        
        if (updateDTO.getStock() != null) {
            product.setStock(updateDTO.getStock());
        }

        return product;
    }

    /**
     * Verifica si un UpdateProductDTO tiene algún campo para actualizar.
     * 
     * @param updateDTO DTO de actualización a verificar
     * @return true si hay campos para actualizar, false en caso contrario
     */
    public boolean hasAnyUpdate(UpdateProductDTO updateDTO) {
        return updateDTO != null && updateDTO.hasUpdates();
    }

    /**
     * Crea un ProductDTO básico con solo los campos esenciales.
     * Útil para respuestas simplificadas.
     * 
     * @param product Entidad Product a convertir
     * @return ProductDTO con campos básicos (id, name, price, category, active)
     */
    public ProductDTO toBasicDTO(Product product) {
        if (product == null) {
            return null;
        }

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setCategory(product.getCategory());
        dto.setActive(product.getActive());

        return dto;
    }

    /**
     * Convierte una lista de productos a DTOs básicos usando streams.
     * 
     * @param products Lista de entidades Product
     * @return Lista de ProductDTO básicos
     */
    public List<ProductDTO> toBasicDTOList(List<Product> products) {
        if (products == null) {
            return List.of();
        }

        return products.stream()
                .filter(product -> product != null)
                .map(this::toBasicDTO)
                .collect(Collectors.toList());
    }
}