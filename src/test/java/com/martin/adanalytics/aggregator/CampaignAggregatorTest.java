package com.martin.adanalytics.aggregator;

import com.martin.adanalytics.model.CampaignStats;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CampaignAggregatorTest {

    @Test
    void process_shouldAccumulateRowsByCampaignId() {
        CampaignAggregator aggregator = new CampaignAggregator();

        aggregator.process("c1", 100, 10, 5.0, 2);
        aggregator.process("c1", 200, 20, 7.5, 3);
        aggregator.process("c2", 50, 5, 3.0, 1);

        assertEquals(2, aggregator.getAll().size());

        CampaignStats c1 = findById(aggregator, "c1");
        assertNotNull(c1);
        assertEquals(300, c1.getImpressions());
        assertEquals(30, c1.getClicks());
        assertEquals(12.5, c1.getSpend(), 1e-9);
        assertEquals(5, c1.getConversions());
    }

    private static CampaignStats findById(CampaignAggregator aggregator, String campaignId) {
        for (CampaignStats stats : aggregator.getAll()) {
            if (campaignId.equals(stats.getCampaignId())) {
                return stats;
            }
        }
        return null;
    }
}
