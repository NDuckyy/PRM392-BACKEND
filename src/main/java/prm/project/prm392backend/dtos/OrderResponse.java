package prm.project.prm392backend.dtos;

import jakarta.persistence.Column;
import lombok.*;
import prm.project.prm392backend.pojos.OrderDetail;
import prm.project.prm392backend.pojos.User;

import java.util.Date;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private UserResponse userID;
    private String paymentMethod;
    private String billingAddress;
    private String orderStatus;
    private Date orderDate;
    private List<OrderDetailResponse> orderDetails;

}
