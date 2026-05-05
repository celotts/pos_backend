package com.celotts.pos.infrastructure.adapter.persistence;

import com.celotts.pos.application.port.out.ProductRepositoryPort;
import com.celotts.pos.domain.model.Product;
import com.celotts.pos.infrastructure.adapter.persistence.entity.ProductEntity;
import com.celotts.pos.infrastructure.adapter.persistence.repository.SpringDataProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final SpringDataProductRepository springDataProductRepository;

    public ProductRepositoryAdapter(SpringDataProductRepository springDataProductRepository) {
        this.springDataProductRepository = springDataProductRepository;
    }

    @Override
    public Product save(Product product) {
        ProductEntity productEntity = toEntity(product);
        ProductEntity savedEntity = springDataProductRepository.save(productEntity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Product> findById(String id) {
        return springDataProductRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return springDataProductRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        springDataProductRepository.deleteById(id);
    }

    private ProductEntity toEntity(Product product) {
        return new ProductEntity(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock()
        );
    }

    private Product toDomain(ProductEntity productEntity) {
        return new Product(
                productEntity.getId(),
                productEntity.getName(),
                productEntity.getDescription(),
                productEntity.getPrice(),
                productEntity.getStock()
        );
    }
}
