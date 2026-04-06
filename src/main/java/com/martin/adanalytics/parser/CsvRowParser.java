package com.martin.adanalytics.parser;

import com.martin.adanalytics.aggregator.CampaignAggregator;

public class CsvRowParser {
    private static final String HEADER_PREFIX = "campaign_id";

    public boolean isHeader(String line) {
        if (line == null || line.isEmpty()) {
            return false;
        }
        String normalized = line.charAt(0) == '\uFEFF' ? line.substring(1) : line;
        return normalized.startsWith(HEADER_PREFIX);
    }

    public boolean parseAndAggregate(String line, CampaignAggregator aggregator) {
        if (isHeader(line)) {
            return false;
        }

        int c1 = line.indexOf(',');
        int c2 = nextComma(line, c1 + 1);
        int c3 = nextComma(line, c2 + 1);
        int c4 = nextComma(line, c3 + 1);
        int c5 = nextComma(line, c4 + 1);

        if (!isValidShape(line, c1, c2, c3, c4, c5)) {
            return false;
        }

        try {
            String campaignId = line.substring(0, c1);
            long impressions = parseLong(line, c2 + 1, c3);
            long clicks = parseLong(line, c3 + 1, c4);
            double spend = Double.parseDouble(line.substring(c4 + 1, c5));
            long conversions = parseLong(line, c5 + 1, line.length());

            aggregator.process(campaignId, impressions, clicks, spend, conversions);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static int nextComma(String line, int fromIndex) {
        if (fromIndex <= 0 || fromIndex >= line.length()) {
            return -1;
        }
        return line.indexOf(',', fromIndex);
    }

    private static boolean isValidShape(String line, int c1, int c2, int c3, int c4, int c5) {
        if (c1 <= 0 || c2 <= c1 + 1 || c3 <= c2 + 1 || c4 <= c3 + 1 || c5 <= c4 + 1) {
            return false;
        }
        return line.indexOf(',', c5 + 1) < 0 && c5 < line.length() - 1;
    }

    private static long parseLong(String s, int start, int end) {
        if (start >= end) {
            throw new NumberFormatException("Empty number");
        }
        long value = 0;
        int i = start;
        boolean negative = false;

        char first = s.charAt(i);
        if (first == '-') {
            negative = true;
            i++;
            if (i >= end) {
                throw new NumberFormatException("Sign only");
            }
        }

        for (; i < end; i++) {
            char ch = s.charAt(i);
            int digit = ch - '0';
            if (digit < 0 || digit > 9) {
                throw new NumberFormatException("Invalid digit: " + ch);
            }
            value = value * 10 + digit;
        }
        return negative ? -value : value;
    }
}
