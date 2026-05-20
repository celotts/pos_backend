package com.celotts.pos.domain.model.product;

import java.math.BigDecimal;


// Declaración de un record para Product
public record Product(
    String id,
    String name,
    String description,
    BigDecimal price,
    int stock
) {

}
