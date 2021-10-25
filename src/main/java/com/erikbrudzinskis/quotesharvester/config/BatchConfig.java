package com.erikbrudzinskis.quotesharvester.config;

import com.erikbrudzinskis.quotesharvester.batch.*;
import com.erikbrudzinskis.quotesharvester.dto.QuoteDTO;
import com.erikbrudzinskis.quotesharvester.entity.Quote;
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

    @Autowired
    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, QuoteRepository quoteRepository, JobLauncher jobLauncher) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.quoteRepository = quoteRepository;
        this.jobLauncher = jobLauncher;
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
                .flow(step()).end().build();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("step")
                .<QuoteDTO, Quote> chunk(8)
                .reader(new Reader())
                .processor(new Processor())
                .writer(new Writer(quoteRepository))
                .build();
    }
}
