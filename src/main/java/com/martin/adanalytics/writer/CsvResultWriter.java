package com.martin.adanalytics.writer;

import com.martin.adanalytics.model.CampaignStats;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvResultWriter {

    public void writeCTR(String path, List<CampaignStats> list) {
        write(path, list);
    }

    public void writeCPA(String path, List<CampaignStats> list) {
        write(path, list);
    }

    private void write(String path, List<CampaignStats> data) {
        try (FileWriter writer = new FileWriter(path)) {

            writer.write("campaign_id,total_impressions,total_clicks,total_spend,total_conversions,CTR,CPA\n");

            for (CampaignStats cs : data) {
                writer.write(String.format(
                        "%s,%d,%d,%.2f,%d,%.4f,%s\n",
                        cs.getCampaignId(),
                        cs.getImpressions(),
                        cs.getClicks(),
                        cs.getSpend(),
                        cs.getConversions(),
                        cs.getCTR(),
                        cs.getCPA() == null ? "null" : String.format("%.2f", cs.getCPA())
                ));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
