package prm.project.prm392backend.pojos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Users", schema = "SalesAppDB")
public class User {
    @Id
    @Column(name = "UserID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Username", nullable = false, length = 50)
    private String username;

    @Column(name = "PasswordHash", nullable = false)
    private String passwordHash;

    @Column(name = "Email", nullable = false, length = 100)
    private String email;

    @Column(name = "PhoneNumber", length = 15)
    private String phoneNumber;

    @Column(name = "Address")
    private String address;

    @Column(name = "Role", nullable = false, length = 50)
    private String role;

}