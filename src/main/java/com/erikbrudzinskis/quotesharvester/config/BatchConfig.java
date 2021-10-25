package com.erikbrudzinskis.quotesharvester.config;

import com.erikbrudzinskis.quotesharvester.batch.*;
import com.erikbrudzinskis.quotesharvester.dto.QuoteDTO;
import com.erikbrudzinskis.quotesharvester.entity.Quote;
import com.erikbrudzinskis.quotesharvester.harvester.BinanceHarvester;
import com.erikbrudzinskis.quotesharvester.harvester.PoloniexHarvester;
import com.erikbrudzinskis.quotesharvester.repository.QuoteRepository;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;


@Configuration
@EnableScheduling
public class BatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final QuoteRepository quoteRepository;
    private final JobLauncher jobLauncher;
    private final PoloniexHarvester poloniexHarvester;
    private final BinanceHarvester binanceHarvester;

    @Autowired
    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, QuoteRepository quoteRepository, JobLauncher jobLauncher, PoloniexHarvester poloniexHarvester, BinanceHarvester binanceHarvester) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.quoteRepository = quoteRepository;
        this.jobLauncher = jobLauncher;
        this.poloniexHarvester = poloniexHarvester;
        this.binanceHarvester = binanceHarvester;
    }

    @Scheduled(fixedRateString = "${flush_period_s}", initialDelay = 20, timeUnit = TimeUnit.SECONDS)
    public void launchJob() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        jobLauncher
                .run(processJob(),
                        new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters());
    }

    @Bean
    public Job processJob() {
        return jobBuilderFactory.get("processJob")
                .incrementer(new RunIdIncrementer())
                .flow(step1()).next(step2()).end().build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step")
                .<QuoteDTO, Quote> chunk(4)
                .reader(new Reader(binanceHarvester))
                .processor(new Processor())
                .writer(new Writer(quoteRepository, binanceHarvester))
                .build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step")
                .<QuoteDTO, Quote> chunk(4)
                .reader(new Reader(poloniexHarvester))
                .processor(new Processor())
                .writer(new Writer(quoteRepository, poloniexHarvester))
                .build();
    }
}
