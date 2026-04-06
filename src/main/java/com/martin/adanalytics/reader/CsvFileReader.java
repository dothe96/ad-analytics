package com.martin.adanalytics.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class CsvFileReader {
    public Iterable<String> readLines(String path) {
        return () -> new Iterator<String>() {

            final BufferedReader reader;
            String nextLine;

            {
                try {
                    reader = new BufferedReader(new FileReader(path));
                    nextLine = reader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public boolean hasNext() {
                return nextLine != null;
            }

            @Override
            public String next() {
                String current = nextLine;
                try {
                    nextLine = reader.readLine();
                    if (nextLine == null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return current;
            }
        };
    }
}
