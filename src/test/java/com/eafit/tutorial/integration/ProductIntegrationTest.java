package com.eafit.tutorial.integration;

import com.eafit.tutorial.dto.ApiResponse;
import com.eafit.tutorial.dto.CreateProductDTO;
import com.eafit.tutorial.dto.ProductDTO;
import com.eafit.tutorial.model.Product;
import com.eafit.tutorial.repository.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración end-to-end para la API de productos.
 * Prueba el flujo completo desde HTTP hasta base de datos.
 * 
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/products";
        
        // Limpiar la base de datos antes de cada test
        productRepository.deleteAll();
    }

    @Test
    void productLifecycle_CreateGetUpdateDelete_ShouldWorkCorrectly() {
        // 1. CREAR PRODUCTO
        CreateProductDTO createDTO = createValidCreateDTO();
        
        ResponseEntity<ApiResponse<ProductDTO>> createResponse = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            new HttpEntity<>(createDTO, createJsonHeaders()),
            new ParameterizedTypeReference<ApiResponse<ProductDTO>>() {}
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().isSuccess()).isTrue();
        
        ProductDTO createdProduct = createResponse.getBody().getData();
        assertThat(createdProduct.getId()).isNotNull();
        assertThat(createdProduct.getName()).isEqualTo("Laptop Test Integration");
        assertThat(createdProduct.getActive()).isTrue();

        Long productId = createdProduct.getId();

        // Verificar en base de datos
        var dbProduct = productRepository.findById(productId);
        assertThat(dbProduct).isPresent();
        assertThat(dbProduct.get().getName()).isEqualTo("Laptop Test Integration");

        // 2. OBTENER PRODUCTO POR ID
        ResponseEntity<ApiResponse<ProductDTO>> getResponse = restTemplate.exchange(
            baseUrl + "/" + productId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<ApiResponse<ProductDTO>>() {}
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().isSuccess()).isTrue();
        
        ProductDTO retrievedProduct = getResponse.getBody().getData();
        assertThat(retrievedProduct.getId()).isEqualTo(productId);
        assertThat(retrievedProduct.getName()).isEqualTo("Laptop Test Integration");

        // 3. ACTUALIZAR PRODUCTO
        CreateProductDTO updateDTO = createValidCreateDTO();
        updateDTO.setName("Laptop Updated Integration");
        updateDTO.setPrice(new BigDecimal("1499.99"));

        ResponseEntity<ApiResponse<ProductDTO>> updateResponse = restTemplate.exchange(
            baseUrl + "/" + productId,
            HttpMethod.PUT,
            new HttpEntity<>(updateDTO, createJsonHeaders()),
            new ParameterizedTypeReference<ApiResponse<ProductDTO>>() {}
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().isSuccess()).isTrue();
        
        ProductDTO updatedProduct = updateResponse.getBody().getData();
        assertThat(updatedProduct.getName()).isEqualTo("Laptop Updated Integration");
        assertThat(updatedProduct.getPrice()).isEqualTo(new BigDecimal("1499.99"));

        // Verificar actualización en base de datos
        var updatedDbProduct = productRepository.findById(productId);
        assertThat(updatedDbProduct).isPresent();
        assertThat(updatedDbProduct.get().getName()).isEqualTo("Laptop Updated Integration");
        assertThat(updatedDbProduct.get().getPrice()).isEqualTo(new BigDecimal("1499.99"));

        // 4. ELIMINAR PRODUCTO (soft delete)
        ResponseEntity<ApiResponse<Object>> deleteResponse = restTemplate.exchange(
            baseUrl + "/" + productId,
            HttpMethod.DELETE,
            null,
            new ParameterizedTypeReference<ApiResponse<Object>>() {}
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deleteResponse.getBody()).isNotNull();
        assertThat(deleteResponse.getBody().isSuccess()).isTrue();

        // Verificar soft delete en base de datos
        var deletedDbProduct = productRepository.findById(productId);
        assertThat(deletedDbProduct).isPresent();
        assertThat(deletedDbProduct.get().getActive()).isFalse(); // Soft delete

        // Verificar que no se puede obtener el producto eliminado
        ResponseEntity<ApiResponse<ProductDTO>> getDeletedResponse = restTemplate.exchange(
            baseUrl + "/" + productId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<ApiResponse<ProductDTO>>() {}
        );

        assertThat(getDeletedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void searchByCategory_ShouldReturnCorrectProducts() {
        // Crear productos de diferentes categorías
        createProductInDB("Laptop HP", "Laptop HP description", new BigDecimal("1200"), "Electrónicos", 10);
        createProductInDB("Mouse Logitech", "Mouse description", new BigDecimal("30"), "Electrónicos", 20);
        createProductInDB("Libro Java", "Java programming book", new BigDecimal("50"), "Libros", 15);

        // Buscar por categoría "Electrónicos"
        ResponseEntity<ApiResponse<List<ProductDTO>>> response = restTemplate.exchange(
            baseUrl + "/category/Electrónicos",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<ApiResponse<List<ProductDTO>>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        
        List<ProductDTO> electronics = response.getBody().getData();
        assertThat(electronics).hasSize(2);
        assertThat(electronics).allMatch(product -> product.getCategory().equals("Electrónicos"));
        assertThat(electronics).anyMatch(product -> product.getName().equals("Laptop HP"));
        assertThat(electronics).anyMatch(product -> product.getName().equals("Mouse Logitech"));
    }

    @Test
    void searchByPriceRange_ShouldReturnProductsInRange() {
        // Crear productos con diferentes precios
        createProductInDB("Producto Barato", "Producto económico", new BigDecimal("25"), "Varios", 10);
        createProductInDB("Producto Medio", "Producto precio medio", new BigDecimal("100"), "Varios", 5);
        createProductInDB("Producto Caro", "Producto costoso", new BigDecimal("500"), "Varios", 2);

        // Buscar productos en rango $50 - $200
        ResponseEntity<ApiResponse<List<ProductDTO>>> response = restTemplate.exchange(
            baseUrl + "/price-range?minPrice=50&maxPrice=200",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<ApiResponse<List<ProductDTO>>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        
        List<ProductDTO> productsInRange = response.getBody().getData();
        assertThat(productsInRange).hasSize(1);
        assertThat(productsInRange.get(0).getName()).isEqualTo("Producto Medio");
        assertThat(productsInRange.get(0).getPrice()).isEqualTo(new BigDecimal("100"));
    }

    @Test
    void createProduct_WithDuplicateName_ShouldReturnConflict() {
        // Crear primer producto
        createProductInDB("Producto Único", "Descripción", new BigDecimal("100"), "Categoría", 10);

        // Intentar crear producto con mismo nombre
        CreateProductDTO duplicateDTO = new CreateProductDTO();
        duplicateDTO.setName("Producto Único");
        duplicateDTO.setDescription("Otra descripción");
        duplicateDTO.setPrice(new BigDecimal("200"));
        duplicateDTO.setCategory("Otra Categoría");
        duplicateDTO.setStock(5);

        ResponseEntity<ApiResponse<ProductDTO>> response = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            new HttpEntity<>(duplicateDTO, createJsonHeaders()),
            new ParameterizedTypeReference<ApiResponse<ProductDTO>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("Producto Único");
    }

    @Test
    void createProduct_WithInvalidData_ShouldReturnBadRequest() {
        // DTO con datos inválidos
        CreateProductDTO invalidDTO = new CreateProductDTO();
        invalidDTO.setName(""); // Nombre vacío
        invalidDTO.setPrice(new BigDecimal("-10")); // Precio negativo
        // Categoría y stock faltantes

        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            new HttpEntity<>(invalidDTO, createJsonHeaders()),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        // Verificar que contiene errores de validación
        String responseBody = response.getBody();
        assertThat(responseBody).contains("success\":false");
        assertThat(responseBody).contains("errors");
    }

    @Test
    void getProduct_WithNonExistentId_ShouldReturnNotFound() {
        // Intentar obtener producto que no existe
        ResponseEntity<ApiResponse<ProductDTO>> response = restTemplate.exchange(
            baseUrl + "/999",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<ApiResponse<ProductDTO>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("Producto no encontrado");
    }

    @Test
    void updateStock_ShouldUpdateSuccessfully() {
        // Crear producto
        Product product = createProductInDB("Producto Stock", "Descripción", 
                                          new BigDecimal("100"), "Categoría", 10);

        // Actualizar stock
        ResponseEntity<ApiResponse<ProductDTO>> response = restTemplate.exchange(
            baseUrl + "/" + product.getId() + "/stock?stock=25",
            HttpMethod.PATCH,
            null,
            new ParameterizedTypeReference<ApiResponse<ProductDTO>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        
        ProductDTO updatedProduct = response.getBody().getData();
        assertThat(updatedProduct.getStock()).isEqualTo(25);

        // Verificar en base de datos
        var dbProduct = productRepository.findById(product.getId());
        assertThat(dbProduct).isPresent();
        assertThat(dbProduct.get().getStock()).isEqualTo(25);
    }

    // ===============================
    // Métodos auxiliares
    // ===============================

    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private CreateProductDTO createValidCreateDTO() {
        CreateProductDTO dto = new CreateProductDTO();
        dto.setName("Laptop Test Integration");
        dto.setDescription("Laptop de prueba para tests de integración");
        dto.setPrice(new BigDecimal("1299.99"));
        dto.setCategory("Electrónicos");
        dto.setStock(10);
        return dto;
    }

    private Product createProductInDB(String name, String description, BigDecimal price, 
                                    String category, Integer stock) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setStock(stock);
        product.setActive(true);
        
        return productRepository.save(product);
    }
}