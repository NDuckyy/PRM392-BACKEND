package prm.project.prm392backend.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartInsertResponse {
    String message;
    Integer cartId;
    BigDecimal newTotal;
}
