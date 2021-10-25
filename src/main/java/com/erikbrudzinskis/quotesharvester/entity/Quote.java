package com.erikbrudzinskis.quotesharvester.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "QUOTES")
@Data
public class Quote {

    private static final int DB_PRECISION = 16;
    private static final int DB_SCALE = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "TIME")
    private LocalDateTime time;

    @Column(name = "BID", scale = DB_SCALE, precision = DB_PRECISION)
    private BigDecimal bid;

    @Column(name = "ASK", scale = DB_SCALE, precision = DB_PRECISION)
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
