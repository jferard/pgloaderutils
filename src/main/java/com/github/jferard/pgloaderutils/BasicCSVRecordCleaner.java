package com.github.jferard.pgloaderutils;

import java.util.AbstractList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;

public class BasicCSVRecordCleaner implements CSVRecordCleaner {

	/* (non-Javadoc)
	 * @see com.github.jferard.pgloaderutils.CSVRecordCleaner#cleanRecord(org.apache.commons.csv.CSVRecord)
	 */
	public List<String> cleanRecord(final CSVRecord record) {
		return new AbstractList<String>() {

			@Override
			public String get(int i) {
				return record.get(i);
			}

			@Override
			public int size() {
				return record.size();
			}
		};
	}
}
