package com.erikbrudzinskis.quotesharvester.harvester;

import info.bitrich.xchangestream.core.StreamingExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class BinanceHarvester extends Harvester{
    public BinanceHarvester(@Qualifier("binanceExchange") Class<? extends StreamingExchange> exchangeClass) {
        super(exchangeClass);
    }
}
