package com.martin.adanalytics.util;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class BenchmarkReporter {
    private final long inputBytes;
    private final MemorySampler memorySampler;
    private final BenchmarkSnapshot gcBefore;

    private BenchmarkReporter(long inputBytes, MemorySampler memorySampler, BenchmarkSnapshot gcBefore) {
        this.inputBytes = inputBytes;
        this.memorySampler = memorySampler;
        this.gcBefore = gcBefore;
    }

    public static BenchmarkReporter start(String inputPath) {
        MemorySampler memorySampler = new MemorySampler();
        memorySampler.start();
        return new BenchmarkReporter(getInputSizeBytes(inputPath), memorySampler, BenchmarkSnapshot.captureGc());
    }

    public void finishAndPrint(int processed,
                               int skipped,
                               long processNanos,
                               long rankingNanos,
                               long writerNanos,
                               long totalNanos) {
        memorySampler.stop();
        BenchmarkSnapshot gcAfter = BenchmarkSnapshot.captureGc();
        long gcCountDelta = gcAfter.gcCount - gcBefore.gcCount;
        long gcTimeMsDelta = gcAfter.gcTimeMs - gcBefore.gcTimeMs;

        int totalRows = processed + skipped;
        double processSeconds = processNanos / 1_000_000_000.0;
        double rowsPerSecond = processSeconds > 0 ? processed / processSeconds : 0.0;
        double nanosPerRow = processed > 0 ? (double) processNanos / processed : 0.0;
        double inputMb = inputBytes >= 0 ? inputBytes / (1024.0 * 1024.0) : -1.0;
        double mbPerSecond = (inputMb >= 0 && processSeconds > 0) ? inputMb / processSeconds : -1.0;
        double skipPct = totalRows > 0 ? (skipped * 100.0) / totalRows : 0.0;

        System.out.println("---- Benchmark ----");
        System.out.println("Process time: " + formatMillis(processNanos));
        System.out.println("Ranking time: " + formatMillis(rankingNanos));
        System.out.println("Writer time: " + formatMillis(writerNanos));
        System.out.println("Total time: " + formatMillis(totalNanos));
        System.out.println("Average throughput (rows/s): " + formatDouble(rowsPerSecond));
        if (mbPerSecond >= 0) {
            System.out.println("Input throughput (MB/s): " + formatDouble(mbPerSecond));
        }
        System.out.println("Avg row cost (ns/row): " + formatDouble(nanosPerRow));
        System.out.println("Skip ratio: " + formatDouble(skipPct) + "%");
        System.out.println("Peak heap used: " + formatBytes(memorySampler.getPeakHeapBytes()));
        System.out.println("Peak total memory (heap+non-heap): " + formatBytes(memorySampler.getPeakTotalBytes()));
        System.out.println("Memory use: " + formatBytes(currentUsedMemoryBytes()));
        System.out.println("GC collections (delta): " + gcCountDelta);
        System.out.println("GC time (delta): " + gcTimeMsDelta + " ms");
        System.out.println("Done in " + formatMillis(totalNanos));
    }

    private static long getInputSizeBytes(String path) {
        try {
            return Files.size(Path.of(path));
        } catch (Exception e) {
            return -1L;
        }
    }

    private static String formatMillis(long nanos) {
        return formatDouble(nanos / 1_000_000.0) + " ms";
    }

    private static String formatDouble(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String formatBytes(long bytes) {
        if (bytes < 0) {
            return "n/a";
        }
        double kb = bytes / 1024.0;
        double mb = kb / 1024.0;
        if (mb >= 1.0) {
            return formatDouble(mb) + " MB";
        }
        return formatDouble(kb) + " KB";
    }

    private static long currentUsedMemoryBytes() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static final class BenchmarkSnapshot {
        private final long gcCount;
        private final long gcTimeMs;

        private BenchmarkSnapshot(long gcCount, long gcTimeMs) {
            this.gcCount = gcCount;
            this.gcTimeMs = gcTimeMs;
        }

        private static BenchmarkSnapshot captureGc() {
            long totalGcCount = 0L;
            long totalGcTimeMs = 0L;
            for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
                long count = gcBean.getCollectionCount();
                long time = gcBean.getCollectionTime();
                if (count >= 0) {
                    totalGcCount += count;
                }
                if (time >= 0) {
                    totalGcTimeMs += time;
                }
            }
            return new BenchmarkSnapshot(totalGcCount, totalGcTimeMs);
        }
    }

    private static final class MemorySampler {
        private static final long SAMPLE_INTERVAL_MS = 20L;

        private final MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();
        private final Runtime runtime = Runtime.getRuntime();
        private volatile boolean running;
        private volatile long peakHeapBytes;
        private volatile long peakTotalBytes;
        private Thread thread;

        private void start() {
            running = true;
            thread = new Thread(this::runLoop, "memory-sampler");
            thread.setDaemon(true);
            thread.start();
        }

        private void stop() {
            running = false;
            if (thread == null) {
                return;
            }
            try {
                thread.join(2000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            sampleOnce();
        }

        private void runLoop() {
            while (running) {
                sampleOnce();
                try {
                    Thread.sleep(SAMPLE_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        private void sampleOnce() {
            long heapUsed = runtime.totalMemory() - runtime.freeMemory();
            long nonHeapUsed = memoryMxBean.getNonHeapMemoryUsage().getUsed();
            long totalUsed = heapUsed + Math.max(nonHeapUsed, 0L);
            if (heapUsed > peakHeapBytes) {
                peakHeapBytes = heapUsed;
            }
            if (totalUsed > peakTotalBytes) {
                peakTotalBytes = totalUsed;
            }
        }

        private long getPeakHeapBytes() {
            return peakHeapBytes;
        }

        private long getPeakTotalBytes() {
            return peakTotalBytes;
        }
    }
}
