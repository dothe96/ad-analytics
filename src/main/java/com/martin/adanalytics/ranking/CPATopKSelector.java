package com.martin.adanalytics.ranking;

import com.martin.adanalytics.model.CampaignStats;

import java.util.*;

public class CPATopKSelector {
    private final int k;

    public CPATopKSelector(int k) {
        this.k = k;
    }

    public List<CampaignStats> select(Collection<CampaignStats> input) {

        PriorityQueue<CampaignStats> heap =
                new PriorityQueue<>((a, b) -> Double.compare(b.getCPA(), a.getCPA()));

        for (CampaignStats cs : input) {
            Double cpa = cs.getCPA();
            if (cpa == null) continue;

            if (heap.size() < k) {
                heap.offer(cs);
            } else if (cpa < heap.peek().getCPA()) {
                heap.poll();
                heap.offer(cs);
            }

            if (heap.size() > k) {
                heap.poll();
            }
        }

        List<CampaignStats> result = new ArrayList<>(heap);
        result.sort(Comparator.comparingDouble(CampaignStats::getCPA));
        return result;
    }
}
