package com.celotts.pos.application.port.in.product;

import com.celotts.pos.domain.model.product.Product;

import java.util.List;
import java.util.Optional;

public interface ProductServicePort {
    Product createProduct(Product product); // Eliminada la declaración duplicada
    Optional<Product> getProductById(String id);
    List<Product> getAllProducts();
    Product updateProduct(String id, Product product);
    void deleteProduct(String id);
}
