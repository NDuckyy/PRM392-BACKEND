package prm.project.prm392backend.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponse {
    Integer id;
    Integer productId;
    String productName;
    String imageURL;
    BigDecimal price;
    Integer quantity;
}
