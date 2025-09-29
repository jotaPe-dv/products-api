package com.eafit.tutorial.repository;

import com.eafit.tutorial.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Product.
 * Proporciona métodos de acceso a datos para productos con consultas
 * personalizadas y operaciones CRUD básicas.
 * 
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Encuentra todos los productos activos.
     * 
     * @return Lista de productos con estado active = true
     */
    List<Product> findByActiveTrue();

    /**
     * Encuentra todos los productos activos con paginación.
     * 
     * @param pageable Objeto de paginación que especifica página, tamaño y ordenamiento
     * @return Página de productos con estado active = true
     */
    Page<Product> findByActiveTrue(Pageable pageable);

    /**
     * Encuentra productos por categoría (ignorando mayúsculas/minúsculas) que estén activos.
     * 
     * @param category Categoría a buscar (case insensitive)
     * @return Lista de productos de la categoría especificada que estén activos
     */
    List<Product> findByCategoryIgnoreCaseAndActiveTrue(String category);

    /**
     * Encuentra productos dentro de un rango de precios.
     * Utiliza consulta JPQL personalizada con BETWEEN.
     * 
     * @param minPrice Precio mínimo del rango (inclusive)
     * @param maxPrice Precio máximo del rango (inclusive)
     * @return Lista de productos cuyo precio esté entre minPrice y maxPrice
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.active = true")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                   @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Encuentra productos cuyo nombre contenga el texto especificado (case insensitive).
     * Utiliza consulta JPQL personalizada con LIKE.
     * 
     * @param name Texto a buscar en el nombre del producto
     * @return Lista de productos cuyo nombre contenga el texto especificado
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.active = true")
    List<Product> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Encuentra productos con stock menor al especificado y que estén activos.
     * Útil para identificar productos con stock bajo.
     * 
     * @param stock Cantidad de stock límite
     * @return Lista de productos con stock menor al especificado y activos
     */
    List<Product> findByStockLessThanAndActiveTrue(Integer stock);

    /**
     * Cuenta el número de productos por categoría.
     * Utiliza consulta JPQL personalizada para contar registros.
     * 
     * @param category Categoría a contar
     * @return Número de productos en la categoría especificada
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE LOWER(p.category) = LOWER(:category)")
    Long countByCategory(@Param("category") String category);

    /**
     * Verifica si existe un producto con el nombre especificado, excluyendo un ID específico.
     * Útil para validar nombres únicos al actualizar productos.
     * 
     * @param name Nombre del producto a verificar (case insensitive)
     * @param id ID del producto a excluir de la búsqueda
     * @return true si existe otro producto con el mismo nombre, false en caso contrario
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    /**
     * Encuentra un producto por ID que esté activo.
     * 
     * @param id ID del producto a buscar
     * @return Optional que contiene el producto si existe y está activo, empty en caso contrario
     */
    Optional<Product> findByIdAndActiveTrue(Long id);

    /**
     * Encuentra productos por múltiples categorías que estén activos.
     * Utiliza consulta JPQL con IN para buscar en múltiples categorías.
     * 
     * @param categories Lista de categorías a buscar
     * @return Lista de productos que pertenezcan a alguna de las categorías especificadas
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.category) IN :categories AND p.active = true")
    List<Product> findByCategoriesIgnoreCaseAndActiveTrue(@Param("categories") List<String> categories);

    /**
     * Encuentra productos por múltiples categorías que estén activos con paginación.
     * 
     * @param categories Lista de categorías a buscar
     * @param pageable Configuración de paginación
     * @return Página de productos que pertenezcan a alguna de las categorías especificadas
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.category) IN :categories AND p.active = true")
    Page<Product> findByCategoriesIgnoreCaseAndActiveTrue(@Param("categories") List<String> categories, Pageable pageable);

    /**
     * Encuentra productos ordenados por fecha de creación descendente.
     * Útil para obtener los productos más recientes.
     * 
     * @param pageable Objeto de paginación
     * @return Página de productos ordenados por fecha de creación (más recientes primero)
     */
    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.createdAt DESC")
    Page<Product> findActiveProductsOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Encuentra productos con precio mayor al especificado.
     * 
     * @param price Precio mínimo
     * @return Lista de productos con precio mayor al especificado
     */
    List<Product> findByPriceGreaterThanAndActiveTrue(BigDecimal price);

    /**
     * Encuentra productos con stock en un rango específico.
     * 
     * @param minStock Stock mínimo
     * @param maxStock Stock máximo
     * @return Lista de productos con stock en el rango especificado
     */
    @Query("SELECT p FROM Product p WHERE p.stock BETWEEN :minStock AND :maxStock AND p.active = true")
    List<Product> findByStockRange(@Param("minStock") Integer minStock, 
                                   @Param("maxStock") Integer maxStock);

    /**
     * Busca productos por múltiples criterios con paginación.
     * Utiliza consulta dinámica para filtrar por nombre, categoría, rango de precios y disponibilidad.
     * 
     * @param name Nombre del producto (búsqueda parcial, case-insensitive)
     * @param category Categoría del producto (exacta, case-insensitive)
     * @param minPrice Precio mínimo (inclusive)
     * @param maxPrice Precio máximo (inclusive)
     * @param inStock Si true, solo productos con stock > 0
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página de productos que coinciden con los criterios especificados
     */
    @Query("""
        SELECT p FROM Product p 
        WHERE p.active = true 
        AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:inStock IS NULL OR (:inStock = true AND p.stock > 0) OR (:inStock = false))
        """)
    Page<Product> findProductsByCriteria(@Param("name") String name,
                                        @Param("category") String category,
                                        @Param("minPrice") BigDecimal minPrice,
                                        @Param("maxPrice") BigDecimal maxPrice,
                                        @Param("inStock") Boolean inStock,
                                        Pageable pageable);

    /**
     * Busca productos por nombre exacto que estén activos.
     * @param name Nombre exacto del producto
     * @return Lista de productos activos con el nombre especificado
     */
    List<Product> findByNameAndActiveTrue(String name);

    /**
     * Verifica si existe un producto con el nombre especificado (case insensitive).
     * 
     * @param name Nombre del producto a verificar
     * @return true si existe un producto con el nombre, false en caso contrario
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Busca productos por categoría que estén activos.
     * @param category Categoría a buscar
     * @return Lista de productos activos de la categoría especificada
     */
    List<Product> findByCategoryAndActiveTrue(String category);
}