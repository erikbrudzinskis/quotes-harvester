package com.erikbrudzinskis.quotesharvester.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "QUOTES")
@Data
public class Quote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "TIME")
    private LocalDateTime time;

    @Column(name = "BID")
    private BigDecimal bid;

    @Column(name = "ASK")
    private BigDecimal ask;

    @Column(name = "EXCHANGE")
    private String exchange;

    @Column(name = "NAME")
    private String name;

    public Quote(BigDecimal bid, BigDecimal ask, String exchange, String name, LocalDateTime time) {
        this.bid = bid;
        this.ask = ask;
        this.exchange = exchange;
        this.name = name;
        this.time = time;
    }
}
