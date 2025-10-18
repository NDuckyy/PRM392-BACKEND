package prm.project.prm392backend.dtos;

import jakarta.persistence.Column;
import lombok.*;
import prm.project.prm392backend.pojos.User;

import java.util.Date;


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

}
