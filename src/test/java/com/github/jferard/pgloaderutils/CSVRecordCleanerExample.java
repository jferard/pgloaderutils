package com.github.jferard.pgloaderutils;

import org.apache.commons.csv.CSVRecord;

import java.util.Iterator;

public class CSVRecordCleanerExample implements CSVRecordCleaner {

	/* (non-Javadoc)
	 * @see com.github.jferard.csvsniffer.CSVRecordCleaner#cleanRecord(org.apache.commons.csv.CSVRecord)
	 */
	@Override
	public Iterable<String> cleanRecord(final CSVRecord record) {
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					private int i = 0;

					@Override
					public boolean hasNext() {
						return this.i < record.size();
					}

					@Override
					public String next() {
						String s = record.get(this.i);
						if (this.i == 11 || this.i == 12 || this.i == 16) // numbers
							s = s.replaceAll(",", "."); // from continental to US

						this.i++;
						return s;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
}
