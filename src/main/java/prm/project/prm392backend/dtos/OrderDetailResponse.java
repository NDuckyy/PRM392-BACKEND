package prm.project.prm392backend.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDetailResponse {
    private Integer id;
    private Integer productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}