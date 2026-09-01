package com.zest.productapi.service;

import com.zest.productapi.dto.ItemResponse;
import com.zest.productapi.exception.ResourceNotFoundException;
import com.zest.productapi.repository.ItemRepository;
import com.zest.productapi.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ProductRepository productRepository;

    public ItemService(ItemRepository itemRepository, ProductRepository productRepository) {
        this.itemRepository = itemRepository;
        this.productRepository = productRepository;
    }

    // get all items that belong to a product
    public List<ItemResponse> getItemsByProduct(Integer productId) {
        // first make sure the product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id " + productId);
        }

        return itemRepository.findByProductId(productId).stream()
                .map(item -> {
                    ItemResponse response = new ItemResponse();
                    response.setId(item.getId());
                    response.setProductId(item.getProduct().getId());
                    response.setQuantity(item.getQuantity());
                    return response;
                })
                .collect(Collectors.toList());
    }
}
