package com.example.myShop.controller;

import com.example.myShop.dto.PayRequestDto;
import com.example.myShop.entity.Payment;
import com.example.myShop.external.kakao.KakaoPayApproveResponse;
import com.example.myShop.external.kakao.KakaoPayReadyResponse;
import com.example.myShop.repository.PaymentRepository;
import com.example.myShop.service.KakaoPayService;
import com.example.myShop.util.slack.SlackBotNotifier;
//import com.example.myShop.util.slack.SlackNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


import java.net.URI;

@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PayController {

    private final KakaoPayService kakaoPayService;
    private final PaymentRepository paymentRepository;
//    private final SlackNotifier slackNotifier;
    private final SlackBotNotifier slackBotNotifier;

//    @GetMapping("/ready")
//    public ResponseEntity<?> pay() {
//        KakaoPayReadyResponse response = kakaoPayService.payReady();
//        return ResponseEntity.status(HttpStatus.FOUND)  // 302 리다이렉트
//                .location(URI.create(response.getNext_redirect_pc_url()))
//                .build();
//    }

    @GetMapping("/ready")
    public ResponseEntity<?> payReady(@RequestParam("itemName") String itemName,
                                      @RequestParam("amount") int amount) {

        KakaoPayReadyResponse response = kakaoPayService.payReady(itemName, amount);
        System.out.println("🔥 카카오페이 ready 응답: " + response);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(response.getNext_redirect_pc_url()))
                .build();
    }


    @GetMapping("/success")
    public ResponseEntity<String> paySuccess(@RequestParam("pg_token") String pgToken) {
        System.out.println("✅ [paySuccess] 컨트롤러 진입 성공: " + pgToken);

        // DB에서 최근 결제 조회
        Payment latestPayment = paymentRepository.findTopByOrderByCreatedAtDesc()
                .orElseThrow(() -> new RuntimeException("❌ 결제 내역이 존재하지 않습니다."));

        // 결제 승인 요청
        KakaoPayApproveResponse approveResponse =
                kakaoPayService.approvePayment(latestPayment.getTid(), pgToken);
        System.out.println("✅ [paySuccess] 승인 응답: " + approveResponse);

        // 승인 시간 저장
        latestPayment.setApprovedAt(LocalDateTime.now());
        paymentRepository.save(latestPayment);
        System.out.println("✅ [paySuccess] 승인 시간 DB 저장 완료: " + latestPayment.getApprovedAt());

        // 슬랙 알림 전송
        System.out.println("🔥 Slack 전송 시도됨!");
        slackBotNotifier.sendMessage("✅ 결제 완료됨: " +
                latestPayment.getItemName() + " - " +
                latestPayment.getTotalAmount() + "원 결제 완료");

        return ResponseEntity.ok("✅ 결제 성공 및 알림 완료");
    }

    @PostMapping("/ready")
    public ResponseEntity<?> payReady(@RequestBody PayRequestDto requestDto) {
        // 🔥 여기에 로그 추가!
        System.out.println("🔥 [PayController] itemNames = " + requestDto.getItemNames());
        System.out.println("🔥 [PayController] amount = " + requestDto.getAmount());

        KakaoPayReadyResponse response = kakaoPayService.payReady(
                requestDto.getItemNames().get(0), requestDto.getAmount()
        );

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(response.getNext_redirect_pc_url()))
                .build();
    }



}

