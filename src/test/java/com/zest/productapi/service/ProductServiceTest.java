package com.zest.productapi.service;

import com.zest.productapi.dto.ProductRequest;
import com.zest.productapi.dto.ProductResponse;
import com.zest.productapi.entity.Product;
import com.zest.productapi.exception.ResourceNotFoundException;
import com.zest.productapi.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_shouldReturnSavedProduct() {
        ProductRequest request = new ProductRequest();
        request.setProductName("Test Product");

        Product saved = new Product();
        saved.setId(1);
        saved.setProductName("Test Product");
        saved.setCreatedBy("admin");
        saved.setCreatedOn(LocalDateTime.now());

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.create(request, "admin");

        assertNotNull(response);
        assertEquals("Test Product", response.getProductName());
        assertEquals("admin", response.getCreatedBy());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void getById_whenExists_shouldReturnProduct() {
        Product product = new Product();
        product.setId(1);
        product.setProductName("Laptop");

        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getById(1);

        assertEquals("Laptop", response.getProductName());
    }

    @Test
    void getById_whenNotFound_shouldThrow() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getById(99));
    }

    @Test
    void deleteProduct_whenNotFound_shouldThrow() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.delete(99));
        verify(productRepository, never()).delete(any());
    }
}
