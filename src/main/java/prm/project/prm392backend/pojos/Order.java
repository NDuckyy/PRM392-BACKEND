package prm.project.prm392backend.pojos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "Orders", schema = "SalesAppDB")
public class Order {
    @Id
    @Column(name = "OrderID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CartID")
    private Cart cartID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID")
    private User userID;

    @Column(name = "PaymentMethod", nullable = false, length = 50)
    private String paymentMethod;

    @Column(name = "BillingAddress", nullable = false)
    private String billingAddress;

    @Column(name = "OrderStatus", nullable = false, length = 50)
    private String orderStatus;

    @Column(name = "OrderDate", nullable = false)
    private Date orderDate;

    @ManyToOne
    @JoinColumn(name = "ProviderID")
    private Provider provider;

}