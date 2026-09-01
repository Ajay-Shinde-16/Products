package com.zest.productapi.config;

import com.zest.productapi.entity.Item;
import com.zest.productapi.entity.Product;
import com.zest.productapi.entity.Role;
import com.zest.productapi.entity.User;
import com.zest.productapi.repository.ItemRepository;
import com.zest.productapi.repository.ProductRepository;
import com.zest.productapi.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// creates a default admin and some sample data on first run
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ItemRepository itemRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ProductRepository productRepository,
                           ItemRepository itemRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.itemRepository = itemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // already seeded
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);

        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("user123"));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);

        Product product = new Product();
        product.setProductName("Sample Laptop");
        product.setCreatedBy("admin");
        product.setCreatedOn(LocalDateTime.now());
        Product saved = productRepository.save(product);

        Item item = new Item();
        item.setProduct(saved);
        item.setQuantity(5);
        itemRepository.save(item);
    }
}
