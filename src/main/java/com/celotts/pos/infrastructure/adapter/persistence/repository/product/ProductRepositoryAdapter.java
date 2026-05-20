package com.celotts.pos.infrastructure.adapter.persistence.repository.product;

import com.celotts.pos.application.port.out.product.ProductRepositoryPort;
import com.celotts.pos.domain.model.product.Product;
import com.celotts.pos.infrastructure.adapter.persistence.entity.product.ProductEntity;
import com.celotts.pos.infrastructure.adapter.persistence.repository.product.SpringDataProductRepository;
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
        // Product es un record, usa los métodos de acceso directos (id(), name(), etc.)
        return new ProductEntity(
                product.id(),
                product.name(),
                product.description(),
                product.price(),
                product.stock()
        );
    }

    private Product toDomain(ProductEntity productEntity) {
        // ProductEntity NO es un record, usa los getters tradicionales (getId(), getName(), etc.)
        return new Product(
                productEntity.getId(),
                productEntity.getName(),
                productEntity.getDescription(),
                productEntity.getPrice(),
                productEntity.getStock()
        );
    }
}
