package com.eafit.tutorial.controller;

import com.eafit.tutorial.dto.ApiResponse;
import com.eafit.tutorial.dto.CreateProductDTO;
import com.eafit.tutorial.dto.ErrorResponse;
import com.eafit.tutorial.dto.PagedResponse;
import com.eafit.tutorial.dto.ProductDTO;
import com.eafit.tutorial.dto.UpdateProductDTO;
import com.eafit.tutorial.exception.ProductAlreadyExistsException;
import com.eafit.tutorial.exception.ProductNotFoundException;
import com.eafit.tutorial.exception.ValidationException;
import com.eafit.tutorial.model.Product;
import com.eafit.tutorial.service.ProductService;
import com.eafit.tutorial.util.ProductMapper;
import com.eafit.tutorial.util.ProductValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", 
     description = "API para gestión de productos. Permite realizar operaciones CRUD completas, " +
                   "búsquedas por diferentes criterios (categoría, nombre, rango de precios), " +
                   "gestión de inventario y consultas paginadas. Incluye validaciones de negocio " +
                   "y manejo de errores estándar.")
@Validated
@CrossOrigin
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductValidator productValidator;

    @GetMapping
    @Operation(summary = "Obtener todos los productos")
    public ResponseEntity<ApiResponse<Object>> getAllProducts(
            @Parameter(description = "Número de página (base 0)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "Tamaño de página", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) int size,
            
            @Parameter(description = "Campo para ordenamiento", example = "id")
            @RequestParam(defaultValue = "id") String sort,
            
            @Parameter(description = "Dirección del ordenamiento", example = "asc")
            @RequestParam(defaultValue = "asc") String direction,
            
            @Parameter(description = "Si true, retorna todos los productos sin paginación", example = "false")
            @RequestParam(defaultValue = "false") boolean unpaged) {
        
        try {
            logger.debug("Iniciando getAllProducts con parámetros: page={}, size={}, sort={}, direction={}, unpaged={}", 
                        page, size, sort, direction, unpaged);
            
            if (unpaged) {
                // Retornar lista simple sin paginación
                List<Product> products = productService.getAllProducts();
                List<ProductDTO> productDTOs = productMapper.toDTOList(products);
                
                logger.info("Retornando {} productos sin paginación", productDTOs.size());
                
                ApiResponse<Object> response = ApiResponse.success(productDTOs, "Productos obtenidos exitosamente");
                return ResponseEntity.ok(response);
                
            } else {
                // Retornar lista paginada
                Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? 
                    Sort.Direction.DESC : Sort.Direction.ASC;
                
                Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
                Page<Product> productPage = productService.getAllProducts(pageable);
                
                // Mapear productos a DTOs
                List<ProductDTO> productDTOs = productMapper.toDTOList(productPage.getContent());
                
                // Crear página con DTOs
                Page<ProductDTO> dtoPage = productPage.map(productMapper::toDTO);
                PagedResponse<ProductDTO> pagedResponse = PagedResponse.of(dtoPage);
                
                logger.info("Retornando página {} de {} con {} productos", 
                           page, productPage.getTotalPages(), productDTOs.size());
                
                ApiResponse<Object> response = ApiResponse.success(pagedResponse, "Productos obtenidos exitosamente");
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception ex) {
            logger.error("Error al obtener productos: {}", ex.getMessage(), ex);
            
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<Object> response = ApiResponse.error("Error interno al obtener productos", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Obtiene un producto por su ID.
     * 
     * @param id ID del producto a buscar
     * @return ProductDTO del producto encontrado
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener producto por ID",
        description = """
            Obtiene un producto específico mediante su identificador único.
            
            **Validaciones:**
            - El ID debe ser un número entero positivo (mayor a 0)
            - El producto debe existir en el sistema
            - El producto debe estar activo
            
            **Casos de uso:**
            - Consultar detalles completos de un producto específico
            - Verificar información antes de realizar operaciones (editar, eliminar)
            - Mostrar página de detalle de producto en frontend
            
            **Ejemplos de uso:**
            - `/api/v1/products/1` - Obtiene el producto con ID 1
            - `/api/v1/products/999` - Si no existe, retorna 404
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Producto encontrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Producto encontrado",
                    summary = "Respuesta exitosa con datos del producto",
                    value = """
                        {
                          "success": true,
                          "message": "Producto encontrado exitosamente",
                          "data": {
                            "id": 1,
                            "name": "iPhone 15 Pro",
                            "description": "Smartphone Apple con chip A17 Pro y sistema de cámaras avanzado",
                            "price": 999.99,
                            "category": "Electrónicos",
                            "stock": 50,
                            "active": true,
                            "createdAt": "2025-09-29 10:30:00",
                            "updatedAt": "2025-09-29 10:30:00"
                          },
                          "timestamp": "2025-09-29 14:45:20",
                          "statusCode": 200
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "ID inválido (debe ser mayor a 0)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "ID inválido",
                    summary = "Error de validación por ID inválido",
                    value = """
                        {
                          "success": false,
                          "message": "Parámetros inválidos",
                          "data": null,
                          "error": {
                            "code": "VALIDATION_ERROR",
                            "message": "El ID debe ser mayor a 0",
                            "details": {
                              "field": "id",
                              "rejectedValue": "0",
                              "validationMessage": "debe ser mayor que o igual a 1"
                            }
                          },
                          "timestamp": "2025-09-29 14:45:20",
                          "statusCode": 400
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Producto no encontrado",
                    summary = "Error cuando el producto no existe o está inactivo",
                    value = """
                        {
                          "success": false,
                          "message": "Producto no encontrado",
                          "data": null,
                          "error": {
                            "code": "PRODUCT_NOT_FOUND",
                            "message": "No se encontró un producto con ID: 999"
                          },
                          "timestamp": "2025-09-29 14:45:20",
                          "statusCode": 404
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Error interno",
                    summary = "Error inesperado del servidor",
                    value = """
                        {
                          "success": false,
                          "message": "Error interno del servidor",
                          "data": null,
                          "error": {
                            "code": "INTERNAL_ERROR",
                            "message": "Error interno del servidor"
                          },
                          "timestamp": "2025-09-29 14:45:20",
                          "statusCode": 500
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(
            @Parameter(description = "ID del producto a buscar", example = "1", required = true)
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id) {
        
        try {
            logger.debug("Iniciando getProductById con ID: {}", id);
            
            // Buscar producto por ID
            Optional<Product> productOpt = productService.getProductById(id);
            
            if (productOpt.isEmpty()) {
                throw new ProductNotFoundException("No se encontró un producto con ID: " + id);
            }
            
            Product product = productOpt.get();
            
            // Mapear a DTO
            ProductDTO productDTO = productMapper.toDTO(product);
            
            logger.info("Producto encontrado exitosamente: ID={}, nombre='{}'", 
                       product.getId(), product.getName());
            
            ApiResponse<ProductDTO> response = ApiResponse.success(productDTO, "Producto obtenido exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (ProductNotFoundException ex) {
            logger.warn("Producto no encontrado: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("PRODUCT_NOT_FOUND", ex.getMessage());
            ApiResponse<ProductDTO> response = ApiResponse.error("Producto no encontrado", errorDetails);
            return ResponseEntity.status(404).body(response);
            
        } catch (Exception ex) {
            logger.error("Error al obtener producto por ID {}: {}", id, ex.getMessage(), ex);
            
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<ProductDTO> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Crea un nuevo producto.
     * 
     * @param createProductDTO Datos del producto a crear
     * @return ProductDTO del producto creado
     */
    @PostMapping
    @Operation(
        summary = "Crear nuevo producto",
        description = """
            Crea un nuevo producto en el sistema con los datos proporcionados.
            
            **Validaciones aplicadas:**
            - **name**: Requerido, no vacío, longitud entre 2-100 caracteres
            - **description**: Opcional, máximo 500 caracteres
            - **price**: Requerido, debe ser mayor a 0, máximo 2 decimales
            - **category**: Requerida, no vacía, longitud entre 2-50 caracteres
            - **stock**: Requerido, debe ser mayor o igual a 0
            
            **Validaciones de negocio:**
            - No puede existir otro producto con el mismo nombre (case-insensitive)
            - Todos los campos requeridos deben estar presentes
            - Los valores numéricos deben estar en rangos válidos
            
            **Comportamiento:**
            - El producto se crea con estado activo por defecto
            - Se asigna automáticamente un ID único
            - Se registran timestamps de creación y actualización
            
            **Ejemplos de uso:**
            - Registrar nuevos productos en el catálogo
            - Importación masiva de productos (llamadas individuales)
            - Creación desde interfaces de administración
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Producto creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Producto creado",
                    summary = "Respuesta exitosa de creación de producto",
                    value = """
                        {
                          "success": true,
                          "message": "Producto creado exitosamente",
                          "data": {
                            "id": 1,
                            "name": "iPhone 15 Pro Max",
                            "description": "Smartphone Apple con chip A17 Pro, pantalla de 6.7 pulgadas y sistema de cámaras profesional",
                            "price": 1199.99,
                            "category": "Electrónicos",
                            "stock": 25,
                            "active": true,
                            "createdAt": "2025-09-29 15:00:00",
                            "updatedAt": "2025-09-29 15:00:00"
                          },
                          "timestamp": "2025-09-29 15:00:00",
                          "statusCode": 201
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos inválidos o producto ya existe",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = {
                    @ExampleObject(
                        name = "Validación fallida",
                        summary = "Error de validación en los datos de entrada",
                        value = """
                            {
                              "success": false,
                              "message": "Datos de entrada inválidos",
                              "data": null,
                              "error": {
                                "code": "VALIDATION_ERROR",
                                "message": "Errores de validación en los campos",
                                "details": {
                                  "name": "El nombre es requerido",
                                  "price": "El precio debe ser mayor a 0",
                                  "category": "La categoría es requerida"
                                }
                              },
                              "timestamp": "2025-09-29 15:00:00",
                              "statusCode": 400
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Producto ya existe",
                        summary = "Error cuando el producto ya existe",
                        value = """
                            {
                              "success": false,
                              "message": "El producto ya existe",
                              "data": null,
                              "error": {
                                "code": "PRODUCT_ALREADY_EXISTS",
                                "message": "Ya existe un producto con el nombre: iPhone 15 Pro Max"
                              },
                              "timestamp": "2025-09-29 15:00:00",
                              "statusCode": 400
                            }
                            """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Error interno",
                    summary = "Error inesperado del servidor",
                    value = """
                        {
                          "success": false,
                          "message": "Error interno del servidor",
                          "data": null,
                          "error": {
                            "code": "INTERNAL_ERROR",
                            "message": "Error interno del servidor"
                          },
                          "timestamp": "2025-09-29 15:00:00",
                          "statusCode": 500
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @Parameter(description = "Datos del producto a crear", required = true)
            @Valid @RequestBody CreateProductDTO createProductDTO) {
        
        try {
            logger.debug("Iniciando createProduct con datos: {}", createProductDTO);
            
            // Validar con reglas de negocio específicas
            productValidator.validateForCreation(createProductDTO);
            
            // Mapear DTO a entidad
            Product product = productMapper.toEntity(createProductDTO);
            
            // Crear producto usando el service
            Product createdProduct = productService.createProduct(product);
            
            // Mapear entidad creada a DTO de respuesta
            ProductDTO productDTO = productMapper.toDTO(createdProduct);
            
            logger.info("Producto creado exitosamente: ID={}, nombre='{}'", 
                       createdProduct.getId(), createdProduct.getName());
            
            ApiResponse<ProductDTO> response = ApiResponse.success(productDTO, "Producto creado exitosamente");
            return ResponseEntity.status(201).body(response);
            
        } catch (ProductAlreadyExistsException ex) {
            logger.warn("Intento de crear producto duplicado: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("PRODUCT_ALREADY_EXISTS", ex.getMessage());
            ApiResponse<ProductDTO> response = ApiResponse.error("El producto ya existe", errorDetails);
            return ResponseEntity.status(400).body(response);
            
        } catch (ValidationException ex) {
            logger.warn("Error de validación al crear producto: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("VALIDATION_ERROR", ex.getMessage());
            ApiResponse<ProductDTO> response = ApiResponse.error("Datos de entrada inválidos", errorDetails);
            return ResponseEntity.status(400).body(response);
            
        } catch (Exception ex) {
            logger.error("Error al crear producto: {}", ex.getMessage(), ex);
            
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<ProductDTO> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Actualiza un producto existente.
     * 
     * @param id ID del producto a actualizar
     * @param updateProductDTO Datos del producto a actualizar
     * @return ProductDTO del producto actualizado
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar producto existente",
        description = """
            Actualiza un producto existente con los datos proporcionados.
            
            **Validaciones de entrada:**
            - **ID**: Debe ser un número entero positivo (mayor a 0)
            - **name**: Opcional, pero si se proporciona debe cumplir reglas (2-100 caracteres)
            - **description**: Opcional, máximo 500 caracteres
            - **price**: Opcional, pero si se proporciona debe ser mayor a 0
            - **category**: Opcional, pero si se proporciona debe cumplir reglas (2-50 caracteres)
            - **stock**: Opcional, pero si se proporciona debe ser mayor o igual a 0
            
            **Validaciones de negocio:**
            - El producto debe existir en el sistema
            - Si se actualiza el nombre, no debe coincidir con otro producto existente
            - Solo se actualizan los campos que se envían (actualización parcial)
            - Los campos no enviados mantienen su valor actual
            
            **Comportamiento:**
            - Actualización parcial: solo campos enviados son modificados
            - Se mantiene el ID y timestamps de creación
            - Se actualiza automáticamente el timestamp de modificación
            - El estado activo/inactivo se puede modificar
            
            **Ejemplos de uso:**
            - Actualizar precio de productos
            - Modificar stock después de ventas/reposición
            - Actualizar descripciones y categorías
            - Activar/desactivar productos
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Producto actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Producto actualizado",
                    summary = "Respuesta exitosa de actualización de producto",
                    value = """
                        {
                          "success": true,
                          "message": "Producto actualizado exitosamente",
                          "data": {
                            "id": 1,
                            "name": "iPhone 15 Pro Max",
                            "description": "Smartphone Apple con chip A17 Pro, pantalla de 6.7 pulgadas y sistema de cámaras profesional. Precio actualizado.",
                            "price": 1099.99,
                            "category": "Electrónicos",
                            "stock": 15,
                            "active": true,
                            "createdAt": "2025-09-29 10:30:00",
                            "updatedAt": "2025-09-29 15:15:00"
                          },
                          "timestamp": "2025-09-29 15:15:00",
                          "statusCode": 200
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos inválidos o conflicto de nombre",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = {
                    @ExampleObject(
                        name = "ID inválido",
                        summary = "Error de validación por ID inválido",
                        value = """
                            {
                              "success": false,
                              "message": "Parámetros inválidos",
                              "data": null,
                              "error": {
                                "code": "VALIDATION_ERROR",
                                "message": "El ID debe ser mayor a 0",
                                "details": {
                                  "field": "id",
                                  "rejectedValue": "0",
                                  "validationMessage": "debe ser mayor que o igual a 1"
                                }
                              },
                              "timestamp": "2025-09-29 15:15:00",
                              "statusCode": 400
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Validación fallida",
                        summary = "Error de validación en los datos de actualización",
                        value = """
                            {
                              "success": false,
                              "message": "Datos de entrada inválidos",
                              "data": null,
                              "error": {
                                "code": "VALIDATION_ERROR",
                                "message": "Errores de validación en los campos",
                                "details": {
                                  "price": "El precio debe ser mayor a 0",
                                  "stock": "El stock no puede ser negativo"
                                }
                              },
                              "timestamp": "2025-09-29 15:15:00",
                              "statusCode": 400
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Nombre duplicado",
                        summary = "Error cuando el nuevo nombre ya existe",
                        value = """
                            {
                              "success": false,
                              "message": "El producto ya existe",
                              "data": null,
                              "error": {
                                "code": "PRODUCT_ALREADY_EXISTS",
                                "message": "Ya existe un producto con el nombre: Samsung Galaxy S24"
                              },
                              "timestamp": "2025-09-29 15:15:00",
                              "statusCode": 400
                            }
                            """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Producto no encontrado",
                    summary = "Error cuando el producto a actualizar no existe",
                    value = """
                        {
                          "success": false,
                          "message": "Producto no encontrado",
                          "data": null,
                          "error": {
                            "code": "PRODUCT_NOT_FOUND",
                            "message": "No se encontró un producto con ID: 999"
                          },
                          "timestamp": "2025-09-29 15:15:00",
                          "statusCode": 404
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Error interno",
                    summary = "Error inesperado del servidor",
                    value = """
                        {
                          "success": false,
                          "message": "Error interno del servidor",
                          "data": null,
                          "error": {
                            "code": "INTERNAL_ERROR",
                            "message": "Error interno del servidor"
                          },
                          "timestamp": "2025-09-29 15:15:00",
                          "statusCode": 500
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @Parameter(description = "ID del producto a actualizar", example = "1", required = true)
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id,
            
            @Parameter(description = "Datos del producto a actualizar", required = true)
            @Valid @RequestBody UpdateProductDTO updateProductDTO) {
        
        try {
            logger.debug("Iniciando updateProduct con ID: {} y datos: {}", id, updateProductDTO);
            
            // Mapear DTO a entidad
            Product productUpdates = productMapper.toEntity(updateProductDTO);
            
            // Actualizar producto usando el service
            Product updatedProduct = productService.updateProduct(id, productUpdates);
            
            // Mapear entidad actualizada a DTO de respuesta
            ProductDTO productDTO = productMapper.toDTO(updatedProduct);
            
            logger.info("Producto actualizado exitosamente: ID={}, nombre='{}'", 
                       updatedProduct.getId(), updatedProduct.getName());
            
            ApiResponse<ProductDTO> response = ApiResponse.success(productDTO, "Producto actualizado exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (ProductNotFoundException ex) {
            logger.warn("Producto no encontrado para actualizar: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("PRODUCT_NOT_FOUND", ex.getMessage());
            ApiResponse<ProductDTO> response = ApiResponse.error("Producto no encontrado", errorDetails);
            return ResponseEntity.status(404).body(response);
            
        } catch (ProductAlreadyExistsException ex) {
            logger.warn("Intento de actualizar con nombre duplicado: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("PRODUCT_ALREADY_EXISTS", ex.getMessage());
            ApiResponse<ProductDTO> response = ApiResponse.error("El producto ya existe", errorDetails);
            return ResponseEntity.status(400).body(response);
            
        } catch (ValidationException ex) {
            logger.warn("Error de validación al actualizar producto: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("VALIDATION_ERROR", ex.getMessage());
            ApiResponse<ProductDTO> response = ApiResponse.error("Datos de entrada inválidos", errorDetails);
            return ResponseEntity.status(400).body(response);
            
        } catch (Exception ex) {
            logger.error("Error al actualizar producto con ID {}: {}", id, ex.getMessage(), ex);
            
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<ProductDTO> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Elimina un producto por su ID.
     * 
     * @param id ID del producto a eliminar
     * @return Respuesta de confirmación sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar producto",
        description = """
            Elimina un producto del sistema mediante su identificador único.
            
            **Validaciones:**
            - El ID debe ser un número entero positivo (mayor a 0)
            - El producto debe existir en el sistema
            
            **Comportamiento:**
            - Eliminación física del producto de la base de datos
            - Una vez eliminado, el producto no se puede recuperar
            - Todas las referencias al producto se perderán
            - No afecta el historial de operaciones ya realizadas
            
            **Consideraciones de negocio:**
            - Use con precaución en entornos de producción
            - Considere implementar eliminación lógica si necesita mantener historial
            - Verifique que no existan dependencias antes de eliminar
            
            **Casos de uso:**
            - Limpiar productos obsoletos o incorrectos
            - Eliminar productos de prueba
            - Mantenimiento de catálogo de productos
            - Cumplimiento de políticas de retención de datos
            
            **Ejemplos de uso:**
            - `/api/v1/products/1` - Elimina el producto con ID 1
            - Si el producto no existe, retorna 404
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Producto eliminado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Eliminación exitosa",
                    summary = "Respuesta exitosa de eliminación de producto",
                    value = """
                        {
                          "success": true,
                          "message": "Producto eliminado exitosamente",
                          "data": null,
                          "timestamp": "2025-09-29 15:30:00",
                          "statusCode": 200
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "ID inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "ID inválido",
                    summary = "Error de validación por ID inválido",
                    value = """
                        {
                          "success": false,
                          "message": "Parámetros inválidos",
                          "data": null,
                          "error": {
                            "code": "VALIDATION_ERROR",
                            "message": "El ID debe ser mayor a 0",
                            "details": {
                              "field": "id",
                              "rejectedValue": "0",
                              "validationMessage": "debe ser mayor que o igual a 1"
                            }
                          },
                          "timestamp": "2025-09-29 15:30:00",
                          "statusCode": 400
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Producto no encontrado",
                    summary = "Error cuando el producto a eliminar no existe",
                    value = """
                        {
                          "success": false,
                          "message": "Producto no encontrado",
                          "data": null,
                          "error": {
                            "code": "PRODUCT_NOT_FOUND",
                            "message": "No se encontró un producto con ID: 999"
                          },
                          "timestamp": "2025-09-29 15:30:00",
                          "statusCode": 404
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Error interno",
                    summary = "Error inesperado del servidor",
                    value = """
                        {
                          "success": false,
                          "message": "Error interno del servidor",
                          "data": null,
                          "error": {
                            "code": "INTERNAL_ERROR",
                            "message": "Error interno del servidor"
                          },
                          "timestamp": "2025-09-29 15:30:00",
                          "statusCode": 500
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "ID del producto a eliminar", example = "1", required = true)
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id) {
        
        try {
            logger.debug("Iniciando deleteProduct con ID: {}", id);
            
            // Eliminar producto usando el service
            productService.deleteProduct(id);
            
            logger.info("Producto eliminado exitosamente: ID={}", id);
            
            ApiResponse<Void> response = ApiResponse.success(null, "Producto eliminado exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (ProductNotFoundException ex) {
            logger.warn("Producto no encontrado para eliminar: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("PRODUCT_NOT_FOUND", ex.getMessage());
            ApiResponse<Void> response = ApiResponse.error("Producto no encontrado", errorDetails);
            return ResponseEntity.status(404).body(response);
            
        } catch (Exception ex) {
            logger.error("Error al eliminar producto con ID {}: {}", id, ex.getMessage(), ex);
            
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<Void> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Busca productos por diferentes criterios.
     * 
     * @param name Nombre del producto (búsqueda parcial)
     * @param category Categoría del producto
     * @param minPrice Precio mínimo
     * @param maxPrice Precio máximo
     * @param inStock Solo productos con stock disponible
     * @param page Número de página (base 0)
     * @param size Tamaño de página
     * @param sort Campo por el cual ordenar
     * @param direction Dirección del ordenamiento (asc/desc)
     * @return Lista paginada de productos que coinciden con los criterios
     */
    @GetMapping("/search")
    @Operation(
        summary = "Buscar productos por criterios",
        description = """
            Realiza búsquedas avanzadas de productos utilizando múltiples criterios de filtrado.
            
            **Criterios de búsqueda disponibles:**
            - **name**: Búsqueda parcial por nombre (case-insensitive, contiene)
            - **category**: Búsqueda exacta por categoría (case-insensitive)
            - **minPrice**: Precio mínimo (inclusive)
            - **maxPrice**: Precio máximo (inclusive)
            - **inStock**: Si true, solo productos con stock > 0
            
            **Combinación de criterios:**
            - Todos los criterios se combinan con operador AND
            - Los criterios no especificados se ignoran
            - Si no se especifica ningún criterio, retorna todos los productos activos
            
            **Paginación y ordenamiento:**
            - Soporte completo de paginación con metadatos
            - Ordenamiento por cualquier campo (id, name, price, category, stock, createdAt)
            - Dirección ascendente o descendente
            
            **Casos de uso:**
            - Búsqueda de productos por nombre
            - Filtrado por rango de precios
            - Búsqueda por categoría específica
            - Productos disponibles en stock
            - Combinaciones de múltiples criterios
            
            **Ejemplos de uso:**
            - `/api/v1/products/search?name=iPhone` - Productos que contengan "iPhone"
            - `/api/v1/products/search?category=Electrónicos&inStock=true` - Electrónicos en stock
            - `/api/v1/products/search?minPrice=100&maxPrice=500` - Productos entre $100-$500
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Búsqueda realizada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Búsqueda exitosa",
                    summary = "Respuesta exitosa con resultados de búsqueda paginados",
                    value = """
                        {
                          "success": true,
                          "message": "Búsqueda realizada exitosamente",
                          "data": {
                            "content": [
                              {
                                "id": 1,
                                "name": "iPhone 15 Pro",
                                "description": "Smartphone Apple con chip A17 Pro",
                                "price": 999.99,
                                "category": "Electrónicos",
                                "stock": 50,
                                "active": true,
                                "createdAt": "2025-09-29 10:30:00",
                                "updatedAt": "2025-09-29 10:30:00"
                              }
                            ],
                            "page": {
                              "number": 0,
                              "size": 20,
                              "totalElements": 1,
                              "totalPages": 1,
                              "first": true,
                              "last": true,
                              "hasNext": false,
                              "hasPrevious": false
                            }
                          },
                          "timestamp": "2025-09-29 15:45:00",
                          "statusCode": 200
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Parámetros de búsqueda inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Parámetros inválidos",
                    summary = "Error de validación en parámetros de búsqueda",
                    value = """
                        {
                          "success": false,
                          "message": "Parámetros de búsqueda inválidos",
                          "data": null,
                          "error": {
                            "code": "VALIDATION_ERROR",
                            "message": "Errores en los parámetros de búsqueda",
                            "details": {
                              "minPrice": "El precio mínimo no puede ser negativo",
                              "maxPrice": "El precio máximo debe ser mayor al precio mínimo"
                            }
                          },
                          "timestamp": "2025-09-29 15:45:00",
                          "statusCode": 400
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Error interno",
                    summary = "Error inesperado del servidor",
                    value = """
                        {
                          "success": false,
                          "message": "Error interno del servidor",
                          "data": null,
                          "error": {
                            "code": "INTERNAL_ERROR",
                            "message": "Error interno del servidor"
                          },
                          "timestamp": "2025-09-29 15:45:00",
                          "statusCode": 500
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<PagedResponse<ProductDTO>>> searchProducts(
            @Parameter(description = "Nombre del producto (búsqueda parcial)", example = "iPhone")
            @RequestParam(required = false) String name,
            
            @Parameter(description = "Categoría del producto", example = "Electrónicos")
            @RequestParam(required = false) String category,
            
            @Parameter(description = "Precio mínimo", example = "100.00")
            @RequestParam(required = false) @DecimalMin(value = "0.01", message = "El precio mínimo debe ser mayor a 0") BigDecimal minPrice,
            
            @Parameter(description = "Precio máximo", example = "1000.00")
            @RequestParam(required = false) @DecimalMin(value = "0.01", message = "El precio máximo debe ser mayor a 0") BigDecimal maxPrice,
            
            @Parameter(description = "Solo productos con stock disponible", example = "true")
            @RequestParam(required = false) Boolean inStock,
            
            @Parameter(description = "Número de página (base 0)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "Tamaño de página", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) int size,
            
            @Parameter(description = "Campo para ordenamiento", example = "name")
            @RequestParam(defaultValue = "id") String sort,
            
            @Parameter(description = "Dirección del ordenamiento", example = "asc")
            @RequestParam(defaultValue = "asc") String direction) {
        
        try {
            logger.debug("Iniciando searchProducts con criterios: name={}, category={}, minPrice={}, maxPrice={}, inStock={}, page={}, size={}", 
                        name, category, minPrice, maxPrice, inStock, page, size);
            
            // Validar rango de precios si ambos están presentes
            if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
                ErrorResponse errorDetails = new ErrorResponse("VALIDATION_ERROR", "El precio mínimo no puede ser mayor al precio máximo");
                ApiResponse<PagedResponse<ProductDTO>> response = ApiResponse.error("Parámetros de búsqueda inválidos", errorDetails);
                return ResponseEntity.status(400).body(response);
            }
            
            // Configurar ordenamiento
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            // Realizar búsqueda usando el service
            Page<Product> productPage = productService.searchProducts(name, category, minPrice, maxPrice, inStock, pageable);
            
            // Mapear productos a DTOs
            Page<ProductDTO> dtoPage = productPage.map(productMapper::toDTO);
            PagedResponse<ProductDTO> pagedResponse = PagedResponse.of(dtoPage);
            
            logger.info("Búsqueda completada: {} productos encontrados en {} páginas", 
                       productPage.getTotalElements(), productPage.getTotalPages());
            
            ApiResponse<PagedResponse<ProductDTO>> response = ApiResponse.success(pagedResponse, "Búsqueda realizada exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (ValidationException ex) {
            logger.warn("Error de validación en búsqueda: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("VALIDATION_ERROR", ex.getMessage());
            ApiResponse<PagedResponse<ProductDTO>> response = ApiResponse.error("Parámetros de búsqueda inválidos", errorDetails);
            return ResponseEntity.status(400).body(response);
            
        } catch (Exception ex) {
            logger.error("Error al realizar búsqueda de productos: {}", ex.getMessage(), ex);
            
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<PagedResponse<ProductDTO>> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Actualiza el stock de un producto específico.
     * 
     * @param id ID del producto a actualizar
     * @param newStock Nuevo valor de stock
     * @return ProductDTO del producto con stock actualizado
     */
    @PatchMapping("/{id}/stock")
    @Operation(
        summary = "Actualizar stock de producto",
        description = """
            Actualiza únicamente el stock de un producto específico sin modificar otros campos.
            
            **Casos de uso:**
            - Reposición de inventario después de recibir mercancía
            - Ajuste de stock después de ventas o devoluciones
            - Corrección de inventarios después de auditorías
            - Sincronización con sistemas externos de inventario
            
            **Validaciones:**
            - El ID debe ser un número entero positivo (mayor a 0)
            - El producto debe existir y estar activo
            - El nuevo stock debe ser mayor o igual a 0
            - Solo se actualiza el campo stock, otros campos permanecen inalterados
            
            **Comportamiento:**
            - Actualización atómica del stock
            - Se actualiza automáticamente el timestamp de modificación
            - No afecta otros campos del producto
            - Operación idempotente (puede ejecutarse múltiples veces)
            
            **Consideraciones de negocio:**
            - Use este endpoint para operaciones frecuentes de inventario
            - Más eficiente que PUT completo para solo actualizar stock
            - Ideal para integraciones con sistemas de warehouse management
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Stock actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Stock actualizado",
                    summary = "Respuesta exitosa de actualización de stock",
                    value = """
                        {
                          "success": true,
                          "message": "Stock actualizado exitosamente",
                          "data": {
                            "id": 1,
                            "name": "iPhone 15 Pro",
                            "description": "Smartphone Apple con chip A17 Pro",
                            "price": 999.99,
                            "category": "Electrónicos",
                            "stock": 150,
                            "active": true,
                            "createdAt": "2025-09-29 10:30:00",
                            "updatedAt": "2025-09-29 16:00:00"
                          },
                          "timestamp": "2025-09-29 16:00:00",
                          "statusCode": 200
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Parámetros inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = {
                    @ExampleObject(
                        name = "ID inválido",
                        summary = "Error de validación por ID inválido",
                        value = """
                            {
                              "success": false,
                              "message": "Parámetros inválidos",
                              "data": null,
                              "error": {
                                "code": "VALIDATION_ERROR",
                                "message": "El ID debe ser mayor a 0",
                                "details": {
                                  "field": "id",
                                  "rejectedValue": "0",
                                  "validationMessage": "debe ser mayor que o igual a 1"
                                }
                              },
                              "timestamp": "2025-09-29 16:00:00",
                              "statusCode": 400
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Stock inválido",
                        summary = "Error cuando el stock es negativo",
                        value = """
                            {
                              "success": false,
                              "message": "Valor de stock inválido",
                              "data": null,
                              "error": {
                                "code": "VALIDATION_ERROR",
                                "message": "El stock no puede ser negativo",
                                "details": {
                                  "field": "newStock",
                                  "rejectedValue": "-5",
                                  "validationMessage": "debe ser mayor que o igual a 0"
                                }
                              },
                              "timestamp": "2025-09-29 16:00:00",
                              "statusCode": 400
                            }
                            """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Producto no encontrado",
                    summary = "Error cuando el producto no existe",
                    value = """
                        {
                          "success": false,
                          "message": "Producto no encontrado",
                          "data": null,
                          "error": {
                            "code": "PRODUCT_NOT_FOUND",
                            "message": "No se encontró un producto con ID: 999"
                          },
                          "timestamp": "2025-09-29 16:00:00",
                          "statusCode": 404
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Error interno",
                    summary = "Error inesperado del servidor",
                    value = """
                        {
                          "success": false,
                          "message": "Error interno del servidor",
                          "data": null,
                          "error": {
                            "code": "INTERNAL_ERROR",
                            "message": "Error interno del servidor"
                          },
                          "timestamp": "2025-09-29 16:00:00",
                          "statusCode": 500
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<ProductDTO>> updateProductStock(
            @Parameter(description = "ID del producto a actualizar", example = "1", required = true)
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id,
            
            @Parameter(description = "Nuevo valor de stock", example = "150", required = true)
            @RequestParam @Min(value = 0, message = "El stock no puede ser negativo") Integer newStock) {
        
        try {
            logger.debug("Iniciando updateProductStock para ID: {} con nuevo stock: {}", id, newStock);
            
            // Actualizar stock usando el service
            Product updatedProduct = productService.updateStock(id, newStock);
            
            // Mapear a DTO
            ProductDTO productDTO = productMapper.toDTO(updatedProduct);
            
            logger.info("Stock actualizado exitosamente: ID={}, nuevo stock={}, producto='{}'", 
                       id, newStock, updatedProduct.getName());
            
            ApiResponse<ProductDTO> response = ApiResponse.success(productDTO, "Stock actualizado exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (ProductNotFoundException ex) {
            logger.warn("Producto no encontrado para actualizar stock: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("PRODUCT_NOT_FOUND", ex.getMessage());
            ApiResponse<ProductDTO> response = ApiResponse.error("Producto no encontrado", errorDetails);
            return ResponseEntity.status(404).body(response);
            
        } catch (ValidationException ex) {
            logger.warn("Error de validación al actualizar stock: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("VALIDATION_ERROR", ex.getMessage());
            ApiResponse<ProductDTO> response = ApiResponse.error("Valor de stock inválido", errorDetails);
            return ResponseEntity.status(400).body(response);
            
        } catch (IllegalArgumentException ex) {
            logger.warn("Argumentos inválidos al actualizar stock: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("VALIDATION_ERROR", ex.getMessage());
            ApiResponse<ProductDTO> response = ApiResponse.error("Parámetros inválidos", errorDetails);
            return ResponseEntity.status(400).body(response);
            
        } catch (Exception ex) {
            logger.error("Error al actualizar stock del producto ID {}: {}", id, ex.getMessage(), ex);
            
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<ProductDTO> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Activa un producto que estaba desactivado.
     * 
     * @param id ID del producto a activar
     * @return ProductDTO del producto activado
     */
    @PatchMapping("/{id}/activate")
    @Operation(
        summary = "Activar producto",
        description = """
            Activa un producto que se encuentra en estado inactivo, haciéndolo visible y disponible 
            en el sistema nuevamente.
            
            **Casos de uso:**
            - Reactivar productos temporalmente deshabilitados
            - Restaurar productos después de mantenimiento
            - Activar productos estacionales
            - Recuperar productos eliminados por error (soft delete)
            
            **Validaciones:**
            - El ID debe ser un número entero positivo (mayor a 0)
            - El producto debe existir en el sistema (puede estar inactivo)
            - Solo se modifica el campo 'active' a true
            
            **Comportamiento:**
            - Cambia el estado active de false a true
            - Si el producto ya estaba activo, no genera error
            - Se actualiza automáticamente el timestamp de modificación
            - El producto vuelve a aparecer en consultas de productos activos
            - Operación idempotente
            
            **Consideraciones:**
            - Use para gestión de ciclo de vida de productos
            - Alternativa a la eliminación física
            - Permite mantener historial y referencias
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Producto activado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Producto activado",
                    summary = "Respuesta exitosa de activación de producto",
                    value = """
                        {
                          "success": true,
                          "message": "Producto activado exitosamente",
                          "data": {
                            "id": 1,
                            "name": "iPhone 15 Pro",
                            "description": "Smartphone Apple con chip A17 Pro",
                            "price": 999.99,
                            "category": "Electrónicos",
                            "stock": 50,
                            "active": true,
                            "createdAt": "2025-09-29 10:30:00",
                            "updatedAt": "2025-09-29 16:15:00"
                          },
                          "timestamp": "2025-09-29 16:15:00",
                          "statusCode": 200
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "ID inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "ID inválido",
                    summary = "Error de validación por ID inválido",
                    value = """
                        {
                          "success": false,
                          "message": "Parámetros inválidos",
                          "data": null,
                          "error": {
                            "code": "VALIDATION_ERROR",
                            "message": "El ID debe ser mayor a 0",
                            "details": {
                              "field": "id",
                              "rejectedValue": "0",
                              "validationMessage": "debe ser mayor que o igual a 1"
                            }
                          },
                          "timestamp": "2025-09-29 16:15:00",
                          "statusCode": 400
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Producto no encontrado",
                    summary = "Error cuando el producto no existe",
                    value = """
                        {
                          "success": false,
                          "message": "Producto no encontrado",
                          "data": null,
                          "error": {
                            "code": "PRODUCT_NOT_FOUND",
                            "message": "No se encontró un producto con ID: 999"
                          },
                          "timestamp": "2025-09-29 16:15:00",
                          "statusCode": 404
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Error interno",
                    summary = "Error inesperado del servidor",
                    value = """
                        {
                          "success": false,
                          "message": "Error interno del servidor",
                          "data": null,
                          "error": {
                            "code": "INTERNAL_ERROR",
                            "message": "Error interno del servidor"
                          },
                          "timestamp": "2025-09-29 16:15:00",
                          "statusCode": 500
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<ProductDTO>> activateProduct(
            @Parameter(description = "ID del producto a activar", example = "1", required = true)
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id) {
        
        try {
            logger.debug("Iniciando activateProduct para ID: {}", id);
            
            // Activar producto usando el service
            Product activatedProduct = productService.activateProduct(id);
            
            // Mapear a DTO
            ProductDTO productDTO = productMapper.toDTO(activatedProduct);
            
            logger.info("Producto activado exitosamente: ID={}, producto='{}'", 
                       id, activatedProduct.getName());
            
            ApiResponse<ProductDTO> response = ApiResponse.success(productDTO, "Producto activado exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (ProductNotFoundException ex) {
            logger.warn("Producto no encontrado para activar: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("PRODUCT_NOT_FOUND", ex.getMessage());
            ApiResponse<ProductDTO> response = ApiResponse.error("Producto no encontrado", errorDetails);
            return ResponseEntity.status(404).body(response);
            
        } catch (IllegalArgumentException ex) {
            logger.warn("Argumentos inválidos al activar producto: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("VALIDATION_ERROR", ex.getMessage());
            ApiResponse<ProductDTO> response = ApiResponse.error("Parámetros inválidos", errorDetails);
            return ResponseEntity.status(400).body(response);
            
        } catch (Exception ex) {
            logger.error("Error al activar producto ID {}: {}", id, ex.getMessage(), ex);
            
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<ProductDTO> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Desactiva un producto activo (soft delete).
     * 
     * @param id ID del producto a desactivar
     * @return Respuesta de confirmación sin contenido
     */
    @PatchMapping("/{id}/deactivate")
    @Operation(
        summary = "Desactivar producto (soft delete)",
        description = """
            Desactiva un producto activo cambiando su estado a inactivo, sin eliminarlo físicamente 
            de la base de datos.
            
            **Casos de uso:**
            - Ocultar productos temporalmente sin perder datos
            - Descontinuar productos manteniendo historial
            - Suspender productos con problemas de calidad
            - Gestión de productos estacionales
            
            **Validaciones:**
            - El ID debe ser un número entero positivo (mayor a 0)
            - El producto debe existir y estar activo
            - Solo se modifica el campo 'active' a false
            
            **Comportamiento:**
            - Cambia el estado active de true a false
            - El producto deja de aparecer en consultas de productos activos
            - Se mantienen todos los datos del producto
            - Se actualiza automáticamente el timestamp de modificación
            - Operación reversible con el endpoint de activación
            
            **Diferencias con DELETE:**
            - Soft delete: mantiene datos, estado = false
            - Hard delete: elimina completamente el registro
            - Use soft delete para preservar integridad referencial
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Producto desactivado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Producto desactivado",
                    summary = "Respuesta exitosa de desactivación de producto",
                    value = """
                        {
                          "success": true,
                          "message": "Producto desactivado exitosamente",
                          "data": null,
                          "timestamp": "2025-09-29 16:20:00",
                          "statusCode": 200
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "ID inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "ID inválido",
                    summary = "Error de validación por ID inválido",
                    value = """
                        {
                          "success": false,
                          "message": "Parámetros inválidos",
                          "data": null,
                          "error": {
                            "code": "VALIDATION_ERROR",
                            "message": "El ID debe ser mayor a 0",
                            "details": {
                              "field": "id",
                              "rejectedValue": "0",
                              "validationMessage": "debe ser mayor que o igual a 1"
                            }
                          },
                          "timestamp": "2025-09-29 16:20:00",
                          "statusCode": 400
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Producto no encontrado",
                    summary = "Error cuando el producto no existe o ya está inactivo",
                    value = """
                        {
                          "success": false,
                          "message": "Producto no encontrado",
                          "data": null,
                          "error": {
                            "code": "PRODUCT_NOT_FOUND",
                            "message": "No se encontró un producto activo con ID: 999"
                          },
                          "timestamp": "2025-09-29 16:20:00",
                          "statusCode": 404
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Error interno",
                    summary = "Error inesperado del servidor",
                    value = """
                        {
                          "success": false,
                          "message": "Error interno del servidor",
                          "data": null,
                          "error": {
                            "code": "INTERNAL_ERROR",
                            "message": "Error interno del servidor"
                          },
                          "timestamp": "2025-09-29 16:20:00",
                          "statusCode": 500
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<Void>> deactivateProduct(
            @Parameter(description = "ID del producto a desactivar", example = "1", required = true)
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id) {
        
        try {
            logger.debug("Iniciando deactivateProduct para ID: {}", id);
            
            // Desactivar producto usando el service
            productService.deleteProduct(id); // Este método ya hace soft delete
            
            logger.info("Producto desactivado exitosamente: ID={}", id);
            
            ApiResponse<Void> response = ApiResponse.success(null, "Producto desactivado exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (ProductNotFoundException ex) {
            logger.warn("Producto no encontrado para desactivar: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("PRODUCT_NOT_FOUND", ex.getMessage());
            ApiResponse<Void> response = ApiResponse.error("Producto no encontrado", errorDetails);
            return ResponseEntity.status(404).body(response);
            
        } catch (IllegalArgumentException ex) {
            logger.warn("Argumentos inválidos al desactivar producto: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("VALIDATION_ERROR", ex.getMessage());
            ApiResponse<Void> response = ApiResponse.error("Parámetros inválidos", errorDetails);
            return ResponseEntity.status(400).body(response);
            
        } catch (Exception ex) {
            logger.error("Error al desactivar producto ID {}: {}", id, ex.getMessage(), ex);
            
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<Void> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Obtiene estadísticas generales de productos.
     * 
     * @return Estadísticas del sistema de productos
     */
    @GetMapping("/statistics")
    @Operation(
        summary = "Obtener estadísticas de productos",
        description = """
            Obtiene estadísticas generales del sistema de productos incluyendo conteos, 
            categorías y métricas de inventario.
            
            **Información incluida:**
            - Total de productos activos e inactivos
            - Número de categorías únicas
            - Conteos por estado
            - Métricas de inventario
            
            **Casos de uso:**
            - Dashboards administrativos
            - Reportes de gestión
            - Análisis de inventario
            - Métricas de negocio
            
            **Rendimiento:**
            - Consulta optimizada para estadísticas
            - Cacheable para mejor rendimiento
            - Solo lectura, no modifica datos
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Estadísticas obtenidas exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Estadísticas del sistema",
                    summary = "Respuesta exitosa con estadísticas de productos",
                    value = """
                        {
                          "success": true,
                          "message": "Estadísticas obtenidas exitosamente",
                          "data": {
                            "totalActiveProducts": 150,
                            "totalProducts": 175,
                            "inactiveProducts": 25,
                            "uniqueCategories": 8
                          },
                          "timestamp": "2025-09-29 16:30:00",
                          "statusCode": 200
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Error interno",
                    summary = "Error inesperado del servidor",
                    value = """
                        {
                          "success": false,
                          "message": "Error interno del servidor",
                          "data": null,
                          "error": {
                            "code": "INTERNAL_ERROR",
                            "message": "Error interno del servidor"
                          },
                          "timestamp": "2025-09-29 16:30:00",
                          "statusCode": 500
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductStatistics() {
        
        try {
            logger.debug("Iniciando obtención de estadísticas de productos");
            
            // Obtener estadísticas usando el service
            Map<String, Object> statistics = productService.getProductStatistics();
            
            logger.info("Estadísticas obtenidas exitosamente: {} productos activos de {} total", 
                       statistics.get("totalActiveProducts"), statistics.get("totalProducts"));
            
            ApiResponse<Map<String, Object>> response = ApiResponse.success(statistics, "Estadísticas obtenidas exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            logger.error("Error al obtener estadísticas de productos: {}", ex.getMessage(), ex);
            
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<Map<String, Object>> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Obtiene productos por categoría específica.
     * 
     * @param category Nombre de la categoría
     * @return Lista de productos de la categoría especificada
     */
    @GetMapping("/category/{category}")
    @Operation(
        summary = "Obtener productos por categoría",
        description = """
            Obtiene todos los productos activos que pertenecen a una categoría específica.
            
            **Comportamiento:**
            - Búsqueda exacta por categoría (case-insensitive)
            - Solo retorna productos activos
            - Lista sin paginación para facilitar filtros por categoría
            
            **Casos de uso:**
            - Filtros por categoría en tiendas online
            - Navegación por catálogo
            - Reportes por línea de productos
            - Análisis de inventario por categoría
            
            **Validaciones:**
            - La categoría no puede estar vacía
            - Búsqueda case-insensitive para flexibilidad
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Productos obtenidos exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Productos por categoría",
                    summary = "Lista de productos de una categoría específica",
                    value = """
                        {
                          "success": true,
                          "message": "Productos obtenidos exitosamente",
                          "data": [
                            {
                              "id": 1,
                              "name": "iPhone 15 Pro",
                              "description": "Smartphone Apple con chip A17 Pro",
                              "price": 999.99,
                              "category": "Electrónicos",
                              "stock": 50,
                              "active": true,
                              "createdAt": "2025-09-29 10:30:00",
                              "updatedAt": "2025-09-29 10:30:00"
                            },
                            {
                              "id": 2,
                              "name": "Samsung Galaxy S24",
                              "description": "Smartphone Samsung con cámara profesional",
                              "price": 899.99,
                              "category": "Electrónicos",
                              "stock": 30,
                              "active": true,
                              "createdAt": "2025-09-29 11:00:00",
                              "updatedAt": "2025-09-29 11:00:00"
                            }
                          ],
                          "timestamp": "2025-09-29 16:45:00",
                          "statusCode": 200
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Categoría inválida",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Categoría vacía",
                    summary = "Error cuando la categoría está vacía",
                    value = """
                        {
                          "success": false,
                          "message": "Categoría inválida",
                          "data": null,
                          "error": {
                            "code": "VALIDATION_ERROR",
                            "message": "La categoría no puede estar vacía"
                          },
                          "timestamp": "2025-09-29 16:45:00",
                          "statusCode": 400
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Error interno",
                    summary = "Error inesperado del servidor",
                    value = """
                        {
                          "success": false,
                          "message": "Error interno del servidor",
                          "data": null,
                          "error": {
                            "code": "INTERNAL_ERROR",
                            "message": "Error interno del servidor"
                          },
                          "timestamp": "2025-09-29 16:45:00",
                          "statusCode": 500
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByCategory(
            @Parameter(description = "Nombre de la categoría", example = "Electrónicos", required = true)
            @PathVariable String category) {
        
        try {
            logger.debug("Iniciando getProductsByCategory para categoría: {}", category);
            
            // Validar que la categoría no esté vacía
            if (category == null || category.trim().isEmpty()) {
                ErrorResponse errorDetails = new ErrorResponse("VALIDATION_ERROR", "La categoría no puede estar vacía");
                ApiResponse<List<ProductDTO>> response = ApiResponse.error("Categoría inválida", errorDetails);
                return ResponseEntity.status(400).body(response);
            }
            
            // Obtener productos por categoría usando el service
            List<Product> products = productService.getProductsByCategory(category);
            
            // Mapear a DTOs
            List<ProductDTO> productDTOs = productMapper.toDTOList(products);
            
            logger.info("Productos obtenidos por categoría '{}': {} productos encontrados", category, productDTOs.size());
            
            ApiResponse<List<ProductDTO>> response = ApiResponse.success(productDTOs, "Productos obtenidos exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException ex) {
            logger.warn("Categoría inválida: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("VALIDATION_ERROR", ex.getMessage());
            ApiResponse<List<ProductDTO>> response = ApiResponse.error("Categoría inválida", errorDetails);
            return ResponseEntity.status(400).body(response);
            
        } catch (Exception ex) {
            logger.error("Error al obtener productos por categoría '{}': {}", category, ex.getMessage(), ex);
            
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<List<ProductDTO>> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Obtiene productos con stock bajo (menor al límite especificado).
     * 
     * @param limit Límite de stock para considerar como "bajo"
     * @return Lista de productos con stock bajo
     */
    @GetMapping("/low-stock")
    @Operation(
        summary = "Obtener productos con stock bajo",
        description = """
            Obtiene todos los productos activos que tienen stock menor al límite especificado.
            Útil para identificar productos que necesitan reabastecimiento.
            
            **Parámetros:**
            - **limit**: Cantidad límite para considerar stock bajo (default: 10)
            
            **Comportamiento:**
            - Solo incluye productos activos
            - Compara stock < límite especificado
            - Ordenado por stock ascendente (los más críticos primero)
            
            **Casos de uso:**
            - Alertas de reabastecimiento
            - Reportes de inventario crítico
            - Planificación de compras
            - Dashboards de gestión de inventario
            
            **Validaciones:**
            - El límite debe ser mayor o igual a 0
            - Si no se especifica, usa 10 como default
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Productos con stock bajo obtenidos exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Productos con stock bajo",
                    summary = "Lista de productos que necesitan reabastecimiento",
                    value = """
                        {
                          "success": true,
                          "message": "Productos con stock bajo obtenidos exitosamente",
                          "data": [
                            {
                              "id": 5,
                              "name": "MacBook Air M2",
                              "description": "Laptop Apple con chip M2",
                              "price": 1199.99,
                              "category": "Computadoras",
                              "stock": 2,
                              "active": true,
                              "createdAt": "2025-09-29 09:00:00",
                              "updatedAt": "2025-09-29 15:30:00"
                            },
                            {
                              "id": 8,
                              "name": "iPad Pro 12.9",
                              "description": "Tablet Apple con pantalla Liquid Retina",
                              "price": 1099.99,
                              "category": "Tablets",
                              "stock": 5,
                              "active": true,
                              "createdAt": "2025-09-29 12:00:00",
                              "updatedAt": "2025-09-29 14:00:00"
                            }
                          ],
                          "timestamp": "2025-09-29 17:00:00",
                          "statusCode": 200
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Límite de stock inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Límite inválido",
                    summary = "Error cuando el límite es negativo",
                    value = """
                        {
                          "success": false,
                          "message": "Límite de stock inválido",
                          "data": null,
                          "error": {
                            "code": "VALIDATION_ERROR",
                            "message": "El límite de stock no puede ser negativo",
                            "details": {
                              "field": "limit",
                              "rejectedValue": "-5",
                              "validationMessage": "debe ser mayor que o igual a 0"
                            }
                          },
                          "timestamp": "2025-09-29 17:00:00",
                          "statusCode": 400
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Error interno",
                    summary = "Error inesperado del servidor",
                    value = """
                        {
                          "success": false,
                          "message": "Error interno del servidor",
                          "data": null,
                          "error": {
                            "code": "INTERNAL_ERROR",
                            "message": "Error interno del servidor"
                          },
                          "timestamp": "2025-09-29 17:00:00",
                          "statusCode": 500
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsWithLowStock(
            @Parameter(description = "Límite de stock para considerar como bajo", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 0, message = "El límite de stock no puede ser negativo") Integer limit) {
        
        try {
            logger.debug("Iniciando getProductsWithLowStock con límite: {}", limit);
            
            // Obtener productos con stock bajo usando el service
            List<Product> products = productService.getProductsWithLowStock(limit);
            
            // Mapear a DTOs
            List<ProductDTO> productDTOs = productMapper.toDTOList(products);
            
            logger.info("Productos con stock bajo (< {}): {} productos encontrados", limit, productDTOs.size());
            
            ApiResponse<List<ProductDTO>> response = ApiResponse.success(productDTOs, "Productos con stock bajo obtenidos exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException ex) {
            logger.warn("Límite de stock inválido: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("VALIDATION_ERROR", ex.getMessage());
            ApiResponse<List<ProductDTO>> response = ApiResponse.error("Límite de stock inválido", errorDetails);
            return ResponseEntity.status(400).body(response);
            
        } catch (Exception ex) {
            logger.error("Error al obtener productos con stock bajo (límite: {}): {}", limit, ex.getMessage(), ex);
            
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<List<ProductDTO>> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Valida si un nombre de producto está disponible.
     * 
     * @param name Nombre del producto a validar
     * @return Resultado de disponibilidad del nombre
     */
    @GetMapping("/validate-name")
    @Operation(
        summary = "Validar disponibilidad de nombre de producto",
        description = """
            Verifica si un nombre de producto está disponible para uso (no existe otro producto con ese nombre).
            
            **Comportamiento:**
            - Validación case-insensitive
            - Solo verifica contra productos activos
            - Útil para validaciones en tiempo real en formularios
            
            **Casos de uso:**
            - Validación en formularios de creación
            - Verificación antes de actualizar nombres
            - APIs de autocompletado
            - Validaciones de front-end en tiempo real
            
            **Respuesta:**
            - `true`: Nombre disponible para uso
            - `false`: Nombre ya existe (no disponible)
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Validación completada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = {
                    @ExampleObject(
                        name = "Nombre disponible",
                        summary = "El nombre está disponible para uso",
                        value = """
                            {
                              "success": true,
                              "message": "Nombre validado exitosamente",
                              "data": {
                                "name": "iPhone 16 Pro",
                                "available": true,
                                "message": "El nombre está disponible"
                              },
                              "timestamp": "2025-09-29 17:15:00",
                              "statusCode": 200
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Nombre no disponible",
                        summary = "El nombre ya está en uso",
                        value = """
                            {
                              "success": true,
                              "message": "Nombre validado exitosamente",
                              "data": {
                                "name": "iPhone 15 Pro",
                                "available": false,
                                "message": "El nombre ya está en uso"
                              },
                              "timestamp": "2025-09-29 17:15:00",
                              "statusCode": 200
                            }
                            """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Nombre inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Nombre vacío",
                    summary = "Error cuando el nombre está vacío",
                    value = """
                        {
                          "success": false,
                          "message": "Nombre inválido",
                          "data": null,
                          "error": {
                            "code": "VALIDATION_ERROR",
                            "message": "El nombre no puede estar vacío"
                          },
                          "timestamp": "2025-09-29 17:15:00",
                          "statusCode": 400
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Error interno",
                    summary = "Error inesperado del servidor",
                    value = """
                        {
                          "success": false,
                          "message": "Error interno del servidor",
                          "data": null,
                          "error": {
                            "code": "INTERNAL_ERROR",
                            "message": "Error interno del servidor"
                          },
                          "timestamp": "2025-09-29 17:15:00",
                          "statusCode": 500
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateProductName(
            @Parameter(description = "Nombre del producto a validar", example = "iPhone 16 Pro", required = true)
            @RequestParam String name) {
        
        try {
            logger.debug("Iniciando validateProductName para nombre: '{}'", name);
            
            // Validar que el nombre no esté vacío
            if (name == null || name.trim().isEmpty()) {
                ErrorResponse errorDetails = new ErrorResponse("VALIDATION_ERROR", "El nombre no puede estar vacío");
                ApiResponse<Map<String, Object>> response = ApiResponse.error("Nombre inválido", errorDetails);
                return ResponseEntity.status(400).body(response);
            }
            
            // Validar disponibilidad usando el service
            boolean isAvailable = productService.isProductNameAvailable(name.trim());
            
            // Crear respuesta estructurada
            Map<String, Object> result = Map.of(
                "name", name.trim(),
                "available", isAvailable,
                "message", isAvailable ? "El nombre está disponible" : "El nombre ya está en uso"
            );
            
            logger.info("Validación de nombre '{}': {}", name, isAvailable ? "disponible" : "no disponible");
            
            ApiResponse<Map<String, Object>> response = ApiResponse.success(result, "Nombre validado exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException ex) {
            logger.warn("Nombre inválido: {}", ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("VALIDATION_ERROR", ex.getMessage());
            ApiResponse<Map<String, Object>> response = ApiResponse.error("Nombre inválido", errorDetails);
            return ResponseEntity.status(400).body(response);
            
        } catch (Exception ex) {
            logger.error("Error al validar nombre '{}': {}", name, ex.getMessage(), ex);
            
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<Map<String, Object>> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint para validar un producto específico por ID
     * Verifica que el producto exista y esté activo, así como que tenga datos válidos
     * 
     * @param id ID del producto a validar
     * @return ResponseEntity con el resultado de la validación
     */
    @PostMapping("/{id}/validate")
    @Operation(
        summary = "Validar producto por ID",
        description = """
            Valida un producto específico por su ID. Verifica que:
            - El producto exista en la base de datos
            - El producto esté activo
            - Los datos del producto sean válidos (nombre, precio, stock, etc.)
            - No tenga conflictos con reglas de negocio
            
            **Casos de uso:**
            - Validación antes de operaciones críticas
            - Verificación de integridad de datos
            - Comprobación de reglas de negocio
            - Auditoría de productos
            
            **Ejemplos de URL:**
            - `POST /api/v1/products/1/validate` - Validar producto con ID 1
            - `POST /api/v1/products/25/validate` - Validar producto con ID 25
            """,
        tags = {"Products"}
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Producto validado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Producto válido",
                    summary = "El producto pasó todas las validaciones",
                    value = """
                        {
                          "success": true,
                          "message": "Producto validado exitosamente",
                          "data": {
                            "productId": 1,
                            "name": "iPhone 15 Pro",
                            "isValid": true,
                            "validationStatus": "PASSED",
                            "checks": {
                              "exists": true,
                              "isActive": true,
                              "hasValidName": true,
                              "hasValidPrice": true,
                              "hasValidStock": true,
                              "hasValidCategory": true
                            },
                            "message": "Todas las validaciones pasaron exitosamente"
                          },
                          "timestamp": "2025-09-29 17:30:00",
                          "statusCode": 200
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "ID inválido o producto no válido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = {
                    @ExampleObject(
                        name = "ID inválido",
                        summary = "El ID proporcionado no es válido",
                        value = """
                            {
                              "success": false,
                              "message": "ID inválido: debe ser mayor a 0",
                              "data": null,
                              "error": {
                                "code": "INVALID_ID",
                                "message": "El ID debe ser un número positivo mayor a 0"
                              },
                              "timestamp": "2025-09-29 17:30:00",
                              "statusCode": 400
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Producto inválido",
                        summary = "El producto no pasó las validaciones",
                        value = """
                            {
                              "success": false,
                              "message": "El producto no pasó las validaciones",
                              "data": {
                                "productId": 5,
                                "name": "Producto Inválido",
                                "isValid": false,
                                "validationStatus": "FAILED",
                                "checks": {
                                  "exists": true,
                                  "isActive": false,
                                  "hasValidName": true,
                                  "hasValidPrice": false,
                                  "hasValidStock": true,
                                  "hasValidCategory": true
                                },
                                "errors": [
                                  "El producto está inactivo",
                                  "El precio debe ser mayor a 0"
                                ]
                              },
                              "timestamp": "2025-09-29 17:30:00",
                              "statusCode": 400
                            }
                            """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Producto no encontrado",
                    summary = "No existe un producto con el ID especificado",
                    value = """
                        {
                          "success": false,
                          "message": "Producto no encontrado con ID: 999",
                          "data": null,
                          "error": {
                            "code": "PRODUCT_NOT_FOUND",
                            "message": "No existe un producto con el ID especificado"
                          },
                          "timestamp": "2025-09-29 17:30:00",
                          "statusCode": 404
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Error interno",
                    summary = "Error inesperado del servidor",
                    value = """
                        {
                          "success": false,
                          "message": "Error interno del servidor",
                          "data": null,
                          "error": {
                            "code": "INTERNAL_ERROR",
                            "message": "Error interno del servidor"
                          },
                          "timestamp": "2025-09-29 17:30:00",
                          "statusCode": 500
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateProduct(
            @Parameter(description = "ID del producto a validar", example = "1", required = true)
            @PathVariable @Min(value = 1, message = "El ID debe ser mayor a 0") Long id
    ) {
        try {
            logger.debug("Iniciando validateProduct para ID: {}", id);
            
            // Validar el producto usando el servicio
            Map<String, Object> validationResult = productService.validateProduct(id);
            
            logger.info("Producto con ID {} validado exitosamente", id);
            
            ApiResponse<Map<String, Object>> response = ApiResponse.success(validationResult, "Producto validado exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (ValidationException ex) {
            logger.warn("Error de validación para producto ID {}: {}", id, ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("VALIDATION_ERROR", ex.getMessage());
            ApiResponse<Map<String, Object>> response = ApiResponse.error(ex.getMessage(), errorDetails);
            return ResponseEntity.status(400).body(response);
            
        } catch (ProductNotFoundException ex) {
            logger.warn("Producto no encontrado ID {}: {}", id, ex.getMessage());
            
            ErrorResponse errorDetails = new ErrorResponse("PRODUCT_NOT_FOUND", ex.getMessage());
            ApiResponse<Map<String, Object>> response = ApiResponse.error(ex.getMessage(), errorDetails);
            return ResponseEntity.status(404).body(response);
            
        } catch (Exception ex) {
            logger.error("Error al validar producto ID {}: {}", id, ex.getMessage(), ex);
            
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<Map<String, Object>> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint para buscar productos por rango de precios
     * Permite filtrar productos que estén dentro de un rango específico de precios
     * 
     * @param minPrice Precio mínimo (opcional)
     * @param maxPrice Precio máximo (opcional)
     * @param page Número de página (opcional, por defecto 0)
     * @param size Tamaño de página (opcional, por defecto 20)
     * @param sort Campo para ordenar (opcional, por defecto "id")
     * @param direction Dirección del ordenamiento (opcional, por defecto "asc")
     * @return ResponseEntity con la lista paginada de productos en el rango de precios
     */
    @GetMapping("/price-range")
    @Operation(
        summary = "Buscar productos por rango de precios",
        description = """
            Busca productos que estén dentro de un rango específico de precios.
            Permite filtrar por precio mínimo, máximo o ambos.
            
            **Características:**
            - Filtrado por precio mínimo y/o máximo
            - Paginación automática de resultados
            - Ordenamiento configurable
            - Solo retorna productos activos
            - Validación de rangos de precios
            
            **Parámetros opcionales:**
            - `minPrice`: Precio mínimo (inclusive)
            - `maxPrice`: Precio máximo (inclusive)
            - `page`: Número de página (0 = primera página)
            - `size`: Elementos por página (máximo 100)
            - `sort`: Campo para ordenar
            - `direction`: Dirección del ordenamiento (asc/desc)
            
            **Ejemplos de uso:**
            - `/api/v1/products/price-range?minPrice=100&maxPrice=500` - Entre $100 y $500
            - `/api/v1/products/price-range?minPrice=1000` - Desde $1000 en adelante
            - `/api/v1/products/price-range?maxPrice=50` - Hasta $50
            - `/api/v1/products/price-range?minPrice=100&maxPrice=500&sort=price&direction=asc` - Ordenado por precio
            """,
        tags = {"Products"}
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Productos encontrados exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Productos en rango de precios",
                    summary = "Lista de productos entre $100 y $500",
                    value = """
                        {
                          "success": true,
                          "message": "Productos en rango de precios obtenidos exitosamente",
                          "data": {
                            "content": [
                              {
                                "id": 3,
                                "name": "iPad Air",
                                "description": "Tablet Apple con chip M1",
                                "price": 299.99,
                                "category": "Electrónicos",
                                "stock": 25,
                                "active": true,
                                "createdAt": "2025-09-29 10:30:00",
                                "updatedAt": "2025-09-29 10:30:00"
                              },
                              {
                                "id": 7,
                                "name": "AirPods Pro",
                                "description": "Auriculares inalámbricos con cancelación de ruido",
                                "price": 249.99,
                                "category": "Electrónicos",
                                "stock": 40,
                                "active": true,
                                "createdAt": "2025-09-29 11:00:00",
                                "updatedAt": "2025-09-29 11:00:00"
                              }
                            ],
                            "pageable": {
                              "pageNumber": 0,
                              "pageSize": 20,
                              "sort": {
                                "sorted": true,
                                "orders": [{"property": "price", "direction": "ASC"}]
                              }
                            },
                            "totalElements": 8,
                            "totalPages": 1,
                            "first": true,
                            "last": true,
                            "hasNext": false,
                            "hasPrevious": false
                          },
                          "timestamp": "2025-09-29 17:45:00",
                          "statusCode": 200
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Parámetros inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Rango de precios inválido",
                    summary = "El precio mínimo es mayor al máximo",
                    value = """
                        {
                          "success": false,
                          "message": "El precio mínimo no puede ser mayor al precio máximo",
                          "data": null,
                          "error": {
                            "code": "INVALID_PRICE_RANGE",
                            "message": "El precio mínimo ($500.00) no puede ser mayor al precio máximo ($100.00)"
                          },
                          "timestamp": "2025-09-29 17:45:00",
                          "statusCode": 400
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<PagedResponse<ProductDTO>>> getProductsByPriceRange(
            @Parameter(description = "Precio mínimo (inclusive)", example = "100.00")
            @RequestParam(required = false) @DecimalMin(value = "0.0", message = "El precio mínimo debe ser mayor o igual a 0") BigDecimal minPrice,
            
            @Parameter(description = "Precio máximo (inclusive)", example = "500.00")  
            @RequestParam(required = false) @DecimalMin(value = "0.0", message = "El precio máximo debe ser mayor o igual a 0") BigDecimal maxPrice,
            
            @Parameter(description = "Número de página (0 = primera página)", example = "0")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "El número de página debe ser mayor o igual a 0") int page,
            
            @Parameter(description = "Número de elementos por página (máximo 100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "El tamaño de página debe ser mayor a 0") @jakarta.validation.constraints.Max(value = 100, message = "El tamaño de página no puede ser mayor a 100") int size,
            
            @Parameter(description = "Campo para ordenar", example = "price")
            @RequestParam(defaultValue = "id") String sort,
            
            @Parameter(description = "Dirección del ordenamiento", example = "asc")
            @RequestParam(defaultValue = "asc") String direction
    ) {
        try {
            logger.debug("Iniciando búsqueda por rango de precios: minPrice={}, maxPrice={}, page={}, size={}", 
                        minPrice, maxPrice, page, size);
            
            // Validar rango de precios
            if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
                String errorMsg = String.format("El precio mínimo ($%.2f) no puede ser mayor al precio máximo ($%.2f)", 
                                               minPrice, maxPrice);
                logger.warn("Rango de precios inválido: {}", errorMsg);
                
                ErrorResponse errorDetails = new ErrorResponse("INVALID_PRICE_RANGE", errorMsg);
                ApiResponse<PagedResponse<ProductDTO>> response = ApiResponse.error("El precio mínimo no puede ser mayor al precio máximo", errorDetails);
                return ResponseEntity.status(400).body(response);
            }
            
            // Crear Pageable
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            // Buscar productos usando el servicio de búsqueda general
            Page<Product> productsPage = productService.searchProducts(null, null, minPrice, maxPrice, null, pageable);
            
            // Convertir a DTOs manteniendo la paginación
            Page<ProductDTO> productDTOsPage = productsPage.map(productMapper::toDTO);
            
            // Crear respuesta paginada
            PagedResponse<ProductDTO> pagedResponse = PagedResponse.of(productDTOsPage);
            
            logger.info("Búsqueda por rango de precios completada: {} productos encontrados entre ${} y ${}", 
                       productsPage.getTotalElements(), minPrice, maxPrice);
            
            ApiResponse<PagedResponse<ProductDTO>> response = ApiResponse.success(pagedResponse, "Productos en rango de precios obtenidos exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            logger.error("Error al buscar productos por rango de precios: {}", ex.getMessage(), ex);
            
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<PagedResponse<ProductDTO>> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint para buscar productos por nombre exacto
     * Busca productos que coincidan exactamente con el nombre proporcionado
     * 
     * @param name Nombre exacto del producto a buscar
     * @return ResponseEntity con la lista de productos que coinciden exactamente
     */
    @GetMapping("/name/{name}")
    @Operation(
        summary = "Buscar productos por nombre exacto",
        description = """
            Busca productos que coincidan exactamente con el nombre proporcionado.
            La búsqueda es case-sensitive y debe ser una coincidencia exacta.
            
            **Características:**
            - Búsqueda por nombre exacto (case-sensitive)
            - Solo retorna productos activos
            - Puede devolver múltiples productos si hay nombres duplicados
            - Incluye información completa del producto
            
            **Ejemplo de uso:**
            - `/api/v1/products/name/iPhone 15 Pro` - Busca exactamente "iPhone 15 Pro"
            - `/api/v1/products/name/MacBook Air M2` - Busca exactamente "MacBook Air M2"
            
            **Nota:** Para búsquedas parciales usar `/api/v1/products/search?name=iPhone`
            """,
        tags = {"Products"}
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Productos encontrados exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Productos encontrados",
                    summary = "Lista de productos con nombre exacto",
                    value = """
                        {
                          "success": true,
                          "message": "Productos encontrados exitosamente",
                          "data": [
                            {
                              "id": 1,
                              "name": "iPhone 15 Pro",
                              "description": "Smartphone Apple con chip A17 Pro",
                              "price": 999.99,
                              "category": "Electrónicos",
                              "stock": 50,
                              "active": true,
                              "createdAt": "2025-09-29 10:30:00",
                              "updatedAt": "2025-09-29 10:30:00"
                            }
                          ],
                          "timestamp": "2025-09-29 18:00:00",
                          "statusCode": 200
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Nombre inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Nombre inválido",
                    summary = "El nombre proporcionado es inválido",
                    value = """
                        {
                          "success": false,
                          "message": "El nombre del producto no puede estar vacío",
                          "data": null,
                          "error": {
                            "code": "INVALID_NAME",
                            "message": "El nombre debe tener al menos 2 caracteres"
                          },
                          "timestamp": "2025-09-29 18:00:00",
                          "statusCode": 400
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "No se encontraron productos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "No encontrado",
                    summary = "No existen productos con ese nombre exacto",
                    value = """
                        {
                          "success": true,
                          "message": "No se encontraron productos con el nombre especificado",
                          "data": [],
                          "timestamp": "2025-09-29 18:00:00",
                          "statusCode": 200
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByExactName(
            @Parameter(description = "Nombre exacto del producto", example = "iPhone 15 Pro", required = true)
            @PathVariable String name
    ) {
        try {
            logger.debug("Iniciando búsqueda por nombre exacto: '{}'", name);
            
            // Validar nombre
            if (name == null || name.trim().isEmpty()) {
                logger.warn("Nombre de producto vacío o nulo");
                
                ErrorResponse errorDetails = new ErrorResponse("INVALID_NAME", "El nombre del producto no puede estar vacío");
                ApiResponse<List<ProductDTO>> response = ApiResponse.error("El nombre del producto no puede estar vacío", errorDetails);
                return ResponseEntity.status(400).body(response);
            }
            
            if (name.trim().length() < 2) {
                logger.warn("Nombre de producto muy corto: '{}'", name);
                
                ErrorResponse errorDetails = new ErrorResponse("INVALID_NAME", "El nombre debe tener al menos 2 caracteres");
                ApiResponse<List<ProductDTO>> response = ApiResponse.error("El nombre del producto no puede estar vacío", errorDetails);
                return ResponseEntity.status(400).body(response);
            }
            
            // Buscar productos por nombre exacto
            List<Product> products = productService.getProductsByName(name.trim());
            
            // Convertir a DTOs
            List<ProductDTO> productDTOs = products.stream()
                    .map(productMapper::toDTO)
                    .toList();
            
            String message = productDTOs.isEmpty() 
                ? "No se encontraron productos con el nombre especificado"
                : "Productos encontrados exitosamente";
            
            logger.info("Búsqueda por nombre exacto '{}' completada: {} productos encontrados", 
                       name, productDTOs.size());
            
            ApiResponse<List<ProductDTO>> response = ApiResponse.success(productDTOs, message);
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            logger.error("Error al buscar productos por nombre exacto '{}': {}", name, ex.getMessage(), ex);
            
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<List<ProductDTO>> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint para obtener productos más populares
     * Retorna los productos ordenados por fecha de creación (simulando popularidad)
     * 
     * @param limit Número máximo de productos a retornar
     * @return ResponseEntity con la lista de productos más populares
     */
    @GetMapping("/popular")
    @Operation(
        summary = "Obtener productos más populares",
        description = """
            Obtiene los productos más populares del sistema.
            Los productos se ordenan por fecha de creación descendente (más recientes primero)
            para simular popularidad.
            
            **Características:**
            - Solo retorna productos activos
            - Ordenados por popularidad (fecha de creación)
            - Límite configurable de resultados
            - Información completa de cada producto
            
            **Ejemplo de uso:**
            - `/api/v1/products/popular?limit=10` - Top 10 productos más populares
            - `/api/v1/products/popular?limit=5` - Top 5 productos más populares
            """,
        tags = {"Products"}
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Productos populares obtenidos exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Productos populares",
                    summary = "Lista de productos más populares",
                    value = """
                        {
                          "success": true,
                          "message": "Productos más populares obtenidos exitosamente",
                          "data": [
                            {
                              "id": 5,
                              "name": "iPhone 16 Pro",
                              "description": "Último modelo de iPhone",
                              "price": 1199.99,
                              "category": "Electrónicos",
                              "stock": 15,
                              "active": true,
                              "createdAt": "2025-09-29 12:00:00",
                              "updatedAt": "2025-09-29 12:00:00"
                            }
                          ],
                          "timestamp": "2025-09-29 18:15:00",
                          "statusCode": 200
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getPopularProducts(
            @Parameter(description = "Número máximo de productos a retornar", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "El límite debe ser mayor a 0") @jakarta.validation.constraints.Max(value = 100, message = "El límite no puede ser mayor a 100") int limit
    ) {
        try {
            logger.debug("Obteniendo {} productos más populares", limit);
            
            Pageable pageable = PageRequest.of(0, limit);
            Page<Product> popularProducts = productService.getMostPopularProducts(pageable);
            
            List<ProductDTO> productDTOs = popularProducts.getContent().stream()
                    .map(productMapper::toDTO)
                    .toList();
            
            logger.info("{} productos populares obtenidos exitosamente", productDTOs.size());
            
            ApiResponse<List<ProductDTO>> response = ApiResponse.success(productDTOs, "Productos más populares obtenidos exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            logger.error("Error al obtener productos populares: {}", ex.getMessage(), ex);
            
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<List<ProductDTO>> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint para buscar productos por múltiples categorías
     * Permite filtrar productos que pertenezcan a cualquiera de las categorías especificadas
     * 
     * @param categories Lista de categorías separadas por comas
     * @param page Número de página
     * @param size Tamaño de página
     * @param sort Campo para ordenar
     * @param direction Dirección del ordenamiento
     * @return ResponseEntity con la lista paginada de productos
     */
    @GetMapping("/categories")
    @Operation(
        summary = "Buscar productos por múltiples categorías",
        description = """
            Busca productos que pertenezcan a cualquiera de las categorías especificadas.
            Las categorías se especifican como una lista separada por comas.
            
            **Características:**
            - Búsqueda en múltiples categorías simultáneamente
            - Paginación automática de resultados
            - Solo retorna productos activos
            - Case-insensitive
            
            **Ejemplo de uso:**
            - `/api/v1/products/categories?categories=Electrónicos,Ropa,Hogar`
            """,
        tags = {"Products"}
    )
    public ResponseEntity<ApiResponse<PagedResponse<ProductDTO>>> getProductsByCategories(
            @Parameter(description = "Categorías separadas por comas", example = "Electrónicos,Ropa,Hogar", required = true)
            @RequestParam List<String> categories,
            
            @Parameter(description = "Número de página", example = "0")
            @RequestParam(defaultValue = "0") @Min(value = 0) int page,
            
            @Parameter(description = "Tamaño de página", example = "20")
            @RequestParam(defaultValue = "20") @Min(value = 1) @jakarta.validation.constraints.Max(value = 100) int size,
            
            @Parameter(description = "Campo para ordenar", example = "name")
            @RequestParam(defaultValue = "id") String sort,
            
            @Parameter(description = "Dirección del ordenamiento", example = "asc")
            @RequestParam(defaultValue = "asc") String direction
    ) {
        try {
            logger.debug("Búsqueda por múltiples categorías: {}", categories);
            
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            Page<Product> productsPage = productService.getProductsByCategories(categories, pageable);
            Page<ProductDTO> productDTOsPage = productsPage.map(productMapper::toDTO);
            PagedResponse<ProductDTO> pagedResponse = PagedResponse.of(productDTOsPage);
            
            logger.info("Búsqueda por categorías completada: {} productos encontrados", productsPage.getTotalElements());
            
            ApiResponse<PagedResponse<ProductDTO>> response = ApiResponse.success(pagedResponse, "Productos por categorías obtenidos exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            logger.error("Error al buscar por categorías: {}", ex.getMessage(), ex);
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<PagedResponse<ProductDTO>> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint para crear múltiples productos de forma masiva
     * Permite crear varios productos en una sola operación
     * 
     * @param productDTOs Lista de productos a crear
     * @return ResponseEntity con la lista de productos creados
     */
    @PostMapping("/batch")
    @Operation(
        summary = "Crear múltiples productos",
        description = """
            Crea múltiples productos en una sola operación transaccional.
            Si algún producto falla en la validación, se rechaza toda la operación.
            
            **Características:**
            - Operación transaccional (todo o nada)
            - Validación individual de cada producto
            - Verificación de nombres duplicados
            - Límite máximo de productos por batch
            
            **Límites:**
            - Máximo 50 productos por batch
            - Validaciones completas para cada producto
            """,
        tags = {"Products"}
    )
    public ResponseEntity<ApiResponse<List<ProductDTO>>> createProductsBatch(
            @Parameter(description = "Lista de productos a crear", required = true)
            @RequestBody @Valid List<CreateProductDTO> productDTOs
    ) {
        try {
            logger.debug("Iniciando creación batch de {} productos", productDTOs.size());
            
            // Validar límite
            if (productDTOs.size() > 50) {
                logger.warn("Intento de crear {} productos excede el límite de 50", productDTOs.size());
                ErrorResponse errorDetails = new ErrorResponse("BATCH_LIMIT_EXCEEDED", "No se pueden crear más de 50 productos por operación");
                ApiResponse<List<ProductDTO>> response = ApiResponse.error("Límite de productos excedido", errorDetails);
                return ResponseEntity.status(400).body(response);
            }
            
            List<Product> createdProducts = productService.createProductsBatch(productDTOs);
            
            List<ProductDTO> productDTOsResponse = createdProducts.stream()
                    .map(productMapper::toDTO)
                    .toList();
            
            logger.info("Batch de {} productos creado exitosamente", createdProducts.size());
            
            ApiResponse<List<ProductDTO>> response = ApiResponse.success(productDTOsResponse, "Productos creados exitosamente");
            return ResponseEntity.status(201).body(response);
            
        } catch (ProductAlreadyExistsException ex) {
            logger.warn("Error de duplicación en batch: {}", ex.getMessage());
            ErrorResponse errorDetails = new ErrorResponse("DUPLICATE_PRODUCT", ex.getMessage());
            ApiResponse<List<ProductDTO>> response = ApiResponse.error(ex.getMessage(), errorDetails);
            return ResponseEntity.status(400).body(response);
            
        } catch (ValidationException ex) {
            logger.warn("Error de validación en batch: {}", ex.getMessage());
            ErrorResponse errorDetails = new ErrorResponse("VALIDATION_ERROR", ex.getMessage());
            ApiResponse<List<ProductDTO>> response = ApiResponse.error(ex.getMessage(), errorDetails);
            return ResponseEntity.status(400).body(response);
            
        } catch (Exception ex) {
            logger.error("Error al crear productos en batch: {}", ex.getMessage(), ex);
            ErrorResponse errorDetails = new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
            ApiResponse<List<ProductDTO>> response = ApiResponse.error("Error interno del servidor", errorDetails);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * PROMPT 34: Actualizar múltiples productos
     */
    @PutMapping("/batch")
    @Operation(summary = "Actualizar múltiples productos", tags = {"Products"})
    public ResponseEntity<ApiResponse<List<ProductDTO>>> updateProductsBatch(
            @RequestBody @Valid List<UpdateProductDTO> productDTOs
    ) {
        try {
            List<Product> updatedProducts = productService.updateProductsBatch(productDTOs);
            List<ProductDTO> response = updatedProducts.stream().map(productMapper::toDTO).toList();
            return ResponseEntity.ok(ApiResponse.success(response, "Productos actualizados exitosamente"));
        } catch (Exception ex) {
            ErrorResponse error = new ErrorResponse("BATCH_UPDATE_ERROR", ex.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Error al actualizar productos", error));
        }
    }

    /**
     * PROMPT 35: Filtro por rango de fechas
     */
    @GetMapping("/date-range")
    @Operation(summary = "Filtrar productos por rango de fechas", tags = {"Products"})
    public ResponseEntity<ApiResponse<PagedResponse<ProductDTO>>> getProductsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products = productService.getProductsByDateRange(startDate, endDate, pageable);
            PagedResponse<ProductDTO> response = PagedResponse.of(products.map(productMapper::toDTO));
            return ResponseEntity.ok(ApiResponse.success(response, "Productos filtrados por fecha"));
        } catch (Exception ex) {
            ErrorResponse error = new ErrorResponse("DATE_FILTER_ERROR", ex.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Error al filtrar por fechas", error));
        }
    }

    /**
     * PROMPT 36: Productos más recientes
     */
    @GetMapping("/recent")
    @Operation(summary = "Obtener productos más recientes", tags = {"Products"})
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getRecentProducts(
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            List<Product> products = productService.getRecentProducts(limit);
            List<ProductDTO> response = products.stream().map(productMapper::toDTO).toList();
            return ResponseEntity.ok(ApiResponse.success(response, "Productos recientes obtenidos"));
        } catch (Exception ex) {
            ErrorResponse error = new ErrorResponse("RECENT_PRODUCTS_ERROR", ex.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Error al obtener productos recientes", error));
        }
    }

    /**
     * PROMPT 37: Conteo de productos por categoría
     */
    @GetMapping("/count-by-category")
    @Operation(summary = "Contar productos por categoría", tags = {"Products"})
    public ResponseEntity<ApiResponse<Map<String, Long>>> getProductCountByCategory() {
        try {
            Map<String, Long> counts = productService.getProductCountByCategory();
            return ResponseEntity.ok(ApiResponse.success(counts, "Conteo por categoría obtenido"));
        } catch (Exception ex) {
            ErrorResponse error = new ErrorResponse("COUNT_ERROR", ex.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Error al contar productos", error));
        }
    }

    /**
     * PROMPT 38: Duplicar producto
     */
    @PostMapping("/{id}/duplicate")
    @Operation(summary = "Duplicar producto", tags = {"Products"})
    public ResponseEntity<ApiResponse<ProductDTO>> duplicateProduct(
            @PathVariable Long id,
            @RequestParam(required = false) String newName
    ) {
        try {
            Product duplicated = productService.duplicateProduct(id, newName);
            ProductDTO response = productMapper.toDTO(duplicated);
            return ResponseEntity.status(201).body(ApiResponse.success(response, "Producto duplicado exitosamente"));
        } catch (ProductNotFoundException ex) {
            ErrorResponse error = new ErrorResponse("PRODUCT_NOT_FOUND", ex.getMessage());
            return ResponseEntity.status(404).body(ApiResponse.error(ex.getMessage(), error));
        } catch (Exception ex) {
            ErrorResponse error = new ErrorResponse("DUPLICATE_ERROR", ex.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Error al duplicar producto", error));
        }
    }

    /**
     * PROMPT 39: Productos relacionados (por categoría)
     */
    @GetMapping("/{id}/related")
    @Operation(summary = "Obtener productos relacionados", tags = {"Products"})
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getRelatedProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int limit
    ) {
        try {
            List<Product> related = productService.getRelatedProducts(id, limit);
            List<ProductDTO> response = related.stream().map(productMapper::toDTO).toList();
            return ResponseEntity.ok(ApiResponse.success(response, "Productos relacionados obtenidos"));
        } catch (ProductNotFoundException ex) {
            ErrorResponse error = new ErrorResponse("PRODUCT_NOT_FOUND", ex.getMessage());
            return ResponseEntity.status(404).body(ApiResponse.error(ex.getMessage(), error));
        } catch (Exception ex) {
            ErrorResponse error = new ErrorResponse("RELATED_ERROR", ex.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Error al obtener productos relacionados", error));
        }
    }

    /**
     * PROMPT 40: Resumen de inventario
     */
    @GetMapping("/inventory-summary")
    @Operation(summary = "Obtener resumen completo del inventario", tags = {"Products"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInventorySummary() {
        try {
            Map<String, Object> summary = productService.getInventorySummary();
            return ResponseEntity.ok(ApiResponse.success(summary, "Resumen de inventario obtenido"));
        } catch (Exception ex) {
            ErrorResponse error = new ErrorResponse("SUMMARY_ERROR", ex.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Error al generar resumen", error));
        }
    }

}