package prm.project.prm392backend.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponse {
    Integer id;
    Integer userId;
    BigDecimal totalPrice;
    List<CartItemResponse> cartItemResponses;
}
