package prm.project.prm392backend.pojos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Providers", schema = "SalesAppDB")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProviderID", nullable = false)
    private Integer id;
    @Column(name = "ProviderName")
    private String providerName;
    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;
}
