package com.github.jferard.pgloaderutils;

import org.apache.commons.csv.CSVRecord;

import java.util.List;
import java.util.Set;

public interface SelectedColsFactory {
    /**
     * Take the header rows (0..n) and return a set of selected cols
     * @param firstRows the header rows
     * @return a set of col indices.
     */
    Set<Integer> create(List<CSVRecord> firstRows);
}
