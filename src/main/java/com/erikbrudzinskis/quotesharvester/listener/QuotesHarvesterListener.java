package com.erikbrudzinskis.quotesharvester.listener;

import com.erikbrudzinskis.quotesharvester.harvester.BinanceHarvester;
import com.erikbrudzinskis.quotesharvester.harvester.PoloniexHarvester;
import lombok.SneakyThrows;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@Order(0)
public class QuotesHarvesterListener implements ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private BinanceHarvester binanceHarvester;

    @Autowired
    private PoloniexHarvester poloniexHarvester;

    @SneakyThrows
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Thread thread = new Thread(binanceHarvester);
        thread.start();
        Thread thread2 = new Thread(poloniexHarvester);
        thread2.start();
    }
}
