package com.erikbrudzinskis.quotesharvester.batch;

import com.erikbrudzinskis.quotesharvester.dto.QuoteDTO;
import com.erikbrudzinskis.quotesharvester.harvester.Harvester;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class Reader implements ItemReader<QuoteDTO> {
    private Map<String, QuoteDTO> quotesMap = null;

    @Override
    public QuoteDTO read() {
        if (quotesMap == null) {
            quotesMap = Harvester.getQuotes();
        }
        if(quotesMap.isEmpty()) {
            return null;
        }
        Map.Entry<String,QuoteDTO> entry = quotesMap.entrySet().iterator().next();
        String key = entry.getKey();
        QuoteDTO quoteDTO = entry.getValue();
        quotesMap.remove(key);
        log.error(quotesMap.toString());
        return quoteDTO;
    }
}
