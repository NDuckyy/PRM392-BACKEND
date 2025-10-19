package prm.project.prm392backend.controllers;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.dtos.ApiResponse;
import prm.project.prm392backend.dtos.ChatDtos.*;
import prm.project.prm392backend.exceptions.AppException;
import prm.project.prm392backend.exceptions.ErrorCode;

import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private Firestore db() { return FirestoreClient.getFirestore(); }

    // ADMIN gửi tin -> ghi Firestore + cập nhật conversations + (tuỳ chọn) bắn FCM
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<MessageResponse>> send(@RequestBody SendMessageRequest req) {
        if (req == null) throw new AppException(ErrorCode.MISSING_PARAMETER);
        if (req.conversationId() == null || req.conversationId().isBlank()) {
            throw new AppException(ErrorCode.CONVERSATION_ID_REQUIRED);
        }
        if (req.message() == null || req.message().isBlank()) {
            throw new AppException(ErrorCode.MESSAGE_REQUIRED);
        }

        String senderType = (req.senderType() == null || req.senderType().isBlank())
                ? "ADMIN" : req.senderType();

        try {
            // 1) Ghi vào subcollection messages
            CollectionReference msgs = db()
                    .collection("conversations")
                    .document(req.conversationId())
                    .collection("messages");

            Map<String, Object> doc = new HashMap<>();
            doc.put("conversationId", req.conversationId());
            doc.put("senderType", senderType);
            doc.put("senderId", req.senderId());
            doc.put("message", req.message());
            doc.put("sentAt", FieldValue.serverTimestamp());

            DocumentReference savedRef = msgs.add(doc).get();

            // 2) Cập nhật summary ở conversations/{cid}
            DocumentReference convRef = db().collection("conversations").document(req.conversationId());
            Map<String, Object> convUpsert = new HashMap<>();
            convUpsert.put("lastMessage", req.message());
            convUpsert.put("lastSender", senderType);
            convUpsert.put("updatedAt", FieldValue.serverTimestamp());
            if (req.userFcmToken() != null && !req.userFcmToken().isBlank()) {
                convUpsert.put("userFcmToken", req.userFcmToken());
            }
            convRef.set(convUpsert, SetOptions.merge()).get();

            // 3) Lấy timestamp thực tế từ Firestore
            Timestamp ts = savedRef.get().get().getTimestamp("sentAt");
            long millis = (ts != null) ? ts.toDate().getTime() : System.currentTimeMillis();

            // 4) (Tuỳ chọn) gửi FCM cho khách
            if (req.userFcmToken() != null && !req.userFcmToken().isBlank()) {
                try {
                    Notification nf = Notification.builder()
                            .setTitle("Hỗ trợ phản hồi")
                            .setBody(req.message())
                            .build();

                    Message fcm = Message.builder()
                            .setToken(req.userFcmToken())
                            .setNotification(nf)
                            .putData("type", "chat")
                            .putData("conversationId", req.conversationId())
                            .build();

                    FirebaseMessaging.getInstance().send(fcm);
                } catch (FirebaseMessagingException ignore) {
                    // Có thể log warn, nhưng không fail request
                }
            }

            MessageResponse data = new MessageResponse(
                    savedRef.getId(),
                    req.conversationId(),
                    senderType,
                    req.senderId(),
                    req.message(),
                    millis
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(data, "Message sent"));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR);
        } catch (ExecutionException e) {
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR);
        }
    }

    // Lấy lịch sử 1 cuộc hội thoại
    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> list(
            @RequestParam String conversationId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new AppException(ErrorCode.CONVERSATION_ID_REQUIRED);
        }

        try {
            CollectionReference msgs = db()
                    .collection("conversations")
                    .document(conversationId)
                    .collection("messages");

            ApiFuture<QuerySnapshot> fut = msgs
                    .orderBy("sentAt", Query.Direction.ASCENDING)
                    .limit(Math.max(1, Math.min(limit, 500)))
                    .get();

            List<MessageResponse> out = new ArrayList<>();
            for (DocumentSnapshot d : fut.get().getDocuments()) {
                Timestamp ts = d.getTimestamp("sentAt");
                out.add(new MessageResponse(
                        d.getId(),
                        conversationId,
                        d.getString("senderType"),
                        Objects.toString(d.get("senderId"), null),
                        d.getString("message"),
                        ts != null ? ts.toDate().getTime() : 0L
                ));
            }

            return ResponseEntity.ok(ApiResponse.ok(out, "Fetched messages successfully"));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR);
        } catch (ExecutionException e) {
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR);
        }
    }

    // Liệt kê danh sách hội thoại cho bảng Admin
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationSummary>>> conversations(
            @RequestParam(defaultValue = "50") int limit
    ) {
        try {
            ApiFuture<QuerySnapshot> fut = db().collection("conversations")
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .limit(Math.max(1, Math.min(limit, 200)))
                    .get();

            List<ConversationSummary> list = new ArrayList<>();
            for (DocumentSnapshot d : fut.get().getDocuments()) {
                Timestamp ts = d.getTimestamp("updatedAt");
                list.add(new ConversationSummary(
                        d.getId(),
                        d.getString("lastMessage"),
                        d.getString("lastSender"),
                        ts != null ? ts.toDate().getTime() : 0L
                ));
            }

            return ResponseEntity.ok(ApiResponse.ok(list, "Fetched conversations successfully"));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR);
        } catch (ExecutionException e) {
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR);
        }
    }
}
