package prm.project.prm392backend.pojos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "StoreLocations", schema = "SalesAppDB")
public class StoreLocation {
    @Id
    @Column(name = "LocationID", nullable = false)
    private Integer id;

    @Column(name = "Latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "Longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "Address", nullable = false)
    private String address;

}