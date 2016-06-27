package com.github.jferard.pgloaderutils;

import java.util.AbstractList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;

public class CSVRecordCleanerExample implements CSVRecordCleaner {

	/* (non-Javadoc)
	 * @see com.github.jferard.csvsniffer.CSVRecordCleaner#cleanRecord(org.apache.commons.csv.CSVRecord)
	 */
	@Override
	public List<String> cleanRecord(final CSVRecord record) {
		return new AbstractList<String> () {
	
			@Override
			public String get(int i) {
				final String s = record.get(i);
				if (i == 11 || i == 12 || i == 16) // numbers
					return s.replaceAll(",", "."); // from continental to US
				else
					return s;
			}
	
			@Override
			public int size() {
				return record.size();
			}};
	}

}
