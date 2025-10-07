package prm.project.prm392backend.dtos;

import lombok.Data;

@Data
public class CartInsertRequest {
    Integer userId;
    Integer productId;
    Integer quantity;
}
