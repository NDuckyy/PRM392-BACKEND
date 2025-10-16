package prm.project.prm392backend.pojos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "ChatMessages", schema = "SalesAppDB")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ChatMessageID", nullable = false)
    private Integer id;

    // người gửi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User sender;

    // phòng (room) — nếu bạn chat 1–1 có thể là "userA_userB", còn livechat với store thì là "ticket/session id"
    @Column(name = "RoomID", nullable = false, length = 100)
    private String roomId;

    @Lob
    @Column(name = "Message", nullable = false)
    private String message;

    @CreationTimestamp
    @Column(name = "SentAt", nullable = false, updatable = false)
    private Instant sentAt;
}
