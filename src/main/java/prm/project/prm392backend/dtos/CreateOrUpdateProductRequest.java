package prm.project.prm392backend.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateOrUpdateProductRequest {
    private String productName;
    private String briefDescription;
    private String fullDescription;
    private String technicalSpecifications;
    private BigDecimal price;
    private String imageURL;
    private Integer stockQuantity;
    private Integer categoryId; // ID của category
}