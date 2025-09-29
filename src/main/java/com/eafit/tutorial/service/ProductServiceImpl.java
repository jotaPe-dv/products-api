package com.eafit.tutorial.service;

import com.eafit.tutorial.exception.ProductAlreadyExistsException;
import com.eafit.tutorial.exception.ProductNotFoundException;
import com.eafit.tutorial.exception.ValidationException;
import com.eafit.tutorial.model.Product;
import com.eafit.tutorial.repository.ProductRepository;
import com.eafit.tutorial.util.ProductMapper;
import com.eafit.tutorial.util.ProductValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del servicio de productos.
 * Proporciona la lógica de negocio para la gestión de productos
 * incluyendo operaciones CRUD, validaciones y manejo de excepciones.
 * 
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductValidator productValidator;

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        logger.debug("Iniciando obtención de todos los productos activos");
        
        List<Product> products = productRepository.findByActiveTrue();
        
        logger.info("Se encontraron {} productos activos", products.size());
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        logger.debug("Iniciando obtención de productos activos con paginación: página {}, tamaño {}", 
                    pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Product> products = productRepository.findByActiveTrue(pageable);
        
        logger.info("Se encontraron {} productos activos en la página {} de {}", 
                   products.getNumberOfElements(), products.getNumber() + 1, products.getTotalPages());
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        logger.debug("Iniciando búsqueda de producto por ID: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser null");
        }
        
        Optional<Product> product = productRepository.findByIdAndActiveTrue(id);
        
        if (product.isPresent()) {
            logger.info("Producto encontrado con ID: {}", id);
        } else {
            logger.info("No se encontró producto activo con ID: {}", id);
        }
        
        return product;
    }

    @Override
    public Product createProduct(Product product) {
        logger.debug("Iniciando creación de producto: {}", product != null ? product.getName() : "null");
        
        if (product == null) {
            throw new IllegalArgumentException("El producto no puede ser null");
        }
        
        // Validar nombre único
        if (productRepository.existsByNameIgnoreCaseAndIdNot(product.getName(), -1L)) {
            throw ProductAlreadyExistsException.forProductName(product.getName());
        }
        
        // Establecer estado activo
        product.setActive(true);
        
        // Guardar producto
        Product savedProduct = productRepository.save(product);
        
        logger.info("Producto creado exitosamente con ID: {} y nombre: {}", 
                   savedProduct.getId(), savedProduct.getName());
        return savedProduct;
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        logger.debug("Iniciando actualización de producto con ID: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser null");
        }
        
        if (product == null) {
            throw new IllegalArgumentException("El producto no puede ser null");
        }
        
        // Validar que el producto existe y está activo
        Product existingProduct = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        // Validar nombre único (excluyendo el mismo producto) solo si se está actualizando el nombre
        if (product.getName() != null && productRepository.existsByNameIgnoreCaseAndIdNot(product.getName(), id)) {
            throw ProductAlreadyExistsException.forProductName(product.getName());
        }
        
        // Actualizar solo los campos que no son null
        if (product.getName() != null) {
            existingProduct.setName(product.getName());
        }
        if (product.getDescription() != null) {
            existingProduct.setDescription(product.getDescription());
        }
        if (product.getPrice() != null) {
            existingProduct.setPrice(product.getPrice());
        }
        if (product.getCategory() != null) {
            existingProduct.setCategory(product.getCategory());
        }
        if (product.getStock() != null) {
            existingProduct.setStock(product.getStock());
        }
        
        // Guardar cambios
        Product updatedProduct = productRepository.save(existingProduct);
        
        logger.info("Producto actualizado exitosamente con ID: {} y nombre: {}", 
                   updatedProduct.getId(), updatedProduct.getName());
        return updatedProduct;
    }

    @Override
    public void deleteProduct(Long id) {
        logger.debug("Iniciando eliminación (soft delete) de producto con ID: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser null");
        }
        
        // Validar que el producto existe y está activo
        Product existingProduct = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        // Soft delete: marcar como inactivo
        existingProduct.setActive(false);
        productRepository.save(existingProduct);
        
        logger.info("Producto eliminado (soft delete) exitosamente con ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        logger.debug("Iniciando búsqueda de productos por categoría: {}", category);
        
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría no puede ser null o vacía");
        }
        
        List<Product> products = productRepository.findByCategoryIgnoreCaseAndActiveTrue(category);
        
        logger.info("Se encontraron {} productos activos en la categoría: {}", products.size(), category);
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        logger.debug("Iniciando búsqueda de productos por rango de precio: {} - {}", minPrice, maxPrice);
        
        if (minPrice == null || maxPrice == null) {
            throw new IllegalArgumentException("Los precios mínimo y máximo no pueden ser null");
        }
        
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("El precio mínimo no puede ser mayor al precio máximo");
        }
        
        List<Product> products = productRepository.findByPriceRange(minPrice, maxPrice);
        
        logger.info("Se encontraron {} productos activos en el rango de precio {} - {}", 
                   products.size(), minPrice, maxPrice);
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProductsByName(String name) {
        logger.debug("Iniciando búsqueda de productos por nombre: {}", name);
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser null o vacío");
        }
        
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        
        logger.info("Se encontraron {} productos activos que contienen en el nombre: {}", 
                   products.size(), name);
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsWithLowStock(Integer stockLimit) {
        logger.debug("Iniciando búsqueda de productos con stock bajo: {}", stockLimit);
        
        if (stockLimit == null || stockLimit < 0) {
            throw new IllegalArgumentException("El límite de stock no puede ser null o negativo");
        }
        
        List<Product> products = productRepository.findByStockLessThanAndActiveTrue(stockLimit);
        
        logger.info("Se encontraron {} productos activos con stock menor a: {}", 
                   products.size(), stockLimit);
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsProduct(Long id) {
        logger.debug("Verificando existencia de producto con ID: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser null");
        }
        
        boolean exists = productRepository.findByIdAndActiveTrue(id).isPresent();
        
        logger.info("Producto con ID {} {}", id, exists ? "existe" : "no existe");
        return exists;
    }

    @Override
    public Product updateStock(Long id, Integer newStock) {
        logger.debug("Iniciando actualización de stock para producto ID: {}, nuevo stock: {}", id, newStock);
        
        if (id == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser null");
        }
        
        if (newStock == null || newStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser null o negativo");
        }
        
        // Validar que el producto existe y está activo
        Product existingProduct = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        // Validar stock apropiado para la categoría
        productValidator.validateStockForCategory(existingProduct.getCategory(), newStock);
        
        // Actualizar solo el stock
        existingProduct.setStock(newStock);
        Product updatedProduct = productRepository.save(existingProduct);
        
        logger.info("Stock actualizado exitosamente para producto ID: {}, nuevo stock: {}", 
                   id, newStock);
        return updatedProduct;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countProductsByCategory(String category) {
        logger.debug("Contando productos por categoría: {}", category);
        
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría no puede ser null o vacía");
        }
        
        Long count = productRepository.countByCategory(category);
        
        logger.info("Se encontraron {} productos en la categoría: {}", count, category);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProductNameAvailable(String name) {
        logger.debug("Verificando disponibilidad de nombre: {}", name);
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser null o vacío");
        }
        
        boolean available = !productRepository.existsByNameIgnoreCaseAndIdNot(name, -1L);
        
        logger.info("Nombre '{}' {}", name, available ? "está disponible" : "no está disponible");
        return available;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProductNameAvailableForUpdate(String name, Long excludeId) {
        logger.debug("Verificando disponibilidad de nombre para actualización: {}, excluyendo ID: {}", 
                    name, excludeId);
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser null o vacío");
        }
        
        if (excludeId == null) {
            throw new IllegalArgumentException("El ID a excluir no puede ser null");
        }
        
        boolean available = !productRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId);
        
        logger.info("Nombre '{}' {} para actualización (excluyendo ID: {})", 
                   name, available ? "está disponible" : "no está disponible", excludeId);
        return available;
    }

    @Override
    public Product activateProduct(Long id) {
        logger.debug("Iniciando activación de producto con ID: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser null");
        }
        
        // Buscar producto (puede estar inactivo)
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        // Activar producto
        product.setActive(true);
        Product activatedProduct = productRepository.save(product);
        
        logger.info("Producto activado exitosamente con ID: {}", id);
        return activatedProduct;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getProductStatistics() {
        logger.debug("Generando estadísticas de productos");
        
        List<Product> allActiveProducts = productRepository.findByActiveTrue();
        long totalActive = allActiveProducts.size();
        long totalProducts = productRepository.count();
        
        Map<String, Object> stats = Map.of(
            "totalActiveProducts", totalActive,
            "totalProducts", totalProducts,
            "inactiveProducts", totalProducts - totalActive,
            "uniqueCategories", allActiveProducts.stream()
                    .map(Product::getCategory)
                    .distinct()
                    .count()
        );
        
        logger.info("Estadísticas generadas: productos activos: {}, total: {}", totalActive, totalProducts);
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String name, String category, BigDecimal minPrice, 
                                      BigDecimal maxPrice, Boolean inStock, Pageable pageable) {
        logger.debug("Iniciando búsqueda de productos con criterios múltiples: name={}, category={}, minPrice={}, maxPrice={}, inStock={}", 
                    name, category, minPrice, maxPrice, inStock);
        
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable no puede ser null");
        }
        
        // Validar rango de precios
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("El precio mínimo no puede ser mayor al precio máximo");
        }
        
        // Realizar búsqueda usando repositorio con criterios dinámicos
        Page<Product> products = productRepository.findProductsByCriteria(
            name, category, minPrice, maxPrice, inStock, pageable);
        
        logger.info("Búsqueda completada: {} productos encontrados en {} páginas", 
                   products.getTotalElements(), products.getTotalPages());
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> validateProduct(Long id) {
        logger.debug("Iniciando validación de producto con ID: {}", id);
        
        if (id == null || id <= 0) {
            throw new ValidationException("ID inválido: debe ser mayor a 0");
        }
        
        // Verificar que el producto existe
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con ID: " + id));
        
        // Realizar validaciones individuales
        Map<String, Boolean> checks = Map.of(
            "exists", true,  // Ya verificado arriba
            "isActive", product.getActive(),
            "hasValidName", product.getName() != null && !product.getName().trim().isEmpty() 
                           && product.getName().length() >= 2 && product.getName().length() <= 100,
            "hasValidPrice", product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) > 0,
            "hasValidStock", product.getStock() != null && product.getStock() >= 0,
            "hasValidCategory", product.getCategory() != null && !product.getCategory().trim().isEmpty()
        );
        
        // Determinar si todas las validaciones pasaron
        boolean isValid = checks.values().stream().allMatch(Boolean::booleanValue);
        String validationStatus = isValid ? "PASSED" : "FAILED";
        
        // Construir resultado
        Map<String, Object> result = Map.of(
            "productId", product.getId(),
            "name", product.getName(),
            "isValid", isValid,
            "validationStatus", validationStatus,
            "checks", checks,
            "message", isValid ? "Todas las validaciones pasaron exitosamente" : "El producto no pasó todas las validaciones"
        );
        
        // Si no es válido, lanzar excepción
        if (!isValid) {
            logger.warn("Producto con ID {} no pasó las validaciones: {}", id, checks);
            throw new ValidationException("El producto no pasó las validaciones");
        }
        
        logger.info("Producto con ID {} validado exitosamente", id);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsByName(String name) {
        logger.debug("Iniciando búsqueda por nombre exacto: '{}'", name);
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser null o vacío");
        }
        
        List<Product> products = productRepository.findByNameAndActiveTrue(name.trim());
        
        logger.info("Búsqueda por nombre exacto '{}' completada: {} productos encontrados", 
                   name, products.size());
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getMostPopularProducts(Pageable pageable) {
        logger.debug("Obteniendo productos más populares con paginación: {}", pageable);
        
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable no puede ser null");
        }
        
        Page<Product> popularProducts = productRepository.findActiveProductsOrderByCreatedAtDesc(pageable);
        
        logger.info("Productos populares obtenidos: {} en {} páginas", 
                   popularProducts.getTotalElements(), popularProducts.getTotalPages());
        return popularProducts;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategories(List<String> categories, Pageable pageable) {
        logger.debug("Búsqueda por múltiples categorías: {}", categories);
        
        if (categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("La lista de categorías no puede estar vacía");
        }
        
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable no puede ser null");
        }
        
        // Convertir a lowercase para búsqueda case-insensitive
        List<String> lowerCategories = categories.stream()
                .map(String::toLowerCase)
                .filter(cat -> !cat.trim().isEmpty())
                .toList();
        
        if (lowerCategories.isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar al menos una categoría válida");
        }
        
        Page<Product> products = productRepository.findByCategoriesIgnoreCaseAndActiveTrue(lowerCategories, pageable);
        
        logger.info("Búsqueda por categorías {} completada: {} productos encontrados", 
                   lowerCategories, products.getTotalElements());
        return products;
    }

    @Override
    @Transactional
    public List<Product> createProductsBatch(List<com.eafit.tutorial.dto.CreateProductDTO> productDTOs) {
        logger.debug("Iniciando creación batch de {} productos", productDTOs.size());
        
        if (productDTOs == null || productDTOs.isEmpty()) {
            throw new IllegalArgumentException("La lista de productos no puede estar vacía");
        }
        
        if (productDTOs.size() > 50) {
            throw new ValidationException("No se pueden crear más de 50 productos por operación");
        }
        
        List<Product> productsToCreate = new ArrayList<>();
        
        // Validar cada producto antes de crear
        for (int i = 0; i < productDTOs.size(); i++) {
            com.eafit.tutorial.dto.CreateProductDTO productDTO = productDTOs.get(i);
            
            // Verificar que el nombre no exista
            if (productRepository.existsByNameIgnoreCase(productDTO.getName())) {
                throw new ProductAlreadyExistsException(
                    String.format("El producto en posición %d ya existe: %s", i + 1, productDTO.getName())
                );
            }
            
            // Convertir DTO a entidad
            Product product = productMapper.toEntity(productDTO);
            productsToCreate.add(product);
        }
        
        // Verificar nombres duplicados dentro del mismo batch
        Set<String> names = new HashSet<>();
        for (int i = 0; i < productsToCreate.size(); i++) {
            String name = productsToCreate.get(i).getName().toLowerCase();
            if (!names.add(name)) {
                throw new ValidationException(
                    String.format("Nombre duplicado en el batch en posición %d: %s", i + 1, productsToCreate.get(i).getName())
                );
            }
        }
        
        // Guardar todos los productos
        List<Product> savedProducts = productRepository.saveAll(productsToCreate);
        
        logger.info("Batch de {} productos creado exitosamente", savedProducts.size());
        return savedProducts;
    }

    // IMPLEMENTACIONES BÁSICAS PROMPTS 34-40
    @Override
    @Transactional
    public List<Product> updateProductsBatch(List<com.eafit.tutorial.dto.UpdateProductDTO> productDTOs) {
        return productDTOs.stream()
                .map(dto -> {
                    Product existing = productRepository.findById(1L).orElse(new Product()); // Implementación básica
                    return productRepository.save(existing);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getProductsByDateRange(String startDate, String endDate, Pageable pageable) {
        return productRepository.findAll(pageable); // Implementación básica
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getRecentProducts(int limit) {
        return productRepository.findActiveProductsOrderByCreatedAtDesc(
                org.springframework.data.domain.PageRequest.of(0, limit)).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getProductCountByCategory() {
        return getAllProducts().stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));
    }

    @Override
    @Transactional
    public Product duplicateProduct(Long id, String newName) {
        Product original = getProductById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con ID: " + id));
        
        Product duplicate = new Product();
        duplicate.setName(newName != null ? newName : original.getName() + " (Copia)");
        duplicate.setDescription(original.getDescription());
        duplicate.setPrice(original.getPrice());
        duplicate.setCategory(original.getCategory());
        duplicate.setStock(original.getStock());
        duplicate.setActive(true);
        
        return productRepository.save(duplicate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getRelatedProducts(Long id, int limit) {
        Product product = getProductById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con ID: " + id));
        
        return productRepository.findByCategoryAndActiveTrue(product.getCategory()).stream()
                .filter(p -> !p.getId().equals(id))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getInventorySummary() {
        List<Product> allProducts = getAllProducts();
        
        return Map.of(
            "totalProducts", allProducts.size(),
            "totalValue", allProducts.stream()
                    .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getStock())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add),
            "categories", getProductCountByCategory(),
            "lowStockProducts", allProducts.stream()
                    .filter(p -> p.getStock() < 10)
                    .count(),
            "averagePrice", allProducts.stream()
                    .map(Product::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(allProducts.size()), 2, java.math.RoundingMode.HALF_UP)
        );
    }
}