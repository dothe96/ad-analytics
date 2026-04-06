package com.martin.adanalytics.aggregator;

import com.martin.adanalytics.model.CampaignStats;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CampaignAggregator {
    private final Map<String, CampaignStats> map = new HashMap<>();

    public void process(String campaignId, long impressions, long clicks, double spend, long conversions) {
        CampaignStats stats = map.computeIfAbsent(campaignId, CampaignStats::new);
        stats.accumulate(impressions, clicks, spend, conversions);
    }

    public Collection<CampaignStats> getAll() {
        return map.values();
    }
}
