package prm.project.prm392backend.pojos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Categories", schema = "SalesAppDB")
public class Category {
    @Id
    @Column(name = "CategoryID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "CategoryName", nullable = false, length = 100)
    private String categoryName;

}