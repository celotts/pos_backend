package com.celotts.pos.application.port.out.product;

import com.celotts.pos.domain.model.product.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {
    Product save(Product product);
    Optional<Product> findById(String id);
    List<Product> findAll();
    void deleteById(String id);
}
