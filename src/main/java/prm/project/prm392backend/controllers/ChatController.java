package prm.project.prm392backend.controllers;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.*;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.dtos.ApiResponse;
import prm.project.prm392backend.dtos.ChatDtos.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private Firestore db() { return FirestoreClient.getFirestore(); }

    // ADMIN gửi tin -> ghi Firestore + cập nhật conversations + (tuỳ chọn) bắn FCM
    @PostMapping("/send")
    public ApiResponse<MessageResponse> send(@RequestBody SendMessageRequest req) {
        ApiResponse<MessageResponse> res = new ApiResponse<>();

        if (req == null || req.conversationId() == null || req.conversationId().isBlank()
                || req.message() == null || req.message().isBlank()) {
            res.setCode(400);
            res.setMessage("conversationId & message required");
            res.setData(null);
            return res;
        }
        String senderType = (req.senderType() == null) ? "ADMIN" : req.senderType();

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

            // Đọc lại timestamp thực tế
            DocumentSnapshot snap = savedRef.get().get();
            Timestamp ts = snap.getTimestamp("sentAt");
            long millis = (ts != null) ? ts.toDate().getTime() : System.currentTimeMillis();

            // 3) (Tuỳ chọn) gửi FCM cho khách
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
                    // log nếu cần
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

            res.setCode(201); // Created (vẫn trả HTTP 200, client đọc code này)
            res.setMessage("Message sent");
            res.setData(data);
            return res;

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            res.setCode(500);
            res.setMessage(e.getMessage());
            res.setData(null);
            return res;
        }
    }

    // Lấy lịch sử 1 cuộc hội thoại
    @GetMapping("/messages")
    public ApiResponse<List<MessageResponse>> list(
            @RequestParam String conversationId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        ApiResponse<List<MessageResponse>> res = new ApiResponse<>();
        if (conversationId == null || conversationId.isBlank()) {
            res.setCode(400);
            res.setMessage("conversationId is required");
            res.setData(null);
            return res;
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

            res.setCode(200);
            res.setMessage("Fetched messages successfully");
            res.setData(out);
            return res;

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            res.setCode(500);
            res.setMessage(e.getMessage());
            res.setData(null);
            return res;
        }
    }

    // Liệt kê danh sách hội thoại cho bảng Admin
    @GetMapping("/conversations")
    public ApiResponse<List<ConversationSummary>> conversations(
            @RequestParam(defaultValue = "50") int limit
    ) {
        ApiResponse<List<ConversationSummary>> res = new ApiResponse<>();
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

            res.setCode(200);
            res.setMessage("Fetched conversations successfully");
            res.setData(list);
            return res;

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            res.setCode(500);
            res.setMessage(e.getMessage());
            res.setData(null);
            return res;
        }
    }
}
