package com.martin.adanalytics.ranking;

import com.martin.adanalytics.model.CampaignStats;

import java.util.*;

public class CTRTopKSelector {
    private final int k;

    public CTRTopKSelector(int k) {
        this.k = k;
    }

    public List<CampaignStats> select(Collection<CampaignStats> input) {

        PriorityQueue<CampaignStats> heap =
                new PriorityQueue<>(Comparator.comparingDouble(CampaignStats::getCTR));

        for (CampaignStats cs : input) {
            heap.offer(cs);
            if (heap.size() > k) {
                heap.poll();
            }
        }

        List<CampaignStats> result = new ArrayList<>(heap);
        result.sort((a, b) -> Double.compare(b.getCTR(), a.getCTR()));
        return result;
    }
}
