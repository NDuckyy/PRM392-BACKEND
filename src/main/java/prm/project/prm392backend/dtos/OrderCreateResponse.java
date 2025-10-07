package prm.project.prm392backend.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderCreateResponse {
    Integer orderId;
    Integer userId;
    BigDecimal totalPrice;
    String paymentMethod;
    String billingAddress;
    String orderStatus;
    LocalDateTime orderDate;

}
