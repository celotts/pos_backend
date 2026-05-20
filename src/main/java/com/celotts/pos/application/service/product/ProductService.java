package com.celotts.pos.application.service.product;

import com.celotts.pos.application.port.in.product.ProductServicePort;
import com.celotts.pos.application.port.out.product.ProductRepositoryPort;
import com.celotts.pos.domain.model.product.Product;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService implements ProductServicePort {

    private final ProductRepositoryPort productRepositoryPort;

    public ProductService(ProductRepositoryPort productRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
    }

    @Override
    public Product createProduct(Product product) {
        // Records son inmutables. Si el ID es nulo, creamos una nueva instancia con un ID generado.
        if (product.id() == null || product.id().isEmpty()) {
            product = new Product(
                UUID.randomUUID().toString(),
                product.name(),
                product.description(),
                product.price(),
                product.stock()
            );
        }
        return productRepositoryPort.save(product);
    }

    @Override
    public Optional<Product> getProductById(String id) {
        return productRepositoryPort.findById(id);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepositoryPort.findAll();
    }

    @Override
    public Product updateProduct(String id, Product updatedProduct) {
        return productRepositoryPort.findById(id).map(existingProduct -> {
            // Records son inmutables. Creamos una nueva instancia con los campos actualizados.
            Product productToSave = new Product(
                existingProduct.id(), // El ID no cambia
                updatedProduct.name(),
                updatedProduct.description(),
                updatedProduct.price(),
                updatedProduct.stock()
            );
            return productRepositoryPort.save(productToSave);
        }).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public void deleteProduct(String id) {
        productRepositoryPort.deleteById(id);
    }
}
