package com.eafit.tutorial.controller;

import com.eafit.tutorial.dto.CreateProductDTO;
import com.eafit.tutorial.dto.ProductDTO;
import com.eafit.tutorial.dto.UpdateProductDTO;
import com.eafit.tutorial.exception.ProductAlreadyExistsException;
import com.eafit.tutorial.exception.ProductNotFoundException;
import com.eafit.tutorial.model.Product;
import com.eafit.tutorial.service.ProductService;
import com.eafit.tutorial.util.ProductMapper;
import com.eafit.tutorial.util.ProductValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para ProductController.
 * Prueba los endpoints REST y el manejo de respuestas HTTP.
 * 
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductMapper productMapper;

    @MockBean
    private ProductValidator productValidator;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;
    private ProductDTO testProductDTO;
    private CreateProductDTO testCreateDTO;

    @BeforeEach
    void setUp() {
        testProduct = createValidProduct();
        testProductDTO = createValidProductDTO();
        testCreateDTO = createValidCreateDTO();

        // Configurar mocks comunes
        when(productMapper.toDTO(any(Product.class))).thenReturn(testProductDTO);
        when(productMapper.toEntity(any(CreateProductDTO.class))).thenReturn(testProduct);
    }

    // ===============================
    // Tests para GET /api/v1/products
    // ===============================

    @Test
    void getAllProducts_WithUnpagedTrue_ShouldReturnSimpleList() throws Exception {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        List<ProductDTO> productDTOs = Arrays.asList(testProductDTO);

        when(productService.getAllProducts()).thenReturn(products);
        when(productMapper.toDTOList(products)).thenReturn(productDTOs);

        // When & Then
        mockMvc.perform(get("/api/v1/products")
                .param("unpaged", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Productos obtenidos exitosamente"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Laptop Test"));

        verify(productService).getAllProducts();
        verify(productMapper).toDTOList(products);
    }

    @Test
    void getAllProducts_WithPagination_ShouldReturnPagedResponse() throws Exception {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct), pageRequest, 1);
        List<ProductDTO> productDTOs = Arrays.asList(testProductDTO);

        when(productService.getAllProducts(any(PageRequest.class))).thenReturn(productPage);
        when(productMapper.toDTOList(anyList())).thenReturn(productDTOs);

        // When & Then
        mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "20")
                .param("sort", "name")
                .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.page.number").value(0))
                .andExpect(jsonPath("$.data.page.size").value(20))
                .andExpect(jsonPath("$.data.page.totalElements").value(1))
                .andExpect(jsonPath("$.data.page.totalPages").value(1));

        verify(productService).getAllProducts(any(PageRequest.class));
        verify(productMapper).toDTOList(anyList());
    }

    @Test
    void getAllProducts_WithInvalidPageParams_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/products")
                .param("page", "-1")) // Página negativa
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/v1/products")
                .param("size", "0")) // Tamaño 0
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(productService);
    }

    // ===============================
    // Tests para GET /api/v1/products/{id}
    // ===============================

    @Test
    void getProductById_WithExistingId_ShouldReturnProduct() throws Exception {
        // Given
        Long productId = 1L;
        when(productService.getProductById(productId)).thenReturn(Optional.of(testProduct));

        // When & Then
        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Producto obtenido exitosamente"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Laptop Test"));

        verify(productService).getProductById(productId);
        verify(productMapper).toDTO(testProduct);
    }

    @Test
    void getProductById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Given
        Long productId = 999L;
        when(productService.getProductById(productId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Producto no encontrado"));

        verify(productService).getProductById(productId);
        verifyNoInteractions(productMapper);
    }

    @Test
    void getProductById_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/products/{id}", 0)) // ID inválido
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(productService);
    }

    // ===============================
    // Tests para POST /api/v1/products
    // ===============================

    @Test
    void createProduct_WithValidData_ShouldReturnCreated() throws Exception {
        // Given
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);
        doNothing().when(productValidator).validateForCreation(any(CreateProductDTO.class));

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Producto creado exitosamente"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Laptop Test"));

        verify(productValidator).validateForCreation(testCreateDTO);
        verify(productMapper).toEntity(testCreateDTO);
        verify(productService).createProduct(any(Product.class));
        verify(productMapper).toDTO(testProduct);
    }

    @Test
    void createProduct_WithValidationErrors_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateProductDTO invalidDTO = new CreateProductDTO();
        // DTO vacío debería fallar la validación

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").exists());

        verifyNoInteractions(productService);
    }

    @Test
    void createProduct_WithDuplicateName_ShouldReturnConflict() throws Exception {
        // Given
        doNothing().when(productValidator).validateForCreation(any(CreateProductDTO.class));
        when(productService.createProduct(any(Product.class)))
                .thenThrow(ProductAlreadyExistsException.forProductName("Laptop Test"));

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Laptop Test")));

        verify(productValidator).validateForCreation(testCreateDTO);
        verify(productMapper).toEntity(testCreateDTO);
        verify(productService).createProduct(any(Product.class));
        verifyNoMoreInteractions(productMapper);
    }

    // ===============================
    // Tests para PUT /api/v1/products/{id}
    // ===============================

    @Test
    void updateProduct_WithValidData_ShouldReturnUpdated() throws Exception {
        // Given
        Long productId = 1L;
        when(productService.updateProduct(eq(productId), any(Product.class))).thenReturn(testProduct);

        // When & Then
        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Producto actualizado exitosamente"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(productMapper).toEntity(testCreateDTO);
        verify(productService).updateProduct(eq(productId), any(Product.class));
        verify(productMapper).toDTO(testProduct);
    }

    @Test
    void updateProduct_WithNonExistentProduct_ShouldReturnNotFound() throws Exception {
        // Given
        Long productId = 999L;
        when(productService.updateProduct(eq(productId), any(Product.class)))
                .thenThrow(new ProductNotFoundException(productId));

        // When & Then
        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        verify(productMapper).toEntity(testCreateDTO);
        verify(productService).updateProduct(eq(productId), any(Product.class));
        verifyNoMoreInteractions(productMapper);
    }

    @Test
    void updateProduct_WithValidationErrors_ShouldReturnBadRequest() throws Exception {
        // Given
        UpdateProductDTO invalidDTO = new UpdateProductDTO();
        invalidDTO.setName(""); // Nombre vacío debería fallar

        // When & Then
        mockMvc.perform(put("/api/v1/products/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").exists());

        verifyNoInteractions(productService);
    }

    // ===============================
    // Tests para DELETE /api/v1/products/{id}
    // ===============================

    @Test
    void deleteProduct_WithExistingProduct_ShouldReturnSuccess() throws Exception {
        // Given
        Long productId = 1L;
        doNothing().when(productService).deleteProduct(productId);

        // When & Then
        mockMvc.perform(delete("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Producto eliminado exitosamente"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(productService).deleteProduct(productId);
    }

    @Test
    void deleteProduct_WithNonExistentProduct_ShouldReturnNotFound() throws Exception {
        // Given
        Long productId = 999L;
        doThrow(new ProductNotFoundException(productId)).when(productService).deleteProduct(productId);

        // When & Then
        mockMvc.perform(delete("/api/v1/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        verify(productService).deleteProduct(productId);
    }

    // ===============================
    // Métodos auxiliares
    // ===============================

    private Product createValidProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop Test");
        product.setDescription("Laptop de prueba para testing");
        product.setPrice(new BigDecimal("1299.99"));
        product.setCategory("Electrónicos");
        product.setStock(10);
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    private ProductDTO createValidProductDTO() {
        ProductDTO dto = new ProductDTO();
        dto.setId(1L);
        dto.setName("Laptop Test");
        dto.setDescription("Laptop de prueba para testing");
        dto.setPrice(new BigDecimal("1299.99"));
        dto.setCategory("Electrónicos");
        dto.setStock(10);
        dto.setActive(true);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }

    private CreateProductDTO createValidCreateDTO() {
        CreateProductDTO dto = new CreateProductDTO();
        dto.setName("Laptop Test");
        dto.setDescription("Laptop de prueba para testing");
        dto.setPrice(new BigDecimal("1299.99"));
        dto.setCategory("Electrónicos");
        dto.setStock(10);
        return dto;
    }
}