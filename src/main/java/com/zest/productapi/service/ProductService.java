package com.zest.productapi.service;

import com.zest.productapi.dto.PagedResponse;
import com.zest.productapi.dto.ProductRequest;
import com.zest.productapi.dto.ProductResponse;
import com.zest.productapi.entity.Product;
import com.zest.productapi.exception.ResourceNotFoundException;
import com.zest.productapi.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse create(ProductRequest request, String username) {
        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setCreatedBy(username);
        product.setCreatedOn(LocalDateTime.now());

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    public PagedResponse<ProductResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> result = productRepository.findAll(pageable);

        List<ProductResponse> content = result.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast());
    }

    public ProductResponse getById(Integer id) {
        Product product = findProduct(id);
        return toResponse(product);
    }

    public ProductResponse update(Integer id, ProductRequest request, String username) {
        Product product = findProduct(id);
        product.setProductName(request.getProductName());
        product.setModifiedBy(username);
        product.setModifiedOn(LocalDateTime.now());

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    public void delete(Integer id) {
        Product product = findProduct(id);
        productRepository.delete(product);
    }

    // helper to load a product or throw 404
    private Product findProduct(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    // convert entity to response dto
    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setProductName(product.getProductName());
        response.setCreatedBy(product.getCreatedBy());
        response.setCreatedOn(product.getCreatedOn());
        response.setModifiedBy(product.getModifiedBy());
        response.setModifiedOn(product.getModifiedOn());
        return response;
    }
}
