package com.erikbrudzinskis.quotesharvester.batch;

import com.erikbrudzinskis.quotesharvester.entity.Quote;
import com.erikbrudzinskis.quotesharvester.harvester.Harvester;
import com.erikbrudzinskis.quotesharvester.repository.QuoteRepository;
import org.springframework.batch.item.ItemWriter;


import java.util.List;

public class Writer implements ItemWriter<Quote> {
    private final Harvester harvester;
    private final QuoteRepository quoteRepository;

    public Writer(QuoteRepository quoteRepository, Harvester harvester) {
        this.quoteRepository = quoteRepository;
        this.harvester = harvester;
    }

    @Override
    public void write(List<? extends Quote> items) {

        for (Quote item : items) {
            quoteRepository.save(item);
        }

        harvester.getQuotes().clear();
    }
}
