package com.zest.productapi.controller;

import com.zest.productapi.dto.ItemResponse;
import com.zest.productapi.dto.PagedResponse;
import com.zest.productapi.dto.ProductRequest;
import com.zest.productapi.dto.ProductResponse;
import com.zest.productapi.service.ItemService;
import com.zest.productapi.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final ItemService itemService;

    public ProductController(ProductService productService, ItemService itemService) {
        this.productService = productService;
        this.itemService = itemService;
    }

    // list products with pagination
    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getAll(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    // only admins can create
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request,
                                                  Authentication authentication) {
        ProductResponse response = productService.create(request, authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable Integer id,
                                                  @Valid @RequestBody ProductRequest request,
                                                  Authentication authentication) {
        return ResponseEntity.ok(productService.update(id, request, authentication.getName()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // items that belong to a product
    @GetMapping("/{id}/items")
    public ResponseEntity<List<ItemResponse>> getItems(@PathVariable Integer id) {
        return ResponseEntity.ok(itemService.getItemsByProduct(id));
    }
}
