package com.example.myShop.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PayRequestDto {
    private List<String> itemNames;
    private int amount;
}