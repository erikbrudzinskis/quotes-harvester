package com.erikbrudzinskis.quotesharvester.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class QuoteDTO {
    private BigDecimal bid;
    private BigDecimal ask;
    private String exchange;
    private String instrumentName;
    private LocalDateTime time;
}
