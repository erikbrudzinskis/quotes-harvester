package com.erikbrudzinskis.quotesharvester.batch;

import com.erikbrudzinskis.quotesharvester.dto.QuoteDTO;
import com.erikbrudzinskis.quotesharvester.entity.Quote;
import org.springframework.batch.item.ItemProcessor;

public class Processor implements ItemProcessor<QuoteDTO, Quote> {
    @Override
    public Quote process(QuoteDTO item) {
        return new Quote(
                item.getBid(),
                item.getAsk(),
                item.getExchange(),
                item.getInstrumentName(),
                item.getTime());
    }
}
