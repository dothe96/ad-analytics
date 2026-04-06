package com.martin.adanalytics.cli;

import com.martin.adanalytics.aggregator.CampaignAggregator;
import com.martin.adanalytics.model.CampaignStats;
import com.martin.adanalytics.parser.CsvRowParser;
import com.martin.adanalytics.ranking.CPATopKSelector;
import com.martin.adanalytics.ranking.CTRTopKSelector;
import com.martin.adanalytics.reader.CsvFileReader;
import com.martin.adanalytics.util.BenchmarkReporter;
import com.martin.adanalytics.writer.CsvResultWriter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Command(
        name = "ad-analytics",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "Process advertising CSV data"
)
public class Application implements Runnable {

    @Option(names = "--input", required = true, description = "Input CSV file path")
    private String inputPath;

    @Option(names = "--output", required = true, description = "Output directory")
    private String outputDir;

    @Option(names = "--top-k", defaultValue = "10", description = "Top K campaigns for CTR/CPA ranking (default: ${DEFAULT-VALUE})")
    private int topK;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        long totalStartNanos = System.nanoTime();

        validatePaths();
        BenchmarkReporter benchmark = BenchmarkReporter.start(inputPath);

        System.out.println("Starting processing...");
        System.out.println("Input: " + inputPath);
        System.out.println("Output: " + outputDir);

        CsvFileReader reader = new CsvFileReader();
        CsvRowParser parser = new CsvRowParser();
        CampaignAggregator aggregator = new CampaignAggregator();

        int processed = 0;
        int skipped = 0;
        long processStartNanos = System.nanoTime();

        for (String line : reader.readLines(inputPath)) {
            if (parser.parseAndAggregate(line, aggregator)) {
                processed++;
            } else {
                skipped++;
            }
        }
        long processEndNanos = System.nanoTime();

        System.out.println("Processed rows: " + processed);
        System.out.println("Skipped rows: " + skipped);

        // Ranking
        long rankingStartNanos = System.nanoTime();
        CTRTopKSelector ctrSelector = new CTRTopKSelector(topK);
        CPATopKSelector cpaSelector = new CPATopKSelector(topK);

        List<CampaignStats> topCtr = ctrSelector.select(aggregator.getAll());
        List<CampaignStats> topCpa = cpaSelector.select(aggregator.getAll());
        long rankingEndNanos = System.nanoTime();

        // Write output
        long writerStartNanos = System.nanoTime();
        CsvResultWriter writer = new CsvResultWriter();
        writer.writeCTR(outputDir + "/top10_ctr.csv", topCtr);
        writer.writeCPA(outputDir + "/top10_cpa.csv", topCpa);
        long writerEndNanos = System.nanoTime();

        benchmark.finishAndPrint(
                processed,
                skipped,
                processEndNanos - processStartNanos,
                rankingEndNanos - rankingStartNanos,
                writerEndNanos - writerStartNanos,
                writerEndNanos - totalStartNanos
        );
    }

    private void validatePaths() {
        if (!Files.exists(Path.of(inputPath))) {
            throw new IllegalArgumentException("Input file does not exist: " + inputPath);
        }
        if (topK <= 0) {
            throw new IllegalArgumentException("Invalid value for --top-k: must be a positive integer (> 0).");
        }

        try {
            Files.createDirectories(Path.of(outputDir));
        } catch (Exception e) {
            throw new RuntimeException("Cannot create output directory: " + outputDir, e);
        }
    }
}
