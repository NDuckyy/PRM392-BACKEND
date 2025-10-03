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
@Table(name = "ChatMessages", schema = "SalesAppDB")
public class ChatMessage {
    @Id
    @Column(name = "ChatMessageID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID")
    private User userID;

    @Lob
    @Column(name = "Message")
    private String message;

    @Column(name = "SentAt", nullable = false)
    private Date sentAt;

}