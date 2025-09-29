package com.eafit.tutorial.util;

import com.eafit.tutorial.dto.CreateProductDTO;
import com.eafit.tutorial.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * Validador de productos para reglas de negocio específicas.
 * Contiene validaciones adicionales más allá de las anotaciones estándar.
 * 
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
@Component
public class ProductValidator {

    /**
     * Palabras prohibidas en nombres de productos.
     * Estas palabras sugieren productos temporales o de prueba.
     */
    private static final Set<String> FORBIDDEN_WORDS = Set.of(
        "test", "prueba", "demo", "temporal", "ejemplo"
    );

    /**
     * Precio máximo permitido para cualquier producto.
     * Valor en BigDecimal para mayor precisión.
     */
    private static final BigDecimal MAX_PRICE = new BigDecimal("100000");

    /**
     * Stock máximo permitido para cualquier producto.
     * Evita errores de captura de datos.
     */
    private static final Integer MAX_STOCK = 10000;

    /**
     * Valida un producto para creación aplicando todas las reglas de negocio.
     * 
     * @param createDTO Datos del producto a crear
     * @throws ValidationException Si hay errores de validación
     */
    public void validateForCreation(CreateProductDTO createDTO) {
        Map<String, String> errors = new HashMap<>();

        // Validar palabras prohibidas en el nombre
        if (containsForbiddenWords(createDTO.getName())) {
            errors.put("name", "El nombre contiene palabras prohibidas: " + String.join(", ", FORBIDDEN_WORDS));
        }

        // Validar precio máximo
        if (createDTO.getPrice() != null && createDTO.getPrice().compareTo(MAX_PRICE) > 0) {
            errors.put("price", "El precio no puede exceder " + MAX_PRICE);
        }

        // Validar stock máximo
        if (createDTO.getStock() != null && createDTO.getStock() > MAX_STOCK) {
            errors.put("stock", "El stock no puede exceder " + MAX_STOCK + " unidades");
        }

        // Validar coherencia precio-categoría
        validatePriceCategoryCoherence(createDTO.getCategory(), createDTO.getPrice(), errors);

        // Lanzar excepción si hay errores
        if (!errors.isEmpty()) {
            throw new ValidationException("Errores de validación en la creación del producto", errors);
        }
    }

    /**
     * Valida que el stock sea apropiado para la categoría del producto.
     * 
     * @param category Categoría del producto
     * @param stock Stock propuesto
     * @throws ValidationException Si el stock no es apropiado para la categoría
     */
    public void validateStockForCategory(String category, Integer stock) {
        Map<String, String> errors = new HashMap<>();

        if (category == null || stock == null) {
            return; // No validar si faltan datos
        }

        String categoryLower = category.toLowerCase();

        // Productos digitales pueden tener stock muy alto
        if (categoryLower.contains("digital") || categoryLower.contains("software")) {
            if (stock > 1000) {
                errors.put("stock", "Los productos digitales no deberían tener stock superior a 1000");
            }
        }
        // Productos perecederos deberían tener stock limitado
        else if (categoryLower.contains("alimento") || categoryLower.contains("comida") || 
                 categoryLower.contains("perecedero")) {
            if (stock > 100) {
                errors.put("stock", "Los productos perecederos no deberían tener stock superior a 100");
            }
        }
        // Productos físicos voluminosos
        else if (categoryLower.contains("mueble") || categoryLower.contains("electrodomestico")) {
            if (stock > 50) {
                errors.put("stock", "Los productos voluminosos no deberían tener stock superior a 50");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Stock inapropiado para la categoría", errors);
        }
    }

    /**
     * Verifica si el nombre del producto contiene palabras prohibidas.
     * 
     * @param name Nombre del producto a verificar
     * @return true si contiene palabras prohibidas, false en caso contrario
     */
    private boolean containsForbiddenWords(String name) {
        if (name == null) {
            return false;
        }

        String nameLower = name.toLowerCase();
        return FORBIDDEN_WORDS.stream()
                .anyMatch(nameLower::contains);
    }

    /**
     * Valida la coherencia entre precio y categoría del producto.
     * Aplica reglas de negocio específicas según el tipo de producto.
     * 
     * @param category Categoría del producto
     * @param price Precio del producto
     * @param errors Mapa para acumular errores encontrados
     */
    private void validatePriceCategoryCoherence(String category, BigDecimal price, Map<String, String> errors) {
        if (category == null || price == null) {
            return; // No validar si faltan datos
        }

        String categoryLower = category.toLowerCase();

        // Libros no deberían ser muy caros (generalmente)
        if (categoryLower.contains("libro") || categoryLower.contains("book")) {
            if (price.compareTo(new BigDecimal("500")) > 0) {
                errors.put("priceCategory", "Los libros generalmente no exceden $500. Verifique el precio.");
            }
        }
        // Electrónicos básicos tienen un rango típico
        else if (categoryLower.contains("electronico") || categoryLower.contains("electronic")) {
            if (price.compareTo(new BigDecimal("10")) < 0) {
                errors.put("priceCategory", "Los productos electrónicos usualmente cuestan más de $10");
            }
        }
        // Ropa tiene rangos típicos
        else if (categoryLower.contains("ropa") || categoryLower.contains("clothing")) {
            if (price.compareTo(new BigDecimal("5000")) > 0) {
                errors.put("priceCategory", "La ropa raramente excede $5000. Verifique si es correcta la categoría.");
            }
        }
        // Productos digitales no deberían ser extremadamente caros
        else if (categoryLower.contains("digital") || categoryLower.contains("software")) {
            if (price.compareTo(new BigDecimal("2000")) > 0) {
                errors.put("priceCategory", "Los productos digitales raramente exceden $2000");
            }
        }
    }
}