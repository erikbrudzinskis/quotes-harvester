package com.erikbrudzinskis.quotesharvester.harvester;

import com.erikbrudzinskis.quotesharvester.config.AppConfig;
import com.erikbrudzinskis.quotesharvester.dto.QuoteDTO;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Data
public abstract class Harvester implements Runnable {

    @Autowired
    private AppConfig appConfig;

    private Class<? extends StreamingExchange> exchangeClass;

    private Map<String, QuoteDTO> quotes = new ConcurrentHashMap<>();
    private Map<String, QuoteDTO> dependsQuotes = new ConcurrentHashMap<>();

    private static final String STREAMING_EXCHANGE = "StreamingExchange";

    protected Harvester(Class<? extends StreamingExchange> exchangeClass) {
        this.exchangeClass = exchangeClass;
    }

    @Override
    public void run() {

        // Create streaming exchange with a bean, e.g. BinanceStreamingExchange
        StreamingExchange exchange = StreamingExchangeFactory.INSTANCE.createExchange(exchangeClass);

        // Get instruments
        List<CurrencyPair> currencyPairs = getInstruments();
        List<CurrencyPair> dependsPairs = getDepends();

        // Create subscription for every instrument from application.yml
        ProductSubscription.ProductSubscriptionBuilder subscriptionBuilder = ProductSubscription.create();
        for (CurrencyPair cp : currencyPairs) {
            subscriptionBuilder.addTicker(cp);
        }
        for (CurrencyPair dp : dependsPairs) {
            subscriptionBuilder.addTicker(dp);
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
                                exchangeClass.getSimpleName().replace(STREAMING_EXCHANGE, ""),
                                ticker.getInstrument().toString(),
                                LocalDateTime.now()));
            });
        }

        // Get data for depends
        for (CurrencyPair dp : dependsPairs) {
            streamingMarketDataService.getTicker(dp).subscribe(ticker -> {
                log.info("Depends ticker: {}", ticker);

                dependsQuotes.put(exchangeClass.getSimpleName() + ' ' + ticker.getInstrument().toString(),
                        new QuoteDTO(
                                ticker.getBid(),
                                ticker.getAsk(),
                                exchangeClass.getSimpleName().replace(STREAMING_EXCHANGE, ""),
                                ticker.getInstrument().toString(),
                                LocalDateTime.now()));
            });
        }
    }

    @Scheduled(fixedRate = 10000)
    public void calculateDepends() {
        getQuotesFromDepends();
    }


    // Gets all instruments from application.yml apart from synthetic
    private List<CurrencyPair> getInstruments() {
        List<Map<String, String>> instruments = appConfig.getInstruments();
        List<CurrencyPair> instrumentList = new ArrayList<>();
        String instrumentName = null;
        boolean depends = false;

        for (Map<String, String> instrument : instruments) {
            for (Map.Entry<String, String> entry : instrument.entrySet()) {

                // Assign the name of the instrument
                if (entry.getKey().equalsIgnoreCase("instrument")) {
                    instrumentName = entry.getValue();
                }

                // Check whether instrument is based on depends
                if (entry.getKey().contains("depends")) {
                    depends = true;
                }

            }

            if (!depends && instrumentName != null) {
                instrumentList.add(new CurrencyPair(instrumentName));
            }

        }

        return instrumentList;
    }

    // Gets all depends
    private List<CurrencyPair> getDepends() {
        List<Map<String, String>> instruments = appConfig.getInstruments();
        List<CurrencyPair> depends = new ArrayList<>();

        for (Map<String, String> instrument : instruments) {
            for (Map.Entry<String, String> entry : instrument.entrySet()) {
                if (entry.getKey().contains("depends")) {
                    depends.add(new CurrencyPair(entry.getValue()));
                }
            }
        }

        return depends;
    }

    // Gets the synthetic quotes from available depends
    private void getQuotesFromDepends() {
        String instrumentName = null;
        QuoteDTO dependQuote1 = null;
        QuoteDTO dependQuote2 = null;

        List<Map<String, String>> instruments = appConfig.getInstruments();

        for (Map<String, String> instrument : instruments) {
            for (Map.Entry<String, String> entry : instrument.entrySet()) {

                if (entry.getKey().contains("name")) {
                    instrumentName = entry.getValue();
                } else if (entry.getKey().contains("depends.0")) {
                    dependQuote1 = getBidAndAskForDepend(entry);
                } else if (entry.getKey().contains("depends.1")) {
                    dependQuote2 = getBidAndAskForDepend(entry);
                }

                if (dependQuote1 != null && dependQuote2 != null) {
                    quotes.put(exchangeClass.getSimpleName() + ' ' + instrumentName, new QuoteDTO(
                            dependQuote1.getBid().divide(dependQuote2.getBid(), 8, RoundingMode.HALF_UP),
                            dependQuote1.getAsk().divide(dependQuote2.getAsk(), 8, RoundingMode.HALF_UP),
                            exchangeClass.getSimpleName().replace(STREAMING_EXCHANGE, ""),
                            instrumentName,
                            LocalDateTime.now()
                    ));
                    dependQuote1 = null;
                    dependQuote2 = null;
                }
            }
        }
    }

    public Map<String, QuoteDTO> getQuotes() {
        return quotes;
    }

    private QuoteDTO getBidAndAskForDepend(Map.Entry<String, String> entry) {
        BigDecimal bid = null;
        BigDecimal ask = null;

        String currency = entry.getValue();

        for (Map.Entry<String, QuoteDTO> entry1 : dependsQuotes.entrySet()) {
            if (entry1.getKey().equalsIgnoreCase(exchangeClass.getSimpleName() + ' ' + currency)) {
                bid = entry1.getValue().getBid();
                ask = entry1.getValue().getAsk();
            }
        }

        if (bid != null && ask != null) {
            return new QuoteDTO(bid, ask, exchangeClass.getSimpleName().replace(STREAMING_EXCHANGE, ""), "N/A", LocalDateTime.now());
        }

        return null;
    }
}
