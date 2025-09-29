package com.eafit.tutorial.config;

import com.eafit.tutorial.model.Product;
import com.eafit.tutorial.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Cargador de datos inicial para la aplicación.
 * Se ejecuta al inicio de la aplicación y crea productos de prueba
 * si la base de datos está vacía.
 * 
 * @author Juan Pablo Rua
 * @version 1.0
 * @since 2025-09-29
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // Solo cargar datos si la base de datos está vacía
        if (productRepository.count() == 0) {
            logger.info("Base de datos vacía. Cargando productos de prueba...");
            loadSampleProducts();
            logger.info("Productos de prueba cargados exitosamente. Total: {}", productRepository.count());
        } else {
            logger.info("Base de datos ya contiene {} productos. No se cargarán datos de prueba.", 
                       productRepository.count());
        }
    }

    /**
     * Carga 20 productos de prueba con diferentes categorías, precios y stocks.
     */
    private void loadSampleProducts() {
        List<Product> products = Arrays.asList(
            // Electrónicos (5 productos)
            createProduct("Laptop HP Pavilion 15", 
                         "Laptop HP Pavilion 15 con Intel i5, 8GB RAM, 256GB SSD", 
                         new BigDecimal("1299.99"), "Electrónicos", 15),
            
            createProduct("Smartphone Samsung Galaxy S23", 
                         "Samsung Galaxy S23 con 128GB de almacenamiento y cámara de 50MP", 
                         new BigDecimal("799.99"), "Electrónicos", 25),
            
            createProduct("Auriculares Sony WH-1000XM4", 
                         "Auriculares inalámbricos con cancelación de ruido activa", 
                         new BigDecimal("299.99"), "Electrónicos", 40),
            
            createProduct("Tablet iPad Air", 
                         "iPad Air de 10.9 pulgadas con chip M1 y 64GB", 
                         new BigDecimal("649.99"), "Electrónicos", 20),
            
            createProduct("Smart TV Samsung 55\"", 
                         "Smart TV Samsung 55 pulgadas 4K UHD con Tizen OS", 
                         new BigDecimal("699.99"), "Electrónicos", 8),
            
            // Libros (4 productos)
            createProduct("Clean Code", 
                         "A Handbook of Agile Software Craftsmanship por Robert C. Martin", 
                         new BigDecimal("45.99"), "Libros", 100),
            
            createProduct("Design Patterns", 
                         "Elements of Reusable Object-Oriented Software", 
                         new BigDecimal("52.99"), "Libros", 75),
            
            createProduct("The Pragmatic Programmer", 
                         "Your Journey to Mastery, 20th Anniversary Edition", 
                         new BigDecimal("39.99"), "Libros", 60),
            
            createProduct("Java: The Complete Reference", 
                         "Eleventh Edition por Herbert Schildt", 
                         new BigDecimal("49.99"), "Libros", 80),
            
            // Ropa (4 productos)
            createProduct("Camiseta Nike Dri-FIT", 
                         "Camiseta deportiva Nike Dri-FIT para hombre, talla M", 
                         new BigDecimal("29.99"), "Ropa", 150),
            
            createProduct("Jeans Levi's 501", 
                         "Jeans clásicos Levi's 501 original fit, talla 32", 
                         new BigDecimal("89.99"), "Ropa", 75),
            
            createProduct("Zapatillas Adidas Ultraboost", 
                         "Zapatillas de running Adidas Ultraboost 22, talla 42", 
                         new BigDecimal("179.99"), "Ropa", 50),
            
            createProduct("Chaqueta The North Face", 
                         "Chaqueta impermeable The North Face para outdoor", 
                         new BigDecimal("149.99"), "Ropa", 30),
            
            // Hogar (4 productos)
            createProduct("Cafetera Nespresso Vertuo", 
                         "Cafetera Nespresso Vertuo Plus con tecnología Centrifusion", 
                         new BigDecimal("199.99"), "Hogar", 25),
            
            createProduct("Aspiradora Dyson V11", 
                         "Aspiradora inalámbrica Dyson V11 con motor digital", 
                         new BigDecimal("599.99"), "Hogar", 12),
            
            createProduct("Set de Sartenes Tefal", 
                         "Set de 3 sartenes antiadherentes Tefal Professional", 
                         new BigDecimal("89.99"), "Hogar", 35),
            
            createProduct("Purificador de Aire Xiaomi", 
                         "Purificador de aire Xiaomi Mi Air Purifier 3H", 
                         new BigDecimal("159.99"), "Hogar", 18),
            
            // Deportes (3 productos)
            createProduct("Bicicleta Trek FX 3", 
                         "Bicicleta híbrida Trek FX 3 Disc con frenos de disco", 
                         new BigDecimal("749.99"), "Deportes", 10),
            
            createProduct("Pelota de Fútbol Adidas", 
                         "Pelota oficial Adidas Tango España con tecnología FIFA", 
                         new BigDecimal("39.99"), "Deportes", 60),
            
            createProduct("Pesas Ajustables Bowflex", 
                         "Set de pesas ajustables Bowflex SelectTech 552", 
                         new BigDecimal("349.99"), "Deportes", 15)
        );

        // Guardar todos los productos
        productRepository.saveAll(products);
    }

    /**
     * Método auxiliar para crear un producto con los parámetros dados.
     * 
     * @param name Nombre del producto
     * @param description Descripción del producto
     * @param price Precio del producto
     * @param category Categoría del producto
     * @param stock Stock inicial del producto
     * @return Producto creado
     */
    private Product createProduct(String name, String description, BigDecimal price, 
                                 String category, Integer stock) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setStock(stock);
        product.setActive(true);
        return product;
    }
}