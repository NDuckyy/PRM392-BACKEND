package prm.project.prm392backend.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin("*")
public class PaymentController {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @PostMapping("/url/{orderId}")
    public ResponseEntity<?> createPaymentUrl(@PathVariable Integer orderId)throws Exception{
        Order order = orderRepository.findOrderById(orderId);
        if(order.getOrderStatus() == "WAITING PAYMENT"){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime createDate = LocalDateTime.now();
            String formattedCreateDate = createDate.format(formatter);

            String orderID = String.valueOf(order.getId());
            BigDecimal totalPrice = order.getCartID().getTotalPrice().multiply(BigDecimal.valueOf(100));
            String amount = String.valueOf(totalPrice.intValue());

            String tmnCode = "U3CV658K";
            String secretKey = "33X3QNHXS4MSQ39G58DZQ1R2XD05XCQQ";
            String vnpUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
            String returnUrl = "#" + order.getId();
            String currCode = "VND";

            Map<String, String> vnpParams = new TreeMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", tmnCode);
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_CurrCode", currCode);
            vnpParams.put("vnp_TxnRef", orderID);
            vnpParams.put("vnp_OrderInfo", "Thanh toan cho ma GD: " + order.getId());
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Amount", amount);

            vnpParams.put("vnp_ReturnUrl", returnUrl);
            vnpParams.put("vnp_CreateDate", formattedCreateDate);
            vnpParams.put("vnp_IpAddr", "127.0.0.1");

            StringBuilder signDataBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
                signDataBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
                signDataBuilder.append("=");
                signDataBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
                signDataBuilder.append("&");
            }
            signDataBuilder.deleteCharAt(signDataBuilder.length() - 1); // Remove last '&'

            String signData = signDataBuilder.toString();
            String signed = generateHMAC(secretKey, signData);

            vnpParams.put("vnp_SecureHash", signed);

            StringBuilder urlBuilder = new StringBuilder(vnpUrl);
            urlBuilder.append("?");
            for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
                urlBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
                urlBuilder.append("=");
                urlBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
                urlBuilder.append("&");
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1); // Remove last '&'

            return ResponseEntity.ok(urlBuilder.toString());
        }
        throw new RuntimeException("Order can not payment yet");
        }

    private String generateHMAC(String secretKey, String signData) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSha512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmacSha512.init(keySpec);
        byte[] hmacBytes = hmacSha512.doFinal(signData.getBytes(StandardCharsets.UTF_8));

        StringBuilder result = new StringBuilder();
        for (byte b : hmacBytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<?> updatePayment(@PathVariable Integer orderId){
        Order order = orderRepository.findOrderById(orderId);
        if(order.getOrderStatus()=="AWAITING PAYMENT"){
            order.setOrderStatus("PAID");
            Payment payment = new Payment();
            payment.setPaymentStatus("SUCCESS");
            payment.setPaymentDate(order.getOrderDate());
            payment.setAmount(order.getCartID().getTotalPrice());
            payment.setOrderID(order);
            paymentRepository.save(payment);
            return ResponseEntity.ok(payment);
        }else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment error");
        }
    }


}

