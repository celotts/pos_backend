package com.celotts.pos.infrastructure.adapter.persistence.repository;

import com.celotts.pos.infrastructure.adapter.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// La importación de Optional ya no es estrictamente necesaria aquí, pero no hace daño
// import java.util.Optional;

@Repository
public interface SpringDataProductRepository extends JpaRepository<ProductEntity, String> {
    // JpaRepository ya proporciona save, findById, findAll, deleteById, etc.
    // No es necesario declarar explícitamente los métodos que ya están en JpaRepository.
}
