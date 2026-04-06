package com.martin.adanalytics.parser;

import com.martin.adanalytics.aggregator.CampaignAggregator;
import com.martin.adanalytics.model.CampaignStats;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvRowParserTest {

    @Test
    void parseAndAggregate_shouldParseValidRowAndAggregate() {
        CsvRowParser parser = new CsvRowParser();
        CampaignAggregator aggregator = new CampaignAggregator();

        boolean ok = parser.parseAndAggregate("cmp-1,2026-04-06,100,7,12.50,3", aggregator);

        assertTrue(ok);
        CampaignStats stats = aggregator.getAll().iterator().next();
        assertEquals("cmp-1", stats.getCampaignId());
        assertEquals(100, stats.getImpressions());
        assertEquals(7, stats.getClicks());
        assertEquals(12.5, stats.getSpend(), 1e-9);
        assertEquals(3, stats.getConversions());
    }

    @Test
    void parseAndAggregate_shouldSkipHeader() {
        CsvRowParser parser = new CsvRowParser();
        CampaignAggregator aggregator = new CampaignAggregator();

        boolean ok = parser.parseAndAggregate("campaign_id,date,impressions,clicks,spend,conversions", aggregator);

        assertFalse(ok);
        assertEquals(0, aggregator.getAll().size());
    }

    @Test
    void parseAndAggregate_shouldRejectMalformedRows() {
        CsvRowParser parser = new CsvRowParser();
        CampaignAggregator aggregator = new CampaignAggregator();

        assertFalse(parser.parseAndAggregate("cmp-1,2026-04-06,abc,7,12.50,3", aggregator));
        assertFalse(parser.parseAndAggregate("cmp-1,2026-04-06,100,7,12.50", aggregator));
        assertFalse(parser.parseAndAggregate("cmp-1,2026-04-06,100,7,12.50,3,extra", aggregator));
        assertEquals(0, aggregator.getAll().size());
    }
}
