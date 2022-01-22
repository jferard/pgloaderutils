/*
 * Some utilities for loading csv data into a PosgtreSQL database:
 * detect file encoding, CSV format and populate database
 *
 *     Copyright (C) 2016, 2018, 2020-2022 J. Férard <https://github.com/jferard>
 *
 * This file is part of pgLoader Utils.
 *
 * pgLoader Utils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pgLoader Utils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.jferard.pgloaderutils.it;

import com.github.jferard.pgloaderutils.reader.CSVRecordProcessor;
import org.apache.commons.csv.CSVRecord;

import java.util.Iterator;

public class CSVRecordCleanerExample implements CSVRecordProcessor {

	/* (non-Javadoc)
	 * @see com.github.jferard.csv.CSVRecordCleaner#cleanRecord(org.apache.commons.csv.CSVRecord)
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
                        {
                            s = s.replaceAll(",", "."); // from continental to US
                        }

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
