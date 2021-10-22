package com.erikbrudzinskis.quotesharvester.harvester;

import com.erikbrudzinskis.quotesharvester.config.Config;
import com.erikbrudzinskis.quotesharvester.dto.QuoteDTO;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@Data
public abstract class Harvester implements Runnable {
    @Autowired
    private Config config;

    private Class<? extends StreamingExchange> exchangeClass;

    private static Map<String, QuoteDTO> quotes = new ConcurrentHashMap<>();

    protected Harvester(Class<? extends StreamingExchange> exchangeClass) {
        this.exchangeClass = exchangeClass;
    }

    @Override
    public void run() {

        // Create streaming exchange with a bean, eg BinanceStreamingExchange
        StreamingExchange exchange = StreamingExchangeFactory.INSTANCE.createExchange(exchangeClass);

        // Get instruments
        List<CurrencyPair> currencyPairs = getInstruments();

        // Create subscription for every instrument from application.yml
        ProductSubscription.ProductSubscriptionBuilder subscriptionBuilder = ProductSubscription.create();
        for (CurrencyPair cp : currencyPairs) {
            subscriptionBuilder.addTicker(cp);
        }
        ProductSubscription subscription = subscriptionBuilder.build();

        // Connect to the exchange with subscription
        exchange.connect(subscription).blockingAwait();

        // Get data for every instrument through tickers
        StreamingMarketDataService streamingMarketDataService = exchange.getStreamingMarketDataService();
        for (CurrencyPair cp : currencyPairs) {
            streamingMarketDataService.getTicker(cp).subscribe(ticker -> {
                log.info("Ticker: {}", ticker);

                // Put tickers into the static hashmap
                quotes.put(exchangeClass.getSimpleName() + ' ' + ticker.getInstrument().toString(),
                        new QuoteDTO(
                                ticker.getBid(),
                                ticker.getAsk(),
                                exchangeClass.getSimpleName().replace("StreamingExchange", ""),
                                ticker.getInstrument().toString(),
                                LocalDateTime.now()));
            });
        }
    }

    // Gets all instruments from application.yml apart from synthetic
    public List<CurrencyPair> getInstruments() {
        List<Map<String, String>> instruments = config.getInstruments();
        List<CurrencyPair> instrumentNames = new ArrayList<>();
        for (Map<String, String> instrument : instruments) {
            for (Map.Entry<String, String> entry : instrument.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("instrument") && !entry.getValue().contains("synth")) {
                    instrumentNames.add(new CurrencyPair(entry.getValue()));
                }
            }
        }
        return instrumentNames;
    }

    public static Map<String, QuoteDTO> getQuotes() {
        return quotes;
    }
}
