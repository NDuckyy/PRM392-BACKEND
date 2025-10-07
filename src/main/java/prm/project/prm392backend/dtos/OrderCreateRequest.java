package prm.project.prm392backend.dtos;

import lombok.Data;

@Data
public class OrderCreateRequest {
    Integer userId;
    String paymentMethod;
    String billingAddress;
}
