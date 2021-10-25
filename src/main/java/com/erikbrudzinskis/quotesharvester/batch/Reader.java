package com.erikbrudzinskis.quotesharvester.batch;

import com.erikbrudzinskis.quotesharvester.dto.QuoteDTO;
import com.erikbrudzinskis.quotesharvester.harvester.Harvester;
import org.springframework.batch.item.ItemReader;


import java.util.Map;

public class Reader implements ItemReader<QuoteDTO> {
    private final Harvester harvester;

    public Reader(Harvester harvester) {
        this.harvester = harvester;
    }

    private Map<String, QuoteDTO> quotesMap = null;

    @Override
    public QuoteDTO read() {
        if (quotesMap == null) {
            quotesMap = harvester.getQuotes();
        }

        if (quotesMap.isEmpty()) {
            return null;
        }

        Map.Entry<String, QuoteDTO> entry = quotesMap.entrySet().iterator().next();
        String key = entry.getKey();
        QuoteDTO quoteDTO = entry.getValue();
        quotesMap.remove(key);

        return quoteDTO;
    }
}
