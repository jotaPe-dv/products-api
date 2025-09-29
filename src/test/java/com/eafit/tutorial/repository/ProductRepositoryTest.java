package com.eafit.tutorial.repository;

import com.eafit.tutorial.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para ProductRepository.
 * Prueba las consultas personalizadas y operaciones de base de datos.
 * 
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Product activeProduct1;
    private Product activeProduct2;
    private Product inactiveProduct;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos
        productRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Crear productos de prueba
        activeProduct1 = createTestProduct("Laptop Dell", "Laptop Dell XPS 13", 
                                         new BigDecimal("1299.99"), "Electrónicos", 10, true);
        
        activeProduct2 = createTestProduct("Mouse Logitech", "Mouse inalámbrico Logitech", 
                                         new BigDecimal("29.99"), "Electrónicos", 5, true);
        
        inactiveProduct = createTestProduct("Producto Inactivo", "Producto eliminado", 
                                          new BigDecimal("99.99"), "Varios", 0, false);

        // Productos adicionales para diferentes categorías y precios
        createTestProduct("Libro Java", "Libro sobre programación Java", 
                         new BigDecimal("45.99"), "Libros", 20, true);
        
        createTestProduct("Camiseta Nike", "Camiseta deportiva Nike", 
                         new BigDecimal("39.99"), "Ropa", 2, true); // Stock bajo
        
        createTestProduct("Auriculares Sony", "Auriculares Sony WH-1000XM4", 
                         new BigDecimal("299.99"), "Electrónicos", 15, true);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByActiveTrue_ShouldReturnOnlyActiveProducts() {
        // When
        List<Product> activeProducts = productRepository.findByActiveTrue();

        // Then
        assertThat(activeProducts).isNotEmpty();
        assertThat(activeProducts).hasSize(5); // 5 productos activos
        assertThat(activeProducts).allMatch(Product::getActive);
        assertThat(activeProducts).noneMatch(product -> 
            product.getName().equals("Producto Inactivo"));
    }

    @Test
    void findByCategoryIgnoreCaseAndActiveTrue_ShouldReturnProductsInCategory() {
        // When - buscar en diferentes variaciones de mayúsculas
        List<Product> electronicsLower = productRepository
            .findByCategoryIgnoreCaseAndActiveTrue("electrónicos");
        List<Product> electronicsUpper = productRepository
            .findByCategoryIgnoreCaseAndActiveTrue("ELECTRÓNICOS");
        List<Product> electronicsMixed = productRepository
            .findByCategoryIgnoreCaseAndActiveTrue("ElEcTrÓnIcOs");

        // Then
        assertThat(electronicsLower).hasSize(3); // Laptop, Mouse, Auriculares
        assertThat(electronicsUpper).hasSize(3);
        assertThat(electronicsMixed).hasSize(3);
        
        assertThat(electronicsLower).allMatch(product -> 
            product.getCategory().equalsIgnoreCase("Electrónicos"));
        assertThat(electronicsLower).allMatch(Product::getActive);
    }

    @Test
    void findByCategoryIgnoreCaseAndActiveTrue_WithNonExistentCategory_ShouldReturnEmpty() {
        // When
        List<Product> products = productRepository
            .findByCategoryIgnoreCaseAndActiveTrue("CategoriaInexistente");

        // Then
        assertThat(products).isEmpty();
    }

    @Test
    void findByPriceRange_ShouldReturnProductsInRange() {
        // When - buscar productos entre $30 y $100
        List<Product> productsInRange = productRepository
            .findByPriceRange(new BigDecimal("30.00"), new BigDecimal("100.00"));

        // Then
        assertThat(productsInRange).isNotEmpty();
        assertThat(productsInRange).allMatch(product -> 
            product.getPrice().compareTo(new BigDecimal("30.00")) >= 0 &&
            product.getPrice().compareTo(new BigDecimal("100.00")) <= 0);
        
        // Verificar que incluye Mouse Logitech (29.99 no debería estar) y otros en rango
        assertThat(productsInRange).anyMatch(product -> 
            product.getName().equals("Libro Java"));
        assertThat(productsInRange).anyMatch(product -> 
            product.getName().equals("Camiseta Nike"));
        assertThat(productsInRange).noneMatch(product -> 
            product.getName().equals("Mouse Logitech")); // Precio menor al rango
    }

    @Test
    void findByPriceRange_WithExactBoundaries_ShouldIncludeBoundaryProducts() {
        // When - rango exacto que incluye un producto en el límite
        List<Product> products = productRepository
            .findByPriceRange(new BigDecimal("29.99"), new BigDecimal("45.99"));

        // Then
        assertThat(products).isNotEmpty();
        assertThat(products).anyMatch(product -> 
            product.getName().equals("Mouse Logitech")); // Límite inferior
        assertThat(products).anyMatch(product -> 
            product.getName().equals("Libro Java")); // Límite superior
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnMatchingProducts() {
        // When - buscar productos que contengan "laptop" (case insensitive)
        List<Product> laptopProducts = productRepository
            .findByNameContainingIgnoreCase("laptop");
        
        List<Product> mouseProducts = productRepository
            .findByNameContainingIgnoreCase("MOUSE");

        // Then
        assertThat(laptopProducts).hasSize(1);
        assertThat(laptopProducts.get(0).getName()).isEqualTo("Laptop Dell");
        
        assertThat(mouseProducts).hasSize(1);
        assertThat(mouseProducts.get(0).getName()).isEqualTo("Mouse Logitech");
    }

    @Test
    void findByNameContainingIgnoreCase_WithPartialMatch_ShouldReturnMatches() {
        // When - buscar por parte del nombre
        List<Product> products = productRepository.findByNameContainingIgnoreCase("sony");

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Auriculares Sony");
    }

    @Test
    void findByNameContainingIgnoreCase_WithNoMatches_ShouldReturnEmpty() {
        // When
        List<Product> products = productRepository
            .findByNameContainingIgnoreCase("ProductoInexistente");

        // Then
        assertThat(products).isEmpty();
    }

    @Test
    void existsByNameIgnoreCaseAndIdNot_ShouldReturnTrueForExistingName() {
        // When - verificar si existe otro producto con el mismo nombre
        boolean exists = productRepository
            .existsByNameIgnoreCaseAndIdNot("LAPTOP DELL", -1L);
        
        boolean existsExcludingSameProduct = productRepository
            .existsByNameIgnoreCaseAndIdNot("Laptop Dell", activeProduct1.getId());

        // Then
        assertThat(exists).isTrue(); // Existe otro producto con ese nombre
        assertThat(existsExcludingSameProduct).isFalse(); // No existe excluyendo el mismo
    }

    @Test
    void existsByNameIgnoreCaseAndIdNot_WithNonExistentName_ShouldReturnFalse() {
        // When
        boolean exists = productRepository
            .existsByNameIgnoreCaseAndIdNot("Producto Inexistente", -1L);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByStockLessThanAndActiveTrue_ShouldReturnLowStockProducts() {
        // When - buscar productos con stock menor a 10
        List<Product> lowStockProducts = productRepository
            .findByStockLessThanAndActiveTrue(10);

        // Then
        assertThat(lowStockProducts).isNotEmpty();
        assertThat(lowStockProducts).allMatch(product -> product.getStock() < 10);
        assertThat(lowStockProducts).allMatch(Product::getActive);
        
        // Debería incluir Mouse (5) y Camiseta (2)
        assertThat(lowStockProducts).anyMatch(product -> 
            product.getName().equals("Mouse Logitech"));
        assertThat(lowStockProducts).anyMatch(product -> 
            product.getName().equals("Camiseta Nike"));
    }

    @Test
    void countByCategory_ShouldReturnCorrectCount() {
        // When
        Long electronicsCount = productRepository.countByCategory("Electrónicos");
        Long booksCount = productRepository.countByCategory("Libros");
        Long clothingCount = productRepository.countByCategory("Ropa");
        Long nonExistentCount = productRepository.countByCategory("CategoriaInexistente");

        // Then
        assertThat(electronicsCount).isEqualTo(3L); // Laptop, Mouse, Auriculares
        assertThat(booksCount).isEqualTo(1L); // Libro Java
        assertThat(clothingCount).isEqualTo(1L); // Camiseta Nike
        assertThat(nonExistentCount).isEqualTo(0L);
    }

    @Test
    void findByIdAndActiveTrue_ShouldReturnActiveProduct() {
        // When
        var foundActive = productRepository.findByIdAndActiveTrue(activeProduct1.getId());
        var foundInactive = productRepository.findByIdAndActiveTrue(inactiveProduct.getId());
        var foundNonExistent = productRepository.findByIdAndActiveTrue(999L);

        // Then
        assertThat(foundActive).isPresent();
        assertThat(foundActive.get().getName()).isEqualTo("Laptop Dell");
        
        assertThat(foundInactive).isEmpty(); // Producto inactivo no debería encontrarse
        assertThat(foundNonExistent).isEmpty(); // Producto inexistente
    }

    /**
     * Método auxiliar para crear productos de prueba.
     */
    private Product createTestProduct(String name, String description, BigDecimal price, 
                                    String category, Integer stock, Boolean active) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setStock(stock);
        product.setActive(active);
        
        return entityManager.persistAndFlush(product);
    }
}