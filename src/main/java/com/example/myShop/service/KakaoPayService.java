package com.example.myShop.service;

import com.example.myShop.config.KakaoPayProperties;
import com.example.myShop.entity.Payment;
import com.example.myShop.external.kakao.KakaoPayApproveResponse;
import com.example.myShop.external.kakao.KakaoPayReadyResponse;
import com.example.myShop.repository.PaymentRepository;
import com.example.myShop.util.slack.SlackBotNotifier;
import com.example.myShop.util.slack.SlackNotifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class KakaoPayService {

    private final KakaoPayProperties kakaoPayProperties;
    private final PaymentRepository paymentRepository;
    private final SlackBotNotifier slackBotNotifier;
//    private final SlackNotifier slackNotifier;
    private final RestTemplate restTemplate = new RestTemplate();

    public KakaoPayService(KakaoPayProperties kakaoPayProperties, PaymentRepository paymentRepository, SlackBotNotifier slackBotNotifier) {
        this.kakaoPayProperties = kakaoPayProperties;
        this.paymentRepository = paymentRepository;
//        this.slackNotifier = slackNotifier;
        this.slackBotNotifier = slackBotNotifier;
    }

//    // 결제 준비 요청
//    public KakaoPayReadyResponse payReady() {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "KakaoAK " + kakaoPayProperties.getAdminKey());
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.add("cid", kakaoPayProperties.getCid());
//        params.add("partner_order_id", "order123");
//        params.add("partner_user_id", "user456");
//        params.add("item_name", "초코파이");
//        params.add("quantity", "1");
//        params.add("total_amount", "2200");
//        params.add("vat_amount", "200");
//        params.add("tax_free_amount", "0");
//        params.add("approval_url", "http://localhost/pay/success");
//        params.add("cancel_url", "http://localhost/pay/cancel");
//        params.add("fail_url", "http://localhost/pay/fail");
//
//        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
//
//        ResponseEntity<KakaoPayReadyResponse> response = restTemplate.postForEntity(
//                "https://kapi.kakao.com/v1/payment/ready",
//                entity,
//                KakaoPayReadyResponse.class
//        );
//
//        KakaoPayReadyResponse body = response.getBody();
//
//        if (body != null) {
//            Payment payment = Payment.builder()
//                    .tid(body.getTid())
//                    .partnerOrderId("order123")
//                    .partnerUserId("user456")
//                    .itemName("초코파이")
//                    .totalAmount(2200)
//                    .createdAt(LocalDateTime.now())
//                    .approvedAt(LocalDateTime.now())
//                    .build();
//            paymentRepository.save(payment);
//        }
//
//        return body;
//    }

    public KakaoPayReadyResponse payReady(String itemNames, int amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoPayProperties.getAdminKey());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", kakaoPayProperties.getCid());
        params.add("partner_order_id", "order123");
        params.add("partner_user_id", "user456");
        params.add("item_name", String.join(", ", itemNames)); // 상품명들 ,로 이어붙이기
        //params.add("item_name", itemName);
        params.add("quantity", "1");
        params.add("total_amount", String.valueOf(amount));
        params.add("vat_amount", "200");
        params.add("tax_free_amount", "0");
        params.add("approval_url", "http://localhost/pay/success");
        params.add("cancel_url", "http://localhost/pay/cancel");
        params.add("fail_url", "http://localhost/pay/fail");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<KakaoPayReadyResponse> response = restTemplate.postForEntity(
                "https://kapi.kakao.com/v1/payment/ready",
                entity,
                KakaoPayReadyResponse.class
        );

        KakaoPayReadyResponse body = response.getBody();

        if (body != null) {
            Payment payment = Payment.builder()
                    .tid(body.getTid())
                    .partnerOrderId("order123")
                    .partnerUserId("user456")
                    .itemName(itemNames)
                    .totalAmount(amount)
                    .createdAt(LocalDateTime.now())
                    .approvedAt(null) // 결제 전이므로 null
                    .build();
            paymentRepository.save(payment);
        }

        return body;
    }

    // 결제 승인 요청
    public KakaoPayApproveResponse approvePayment(String tid, String pgToken) {
        // 최근 결제 정보 가져오기
        Payment latestPayment = paymentRepository.findTopByOrderByCreatedAtDesc()
                .orElseThrow(() -> new IllegalStateException("결제 정보가 없습니다."));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoPayProperties.getAdminKey());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", kakaoPayProperties.getCid());
        params.add("tid", tid);
        params.add("partner_order_id", latestPayment.getPartnerOrderId());
        params.add("partner_user_id", latestPayment.getPartnerUserId());
        params.add("pg_token", pgToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<KakaoPayApproveResponse> response = restTemplate.postForEntity(
                "https://kapi.kakao.com/v1/payment/approve",
                entity,
                KakaoPayApproveResponse.class
        );

        KakaoPayApproveResponse approveResponse = response.getBody();

        if (approveResponse != null) {
            latestPayment.setApprovedAt(LocalDateTime.now());
            paymentRepository.save(latestPayment);

            // ✅ 슬랙봇을 이용한 메시지 전송
            String message = "🤖 Team1 [주문 완료]\n"
                    + "👤 사용자: " + latestPayment.getPartnerUserId() + "\n"
                    + "📦 상품명: " + latestPayment.getItemName() + "\n"
                    + "💰 금액: " + latestPayment.getTotalAmount() + "원";

            slackBotNotifier.sendMessage(message);
        } else {
            System.out.println("approveResponse가 null임!");
        }

//            // 슬랙 웹훅을 이용한 알림 전송
//            System.out.println("슬랙 메시지 전송 시작");
//            slackNotifier.sendMessage("Team1 [주문 완료] 사용자: " + latestPayment.getPartnerUserId()
//                    + ", 상품명: " + latestPayment.getItemName()
//                    + ", 금액: " + latestPayment.getTotalAmount() + "원");
//            System.out.println("슬랙 메시지 전송 완료");
//        } else {
//            System.out.println("approveResponse가 null임!");
//        }

        return approveResponse;

    }
}

