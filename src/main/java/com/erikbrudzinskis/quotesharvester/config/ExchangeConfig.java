package com.erikbrudzinskis.quotesharvester.config;

import info.bitrich.xchangestream.binance.BinanceStreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.poloniex2.PoloniexStreamingExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExchangeConfig {

    @Bean(name = "poloniexExchange")
    public Class<? extends StreamingExchange> getExchangeClassPoloniex() {
        return PoloniexStreamingExchange.class;
    }

    @Bean(name = "binanceExchange")
    public Class<? extends StreamingExchange> getExchangeClassBinance() {
        return BinanceStreamingExchange.class;
    }
}
