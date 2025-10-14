package prm.project.prm392backend.pojos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "Products", schema = "SalesAppDB")
public class Product {
    @Id
    @Column(name = "ProductID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ProductName", nullable = false, length = 100)
    private String productName;

    @Column(name = "BriefDescription")
    private String briefDescription;

    @Lob
    @Column(name = "FullDescription")
    private String fullDescription;

    @Lob
    @Column(name = "TechnicalSpecifications")
    private String technicalSpecifications;

    @Column(name = "Price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "ImageURL")
    private String imageURL;

    @ManyToOne
    @JoinColumn(name = "CategoryID")
    private Category categoryID;

}