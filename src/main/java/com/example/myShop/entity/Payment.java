package com.example.myShop.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String tid;
    private String partnerOrderId;
    private String partnerUserId;
    private String itemName;
    private int totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;

    @Builder
    public Payment(String tid, String partnerOrderId, String partnerUserId,
                   String itemName, int totalAmount, LocalDateTime createdAt, LocalDateTime approvedAt) {
        this.tid = tid;
        this.partnerOrderId = partnerOrderId;
        this.partnerUserId = partnerUserId;
        this.itemName = itemName;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.approvedAt = approvedAt;
    }
}
