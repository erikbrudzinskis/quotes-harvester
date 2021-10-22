package com.erikbrudzinskis.quotesharvester.config;

import com.erikbrudzinskis.quotesharvester.batch.*;
import com.erikbrudzinskis.quotesharvester.dto.QuoteDTO;
import com.erikbrudzinskis.quotesharvester.entity.Quote;
import com.erikbrudzinskis.quotesharvester.harvester.Harvester;
import com.erikbrudzinskis.quotesharvester.repository.QuoteRepository;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;


@Configuration
@EnableBatchProcessing
@EnableScheduling
public class BatchConfig {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private Config config;

    @Autowired
    JobLauncher jobLauncher;

    @Scheduled(fixedRateString = "${flush_period_s}", initialDelay = 20, timeUnit = TimeUnit.SECONDS)
    public void launchJob() throws Exception {
        jobLauncher
                .run(processJob(),
                        new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters());
    }

    @Bean
    public Job processJob() {
        return jobBuilderFactory.get("processJob")
                .incrementer(new RunIdIncrementer())
                .flow(step()).end().build();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("step")
                .<QuoteDTO, Quote> chunk(4)
                .reader(new Reader())
                .processor(new Processor())
                .writer(new Writer(quoteRepository))
                .build();
    }
}
