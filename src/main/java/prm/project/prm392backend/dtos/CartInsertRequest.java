package prm.project.prm392backend.dtos;

import lombok.Data;

@Data
public class CartInsertRequest {
    Integer productId;
    Integer quantity;
}
