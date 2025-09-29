package com.eafit.tutorial.service;

import com.eafit.tutorial.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interfaz de servicio para la gestión de productos.
 * Define las operaciones de negocio disponibles para el manejo de productos
 * en el sistema, incluyendo operaciones CRUD, búsquedas y validaciones.
 * 
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
public interface ProductService {

    /**
     * Obtiene todos los productos activos del sistema.
     * 
     * @return Lista de todos los productos con estado active = true
     */
    List<Product> getAllProducts();

    /**
     * Obtiene todos los productos activos del sistema con paginación.
     * Permite controlar el número de elementos por página y el ordenamiento.
     * 
     * @param pageable Objeto que contiene información de paginación (página, tamaño, ordenamiento)
     * @return Página de productos activos según los parámetros de paginación
     */
    Page<Product> getAllProducts(Pageable pageable);

    /**
     * Busca un producto por su identificador único.
     * Solo retorna productos que estén activos (active = true).
     * 
     * @param id Identificador único del producto
     * @return Optional que contiene el producto si existe y está activo, empty en caso contrario
     * @throws IllegalArgumentException si el id es null
     */
    Optional<Product> getProductById(Long id);

    /**
     * Crea un nuevo producto en el sistema.
     * Valida que los datos sean correctos y que el nombre no esté duplicado.
     * Establece automáticamente el estado como activo y las fechas de auditoría.
     * 
     * @param product Entidad producto con los datos a crear
     * @return Producto creado con ID generado y fechas de auditoría
     * @throws IllegalArgumentException si el producto es null o tiene datos inválidos
     * @throws RuntimeException si ya existe un producto con el mismo nombre
     */
    Product createProduct(Product product);

    /**
     * Actualiza un producto existente en el sistema.
     * Solo actualiza productos que estén activos. Valida que el nombre
     * no esté duplicado con otros productos.
     * 
     * @param id Identificador único del producto a actualizar
     * @param product Entidad producto con los nuevos datos
     * @return Producto actualizado con fecha de modificación actualizada
     * @throws IllegalArgumentException si el id o producto son null
     * @throws RuntimeException si el producto no existe, no está activo, o el nombre está duplicado
     */
    Product updateProduct(Long id, Product product);

    /**
     * Elimina un producto del sistema mediante soft delete.
     * Cambia el estado del producto a inactivo (active = false) en lugar
     * de eliminarlo físicamente de la base de datos.
     * 
     * @param id Identificador único del producto a eliminar
     * @throws IllegalArgumentException si el id es null
     * @throws RuntimeException si el producto no existe o ya está inactivo
     */
    void deleteProduct(Long id);

    /**
     * Obtiene todos los productos activos de una categoría específica.
     * La búsqueda es case-insensitive.
     * 
     * @param category Categoría de productos a buscar
     * @return Lista de productos activos de la categoría especificada
     * @throws IllegalArgumentException si la categoría es null o vacía
     */
    List<Product> getProductsByCategory(String category);

    /**
     * Obtiene productos activos dentro de un rango de precios específico.
     * Incluye productos cuyo precio esté entre el mínimo y máximo (inclusive).
     * 
     * @param minPrice Precio mínimo del rango (inclusive)
     * @param maxPrice Precio máximo del rango (inclusive)
     * @return Lista de productos activos en el rango de precio especificado
     * @throws IllegalArgumentException si algún precio es null o si minPrice > maxPrice
     */
    List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Busca productos activos cuyo nombre contenga el texto especificado.
     * La búsqueda es case-insensitive y utiliza coincidencia parcial.
     * 
     * @param name Texto a buscar en el nombre de los productos
     * @return Lista de productos activos cuyo nombre contenga el texto especificado
     * @throws IllegalArgumentException si el nombre es null o vacío
     */
    List<Product> searchProductsByName(String name);

    /**
     * Busca productos por múltiples criterios con paginación.
     * 
     * @param name Nombre del producto (búsqueda parcial, case-insensitive)
     * @param category Categoría del producto (exacta, case-insensitive)
     * @param minPrice Precio mínimo (inclusive)
     * @param maxPrice Precio máximo (inclusive)
     * @param inStock Si true, solo productos con stock > 0
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página de productos que coinciden con los criterios
     */
    Page<Product> searchProducts(String name, String category, BigDecimal minPrice, 
                                BigDecimal maxPrice, Boolean inStock, Pageable pageable);

    /**
     * Obtiene productos activos con stock menor al límite especificado.
     * Útil para identificar productos que necesitan reabastecimiento.
     * 
     * @param stockLimit Cantidad límite de stock
     * @return Lista de productos activos con stock menor al límite especificado
     * @throws IllegalArgumentException si stockLimit es null o negativo
     */
    List<Product> getProductsWithLowStock(Integer stockLimit);

    /**
     * Verifica si existe un producto activo con el ID especificado.
     * 
     * @param id Identificador único del producto
     * @return true si existe un producto activo con el ID especificado, false en caso contrario
     * @throws IllegalArgumentException si el id es null
     */
    boolean existsProduct(Long id);

    /**
     * Actualiza únicamente el stock de un producto específico.
     * Valida que el producto exista y esté activo antes de actualizar.
     * 
     * @param id Identificador único del producto
     * @param newStock Nueva cantidad de stock a establecer
     * @return Producto actualizado con el nuevo stock
     * @throws IllegalArgumentException si el id o newStock son null, o si newStock es negativo
     * @throws RuntimeException si el producto no existe o no está activo
     */
    Product updateStock(Long id, Integer newStock);

    /**
     * Obtiene el conteo de productos por categoría.
     * Cuenta solo productos activos.
     * 
     * @param category Categoría a contar
     * @return Número de productos activos en la categoría especificada
     * @throws IllegalArgumentException si la categoría es null o vacía
     */
    Long countProductsByCategory(String category);

    /**
     * Verifica si un nombre de producto está disponible para uso.
     * Útil para validaciones durante la creación de productos.
     * 
     * @param name Nombre del producto a verificar
     * @return true si el nombre está disponible, false si ya está en uso
     * @throws IllegalArgumentException si el nombre es null o vacío
     */
    boolean isProductNameAvailable(String name);

    /**
     * Verifica si un nombre de producto está disponible para actualización.
     * Excluye el producto con el ID especificado de la validación.
     * 
     * @param name Nombre del producto a verificar
     * @param excludeId ID del producto a excluir de la verificación
     * @return true si el nombre está disponible, false si ya está en uso por otro producto
     * @throws IllegalArgumentException si el nombre es null o vacío, o si excludeId es null
     */
    boolean isProductNameAvailableForUpdate(String name, Long excludeId);

    /**
     * Activa un producto previamente desactivado.
     * Cambia el estado del producto a activo (active = true).
     * 
     * @param id Identificador único del producto a activar
     * @return Producto activado
     * @throws IllegalArgumentException si el id es null
     * @throws RuntimeException si el producto no existe
     */
    Product activateProduct(Long id);

    /**
     * Obtiene estadísticas básicas de productos.
     * Retorna información como total de productos activos, categorías únicas, etc.
     * 
     * @return Mapa con estadísticas básicas del inventario
     */
    java.util.Map<String, Object> getProductStatistics();

    /**
     * Valida un producto específico por su ID.
     * Verifica que el producto exista, esté activo y cumpla con todas las reglas de negocio.
     * 
     * @param id ID del producto a validar
     * @return Mapa con el resultado de la validación incluyendo checks individuales
     * @throws ProductNotFoundException si el producto no existe
     * @throws ValidationException si el producto no pasa las validaciones
     */
    java.util.Map<String, Object> validateProduct(Long id);

    /**
     * Busca productos por nombre exacto.
     * Solo retorna productos activos que coincidan exactamente con el nombre.
     * 
     * @param name Nombre exacto del producto
     * @return Lista de productos que coinciden exactamente con el nombre
     * @throws IllegalArgumentException si el nombre es null o vacío
     */
    List<Product> getProductsByName(String name);

    /**
     * Obtiene los productos más populares del sistema.
     * Los productos se ordenan por fecha de creación (simulando popularidad).
     * 
     * @param pageable Configuración de paginación
     * @return Página de productos ordenados por popularidad
     */
    Page<Product> getMostPopularProducts(Pageable pageable);

    /**
     * Busca productos por múltiples categorías.
     * 
     * @param categories Lista de categorías a buscar
     * @param pageable Configuración de paginación
     * @return Página de productos que pertenecen a las categorías especificadas
     */
    Page<Product> getProductsByCategories(List<String> categories, Pageable pageable);

    /**
     * Crea múltiples productos de forma transaccional.
     * 
     * @param productDTOs Lista de productos a crear
     * @return Lista de productos creados
     * @throws ProductAlreadyExistsException si algún producto ya existe
     * @throws ValidationException si algún producto no es válido
     */
    List<Product> createProductsBatch(List<com.eafit.tutorial.dto.CreateProductDTO> productDTOs);

    // PROMPTS 34-40: Métodos adicionales
    List<Product> updateProductsBatch(List<com.eafit.tutorial.dto.UpdateProductDTO> productDTOs);
    Page<Product> getProductsByDateRange(String startDate, String endDate, Pageable pageable);
    List<Product> getRecentProducts(int limit);
    Map<String, Long> getProductCountByCategory();
    Product duplicateProduct(Long id, String newName);
    List<Product> getRelatedProducts(Long id, int limit);
    Map<String, Object> getInventorySummary();
}