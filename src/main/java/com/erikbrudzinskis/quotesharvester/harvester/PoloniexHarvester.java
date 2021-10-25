package com.erikbrudzinskis.quotesharvester.harvester;

import info.bitrich.xchangestream.core.StreamingExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PoloniexHarvester extends Harvester {

    public PoloniexHarvester(@Qualifier("poloniexExchange") Class<? extends StreamingExchange> exchangeClass) {
        super(exchangeClass);
    }
}
