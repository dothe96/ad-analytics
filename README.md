# ad-analytics

CLI tool (Java 21) to process large advertising CSV files, aggregate by `campaign_id`, and export top campaigns by CTR/CPA.

## Setup Instructions

### Option 1: Local (without Docker)
- JDK 21
- No separate Gradle installation required (the repo includes `./gradlew`)

```bash
./gradlew build
```

### Run locally
```bash
./gradlew run --args="--input /path/ad_data.csv --output /path/out --top-k 10"
```

### Option 2: Docker
- Docker engine

```bash
docker build -t ad-analytics:latest .
```

### Run with Docker
```bash
docker run --rm \                                                                                                                                                               7s
  -v /path/to/csv:/data/ad_data.csv \
  -v /path/to/out/dir:/data/out \
  ad-analytics:latest \
  --input /data/ad_data.csv --output /data/out --top-k 10
```

Example
```bash
docker run --rm \                                                                                                                                                               7s
  -v ~/Downloads/ad_data.csv:/data/ad_data.csv \
  -v ~/Downloads/out:/data/out \
  ad-analytics:latest \
  --input /data/ad_data.csv --output /data/out --top-k 10
```

### CLI options
- `--input` (required): input CSV file path
- `--output` (required): output directory path (created automatically if missing)
- `--top-k` (optional, default `10`): top K campaigns for both CTR/CPA ranking

`--top-k` validation rules:
- Must be a positive integer (`> 0`)
- If omitted, default is `10`
- If invalid (non-integer or `<= 0`), the program exits with an English validation error

## Processing Flow (How the App Works)

1. `CsvFileReader` streams the input file line-by-line using `BufferedReader` (no full file load into memory).
2. `CsvRowParser` parses each row with a manual comma-index parser (`indexOf`) and validates numeric fields.
3. `CampaignAggregator` accumulates totals per `campaign_id` in a `HashMap` (`impressions`, `clicks`, `spend`, `conversions`).
4. `CTRTopKSelector` and `CPATopKSelector` pick top K campaigns from aggregated results.
5. `CsvResultWriter` writes two output files (`top10_ctr.csv` and `top10_cpa.csv`).
6. `BenchmarkReporter` prints runtime metrics (timing, throughput, memory, GC).

## Libraries Used

- `info.picocli:picocli:4.7.5` (CLI parsing)
- `org.junit.jupiter:junit-jupiter` (unit tests, JUnit 5 via BOM `5.10.2`)

## Output

The app writes 2 files to the `--output` directory:
- `top10_ctr.csv`
- `top10_cpa.csv`

> **Notice:** The generated output files `top10_cpa.csv` and `top10_str.csv` are located in the `output` folder.

Columns:
```text
campaign_id,total_impressions,total_clicks,total_spend,total_conversions,CTR,CPA
```

## Processing Time for the 1GB File

Benchmark captured from this project (file `/home/martin/Downloads/ad_data.csv`, size `1,043,304,870` bytes, ~1GB):

- Processed rows: `26,843,544`
- Skipped rows: `1`
- Process time: `7,560.01 ms`
- Ranking time: `11.21 ms`
- Writer time: `14.36 ms`
- Total time: `7,623.45 ms`
- Average throughput: `3,550,727.39 rows/s`

## Peak Memory Usage (Measured)

From the app benchmark output:

- Peak heap used: `131.03 MB`
- Peak total memory (heap + non-heap): `137.23 MB`

## Trade-offs and Possible Improvements

This implementation is production-oriented for the interview scope, but not fully optimized in every dimension.

Current trade-offs:
- The parser is fast for simple CSV shape, but does not handle advanced CSV features (quoted commas, escaped quotes).
- The app is single-process/single-thread in the main pipeline, which keeps behavior simple and deterministic.
- Ranking is done after aggregation, which is correct and simple, but still executes a separate pass over campaign stats.

Potential improvements (senior-level next steps):
- Replace custom parsing with a robust high-performance CSV parser if full CSV compliance is required.
- Add optional parallel ingestion/aggregation (partitioned maps + merge) for higher throughput on multi-core systems.
- Use primitive-focused structures or off-heap strategies when campaign cardinality becomes very large.
- Add integration tests for full pipeline I/O and property-based tests for parser edge cases.
- Add structured metrics export (e.g., Prometheus/OpenTelemetry) in addition to console benchmark logs.

## Run Tests

```bash
./gradlew test
```
