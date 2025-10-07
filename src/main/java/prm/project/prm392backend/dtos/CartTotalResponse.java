package prm.project.prm392backend.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartTotalResponse {
    Integer userId;
    BigDecimal totalPrice;
    String status;
}
