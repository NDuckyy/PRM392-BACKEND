package prm.project.prm392backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import prm.project.prm392backend.dtos.ApiResponse;
import prm.project.prm392backend.exceptions.AppException;
import prm.project.prm392backend.exceptions.ErrorCode;
import prm.project.prm392backend.pojos.Order;
import prm.project.prm392backend.pojos.Payment;
import prm.project.prm392backend.repositories.OrderRepository;
import prm.project.prm392backend.repositories.PaymentRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @PostMapping("/url/{orderId}")
    public ResponseEntity<ApiResponse<String>> createPaymentUrl(@PathVariable Integer orderId) {
        Order order = orderRepository.findOrderById(orderId);
        if (order == null) throw new AppException(ErrorCode.ORDER_NOT_FOUND);

        if (!"WAITING PAYMENT".equals(order.getOrderStatus())) {
            throw new AppException(ErrorCode.ORDER_INVALID_STATE);
        }

        try {
            // ===== Hard-coded keys as requested =====
            final String tmnCode = "U3CV658K";
            final String secretKey = "33X3QNHXS4MSQ39G58DZQ1R2XD05XCQQ";
            final String vnpUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
            final String returnUrl = "prm392://payment/result" + order.getId();
            final String currCode = "VND";
            // ========================================

            // amount phải nhân 100 (đơn vị nhỏ nhất)
            BigDecimal amountVnd = order.getCartID().getTotalPrice();
            if (amountVnd == null || amountVnd.compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException(ErrorCode.PAYMENT_URL_CREATE_FAILED);
            }
            String amount = amountVnd.multiply(BigDecimal.valueOf(100)).toBigInteger().toString();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String createDate = LocalDateTime.now().format(formatter);

            Map<String, String> vnpParams = new TreeMap<>(); // giữ thứ tự alpha để ký
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", tmnCode);
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_CurrCode", currCode);
            vnpParams.put("vnp_TxnRef", String.valueOf(order.getId()));
            vnpParams.put("vnp_OrderInfo", "Thanh toan cho ma GD: " + order.getId());
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Amount", amount);
            vnpParams.put("vnp_ReturnUrl", returnUrl);
            vnpParams.put("vnp_CreateDate", createDate);
            vnpParams.put("vnp_IpAddr", "127.0.0.1"); // tuỳ bạn lấy IP thật từ request

            // build chuỗi ký
            StringBuilder signDataBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
                signDataBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                signDataBuilder.append("=");
                signDataBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                signDataBuilder.append("&");
            }
            signDataBuilder.deleteCharAt(signDataBuilder.length() - 1);

            String signData = signDataBuilder.toString();
            String signed = hmacSHA512(secretKey, signData);

            vnpParams.put("vnp_SecureHash", signed);

            // build URL
            StringBuilder urlBuilder = new StringBuilder(vnpUrl).append("?");
            for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
                urlBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                        .append("&");
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);

            return ResponseEntity.ok(ApiResponse.ok(urlBuilder.toString(), "Created payment URL successfully"));
        } catch (Exception e) {
            throw new AppException(ErrorCode.PAYMENT_URL_CREATE_FAILED);
        }
    }

    @PutMapping("/{orderId}")
    @Transactional
    public ResponseEntity<ApiResponse<Payment>> updatePayment(@PathVariable Integer orderId) {
        Order order = orderRepository.findOrderById(orderId);
        if (order == null) throw new AppException(ErrorCode.ORDER_NOT_FOUND);

        if (!"WAITING PAYMENT".equals(order.getOrderStatus())) {
            throw new AppException(ErrorCode.ORDER_INVALID_STATE);
        }

        try {
            // cập nhật trạng thái Order + tạo Payment
            order.setOrderStatus("PAID");
            orderRepository.save(order);

            Payment payment = new Payment();
            payment.setPaymentStatus("SUCCESS");
            payment.setPaymentDate(new Date());
            payment.setAmount(order.getCartID().getTotalPrice());
            payment.setOrderID(order);
            paymentRepository.save(payment);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.ok(payment, "Payment updated successfully"));
        } catch (Exception e) {
            throw new AppException(ErrorCode.PAYMENT_UPDATE_FAILED);
        }
    }

    // Helpers
    private static String hmacSHA512(String secretKey, String data)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
