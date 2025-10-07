package prm.project.prm392backend.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemUpdateResponse {
    String message;
    Integer cartId;
    BigDecimal totalPrice;
}
