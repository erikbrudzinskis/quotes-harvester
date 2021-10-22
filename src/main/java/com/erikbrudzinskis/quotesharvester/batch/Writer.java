package com.erikbrudzinskis.quotesharvester.batch;

import com.erikbrudzinskis.quotesharvester.entity.Quote;
import com.erikbrudzinskis.quotesharvester.harvester.Harvester;
import com.erikbrudzinskis.quotesharvester.repository.QuoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class Writer implements ItemWriter<Quote> {
    private final QuoteRepository quoteRepository;

    @Autowired
    public Writer(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    @Override
    public void write(List<? extends Quote> items) {
        for (Quote item : items) {
            quoteRepository.save(item);
        }
        Harvester.getQuotes().clear();
    }
}
