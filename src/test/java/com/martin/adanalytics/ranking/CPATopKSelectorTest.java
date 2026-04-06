package com.martin.adanalytics.ranking;

import com.martin.adanalytics.model.CampaignStats;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CPATopKSelectorTest {

    @Test
    void select_shouldReturnLowestCpaInAscendingOrder_andSkipNullCpa() {
        CPATopKSelector selector = new CPATopKSelector(2);
        List<CampaignStats> input = List.of(
                stats("c1", 100, 10, 40.0, 2), // 20.0
                stats("c2", 100, 10, 10.0, 2), // 5.0
                stats("c3", 100, 10, 24.0, 3), // 8.0
                stats("c4", 100, 10, 50.0, 0)  // null
        );

        List<CampaignStats> result = selector.select(input);

        assertEquals(2, result.size());
        assertEquals("c2", result.get(0).getCampaignId());
        assertEquals("c3", result.get(1).getCampaignId());
    }

    private static CampaignStats stats(String id, long impressions, long clicks, double spend, long conversions) {
        CampaignStats s = new CampaignStats(id);
        s.accumulate(impressions, clicks, spend, conversions);
        return s;
    }
}
