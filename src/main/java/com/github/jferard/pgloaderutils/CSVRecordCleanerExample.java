package com.github.jferard.pgloaderutils;

import java.util.AbstractList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;

public class CSVRecordCleanerExample implements CSVRecordCleaner {

	/* (non-Javadoc)
	 * @see com.github.jferard.pgloaderutils.CSVRecordCleaner#cleanRecord(org.apache.commons.csv.CSVRecord)
	 */
	public List<String> cleanRecord(final CSVRecord record) {
		return new AbstractList<String> () {
	
			@Override
			public String get(int i) {
				if (i == 11 || i == 12 || i == 16)
					return record.get(i).replaceAll(",", ".");
				else
					return record.get(i).replaceAll(",", ".");
			}
	
			@Override
			public int size() {
				return record.size();
			}};
	}

}
