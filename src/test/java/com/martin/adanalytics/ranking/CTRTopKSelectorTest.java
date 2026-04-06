package com.martin.adanalytics.ranking;

import com.martin.adanalytics.model.CampaignStats;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CTRTopKSelectorTest {

    @Test
    void select_shouldReturnHighestCtrInDescendingOrder() {
        CTRTopKSelector selector = new CTRTopKSelector(2);
        List<CampaignStats> input = List.of(
                stats("c1", 100, 10, 0, 1),  // 0.10
                stats("c2", 100, 30, 0, 1),  // 0.30
                stats("c3", 100, 20, 0, 1)   // 0.20
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
