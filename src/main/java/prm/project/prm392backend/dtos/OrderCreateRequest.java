package prm.project.prm392backend.dtos;

import lombok.Data;

@Data
public class OrderCreateRequest {
    String paymentMethod;
    String billingAddress;
}
