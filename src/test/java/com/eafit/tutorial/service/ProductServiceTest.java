package com.eafit.tutorial.service;

import com.eafit.tutorial.exception.ProductAlreadyExistsException;
import com.eafit.tutorial.exception.ProductNotFoundException;
import com.eafit.tutorial.model.Product;
import com.eafit.tutorial.repository.ProductRepository;
import com.eafit.tutorial.util.ProductMapper;
import com.eafit.tutorial.util.ProductValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ProductServiceImpl.
 * Prueba la lógica de negocio y el manejo de excepciones.
 * 
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductValidator productValidator;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private Product savedProduct;

    @BeforeEach
    void setUp() {
        testProduct = createTestProduct();
        savedProduct = createSavedProduct();
    }

    // ===============================
    // Tests para createProduct
    // ===============================

    @Test
    void createProduct_WithValidProduct_ShouldReturnSavedProduct() {
        // Given
        when(productRepository.existsByNameIgnoreCaseAndIdNot(testProduct.getName(), -1L))
            .thenReturn(false);
        when(productRepository.save(testProduct)).thenReturn(savedProduct);

        // When
        Product result = productService.createProduct(testProduct);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Laptop Test");
        assertThat(result.getActive()).isTrue();

        verify(productRepository).existsByNameIgnoreCaseAndIdNot(testProduct.getName(), -1L);
        verify(productRepository).save(testProduct);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void createProduct_WithDuplicateName_ShouldThrowProductAlreadyExistsException() {
        // Given
        when(productRepository.existsByNameIgnoreCaseAndIdNot(testProduct.getName(), -1L))
            .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(testProduct))
            .isInstanceOf(ProductAlreadyExistsException.class)
            .hasMessageContaining("Laptop Test");

        verify(productRepository).existsByNameIgnoreCaseAndIdNot(testProduct.getName(), -1L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_WithNullProduct_ShouldThrowIllegalArgumentException() {
        // When & Then
        assertThatThrownBy(() -> productService.createProduct(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("El producto no puede ser null");

        verifyNoInteractions(productRepository);
    }

    // ===============================
    // Tests para getProductById
    // ===============================

    @Test
    void getProductById_WithExistingId_ShouldReturnProduct() {
        // Given
        Long productId = 1L;
        when(productRepository.findByIdAndActiveTrue(productId))
            .thenReturn(Optional.of(savedProduct));

        // When
        Optional<Product> result = productService.getProductById(productId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(productId);
        assertThat(result.get().getName()).isEqualTo("Laptop Test");

        verify(productRepository).findByIdAndActiveTrue(productId);
    }

    @Test
    void getProductById_WithNonExistentId_ShouldReturnEmpty() {
        // Given
        Long productId = 999L;
        when(productRepository.findByIdAndActiveTrue(productId))
            .thenReturn(Optional.empty());

        // When
        Optional<Product> result = productService.getProductById(productId);

        // Then
        assertThat(result).isEmpty();

        verify(productRepository).findByIdAndActiveTrue(productId);
    }

    @Test
    void getProductById_WithNullId_ShouldThrowIllegalArgumentException() {
        // When & Then
        assertThatThrownBy(() -> productService.getProductById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("El ID del producto no puede ser null");

        verifyNoInteractions(productRepository);
    }

    // ===============================
    // Tests para updateProduct
    // ===============================

    @Test
    void updateProduct_WithValidData_ShouldReturnUpdatedProduct() {
        // Given
        Long productId = 1L;
        Product updateData = createTestProduct();
        updateData.setName("Laptop Actualizada");
        updateData.setPrice(new BigDecimal("1499.99"));

        Product existingProduct = createSavedProduct();
        Product updatedProduct = createSavedProduct();
        updatedProduct.setName("Laptop Actualizada");
        updatedProduct.setPrice(new BigDecimal("1499.99"));

        when(productRepository.findByIdAndActiveTrue(productId))
            .thenReturn(Optional.of(existingProduct));
        when(productRepository.existsByNameIgnoreCaseAndIdNot("Laptop Actualizada", productId))
            .thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // When
        Product result = productService.updateProduct(productId, updateData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Laptop Actualizada");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("1499.99"));

        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productRepository).existsByNameIgnoreCaseAndIdNot("Laptop Actualizada", productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_WithNonExistentProduct_ShouldThrowProductNotFoundException() {
        // Given
        Long productId = 999L;
        Product updateData = createTestProduct();

        when(productRepository.findByIdAndActiveTrue(productId))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.updateProduct(productId, updateData))
            .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_WithDuplicateName_ShouldThrowProductAlreadyExistsException() {
        // Given
        Long productId = 1L;
        Product updateData = createTestProduct();
        updateData.setName("Nombre Duplicado");

        Product existingProduct = createSavedProduct();

        when(productRepository.findByIdAndActiveTrue(productId))
            .thenReturn(Optional.of(existingProduct));
        when(productRepository.existsByNameIgnoreCaseAndIdNot("Nombre Duplicado", productId))
            .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> productService.updateProduct(productId, updateData))
            .isInstanceOf(ProductAlreadyExistsException.class)
            .hasMessageContaining("Nombre Duplicado");

        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productRepository).existsByNameIgnoreCaseAndIdNot("Nombre Duplicado", productId);
        verify(productRepository, never()).save(any());
    }

    // ===============================
    // Tests para deleteProduct
    // ===============================

    @Test
    void deleteProduct_WithExistingProduct_ShouldMarkAsInactive() {
        // Given
        Long productId = 1L;
        Product existingProduct = createSavedProduct();
        existingProduct.setActive(true);

        Product inactiveProduct = createSavedProduct();
        inactiveProduct.setActive(false);

        when(productRepository.findByIdAndActiveTrue(productId))
            .thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(inactiveProduct);

        // When
        productService.deleteProduct(productId);

        // Then
        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productRepository).save(argThat(product -> !product.getActive()));
    }

    @Test
    void deleteProduct_WithNonExistentProduct_ShouldThrowProductNotFoundException() {
        // Given
        Long productId = 999L;

        when(productRepository.findByIdAndActiveTrue(productId))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.deleteProduct(productId))
            .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productRepository, never()).save(any());
    }

    // ===============================
    // Tests para getProductsByPriceRange
    // ===============================

    @Test
    void getProductsByPriceRange_WithValidRange_ShouldReturnProducts() {
        // Given
        BigDecimal minPrice = new BigDecimal("100.00");
        BigDecimal maxPrice = new BigDecimal("500.00");

        when(productRepository.findByPriceRange(minPrice, maxPrice))
            .thenReturn(java.util.Arrays.asList(savedProduct));

        // When
        var result = productService.getProductsByPriceRange(minPrice, maxPrice);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(savedProduct);

        verify(productRepository).findByPriceRange(minPrice, maxPrice);
    }

    @Test
    void getProductsByPriceRange_WithInvalidRange_ShouldThrowIllegalArgumentException() {
        // Given
        BigDecimal minPrice = new BigDecimal("500.00");
        BigDecimal maxPrice = new BigDecimal("100.00"); // menor que minPrice

        // When & Then
        assertThatThrownBy(() -> productService.getProductsByPriceRange(minPrice, maxPrice))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("El precio mínimo no puede ser mayor al precio máximo");

        verifyNoInteractions(productRepository);
    }

    @Test
    void getProductsByPriceRange_WithNullPrices_ShouldThrowIllegalArgumentException() {
        // When & Then
        assertThatThrownBy(() -> productService.getProductsByPriceRange(null, new BigDecimal("100.00")))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> productService.getProductsByPriceRange(new BigDecimal("100.00"), null))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(productRepository);
    }

    // ===============================
    // Tests para updateStock
    // ===============================

    @Test
    void updateStock_WithValidData_ShouldReturnUpdatedProduct() {
        // Given
        Long productId = 1L;
        Integer newStock = 25;
        Product existingProduct = createSavedProduct();
        Product updatedProduct = createSavedProduct();
        updatedProduct.setStock(newStock);

        when(productRepository.findByIdAndActiveTrue(productId))
            .thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // When
        Product result = productService.updateStock(productId, newStock);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStock()).isEqualTo(newStock);

        verify(productRepository).findByIdAndActiveTrue(productId);
        verify(productValidator).validateStockForCategory(existingProduct.getCategory(), newStock);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateStock_WithNegativeStock_ShouldThrowIllegalArgumentException() {
        // Given
        Long productId = 1L;
        Integer negativeStock = -5;

        // When & Then
        assertThatThrownBy(() -> productService.updateStock(productId, negativeStock))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("El stock no puede ser null o negativo");

        verifyNoInteractions(productRepository);
        verifyNoInteractions(productValidator);
    }

    @Test
    void updateStock_WithNullStock_ShouldThrowIllegalArgumentException() {
        // Given
        Long productId = 1L;

        // When & Then
        assertThatThrownBy(() -> productService.updateStock(productId, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("El stock no puede ser null o negativo");

        verifyNoInteractions(productRepository);
        verifyNoInteractions(productValidator);
    }

    @Test
    void updateStock_WithNonExistentProduct_ShouldThrowProductNotFoundException() {
        // Given
        Long productId = 999L;
        Integer newStock = 25;

        when(productRepository.findByIdAndActiveTrue(productId))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.updateStock(productId, newStock))
            .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findByIdAndActiveTrue(productId);
        verifyNoInteractions(productValidator);
        verify(productRepository, never()).save(any());
    }

    // ===============================
    // Métodos auxiliares
    // ===============================

    private Product createTestProduct() {
        Product product = new Product();
        product.setName("Laptop Test");
        product.setDescription("Laptop de prueba");
        product.setPrice(new BigDecimal("1299.99"));
        product.setCategory("Electrónicos");
        product.setStock(10);
        product.setActive(true);
        return product;
    }

    private Product createSavedProduct() {
        Product product = createTestProduct();
        product.setId(1L);
        return product;
    }
}