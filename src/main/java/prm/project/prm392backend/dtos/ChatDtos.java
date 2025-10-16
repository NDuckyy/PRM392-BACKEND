package prm.project.prm392backend.dtos;

public class ChatDtos {
    public record SendMessageRequest(
            String conversationId,   // ví dụ "user-42"
            String senderType,       // "ADMIN" | "USER" (admin API bạn mặc định "ADMIN" cũng được)
            String senderId,         // id admin
            String message,
            String userFcmToken      // optional: để bắn push
    ) {}

    public record MessageResponse(
            String id,
            String conversationId,
            String senderType,
            String senderId,
            String message,
            long sentAtMillis
    ) {}

    public record ConversationSummary(
            String conversationId,
            String lastMessage,
            String lastSender,
            long updatedAtMillis
    ) {}
}
