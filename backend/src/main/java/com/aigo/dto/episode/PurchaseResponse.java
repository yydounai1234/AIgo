package com.aigo.dto.episode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {
    private String episodeId;
    private Integer coinCost;
    private Integer newBalance;
    private LocalDateTime purchasedAt;
}
