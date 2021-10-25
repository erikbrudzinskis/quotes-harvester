package com.erikbrudzinskis.quotesharvester.listener;

import com.erikbrudzinskis.quotesharvester.harvester.BinanceHarvester;
import com.erikbrudzinskis.quotesharvester.harvester.PoloniexHarvester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class QuotesHarvesterListener implements ApplicationListener<ApplicationReadyEvent> {

    private final BinanceHarvester binanceHarvester;
    private final PoloniexHarvester poloniexHarvester;

    @Autowired
    public QuotesHarvesterListener(BinanceHarvester binanceHarvester, PoloniexHarvester poloniexHarvester) {
        this.binanceHarvester = binanceHarvester;
        this.poloniexHarvester = poloniexHarvester;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Thread thread = new Thread(binanceHarvester);
        thread.start();
        Thread thread2 = new Thread(poloniexHarvester);
        thread2.start();
    }
}
