package com.martin.adanalytics.reader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvFileReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void readLines_shouldReturnEmptyIterable_forEmptyFile() throws Exception {
        Path file = tempDir.resolve("empty.csv");
        Files.writeString(file, "", StandardCharsets.UTF_8);

        CsvFileReader reader = new CsvFileReader();
        List<String> lines = new ArrayList<>();
        for (String line : reader.readLines(file.toString())) {
            lines.add(line);
        }

        assertEquals(0, lines.size());
    }
}
