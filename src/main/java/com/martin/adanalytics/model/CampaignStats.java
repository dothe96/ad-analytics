package com.martin.adanalytics.model;

public class CampaignStats {
    private final String campaignId;

    private long impressions;
    private long clicks;
    private double spend;
    private long conversions;

    public CampaignStats(String campaignId) {
        this.campaignId = campaignId;
    }

    public void accumulate(long impressions, long clicks, double spend, long conversions) {
        this.impressions += impressions;
        this.clicks += clicks;
        this.spend += spend;
        this.conversions += conversions;
    }

    public double getCTR() {
        if (impressions == 0) return 0.0;
        return (double) clicks / impressions;
    }

    public Double getCPA() {
        if (conversions == 0) return null;
        return spend / conversions;
    }

    public String getCampaignId() { return campaignId; }
    public long getImpressions() { return impressions; }
    public long getClicks() { return clicks; }
    public double getSpend() { return spend; }
    public long getConversions() { return conversions; }
}
