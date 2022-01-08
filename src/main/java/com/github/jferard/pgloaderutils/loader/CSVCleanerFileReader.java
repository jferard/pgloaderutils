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
package com.github.jferard.pgloaderutils.loader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Normalized File Reader. An OpenableReader that cleans records on the fly.
 *
 * @author Julien Férard
 */
public class CSVCleanerFileReader extends OpenableReader {
	private static final int BUFFER_SIZE = 4096;
	private final Logger logger;
	private final Reader modifiedStreamReader;
	private Iterator<CSVRecord> iterator;
	private final CSVPrinter printer;
	private final CSVRecordCleaner recordCleaner;
	private final CSVParser parser;

	public CSVCleanerFileReader(final CSVParser parser, final CSVRecordCleaner recordCleaner) throws IOException {
		this.recordCleaner = recordCleaner;
		final PipedWriter pipedWriter = new PipedWriter();
		this.modifiedStreamReader = new PipedReader(pipedWriter, BUFFER_SIZE);

		this.parser = parser;
		this.printer = new CSVPrinter(pipedWriter, CSVFormat.RFC4180);
		this.logger = Logger.getLogger("Cleaner");
	}


	public static CSVCleanerFileReader fromStream(final InputStream stream, final Charset charset, final CSVFormat format,
                                                  final CSVRecordCleaner recordCleaner) throws IOException {
		final Reader streamReader = new InputStreamReader(stream, charset);
		final CSVParser parser = new CSVParser(streamReader, format);
		return new CSVCleanerFileReader(parser, recordCleaner);
	}

	public static CSVCleanerFileReader fromReader(final Reader reader, final CSVFormat format, final CSVRecordCleaner recordCleaner) throws IOException {
		final CSVParser parser = new CSVParser(reader, format);
		return new CSVCleanerFileReader(parser, recordCleaner);
	}

	@Override
	public void open() throws IOException {
		int i = 0;
		CSVRecord record = null;
		this.iterator = this.parser.iterator();
		try {
			while (this.iterator.hasNext()) {
				record = this.iterator.next();
				final Iterable<String> l = this.recordCleaner.cleanRecord(record);
				this.printer.printRecord(l);
				if (i % 100000 == 0) {
					this.logger.info("Lines written:" + i);
				}
				i++;
			}
		} catch (final IOException e) {
			this.logger
					.log(Level.SEVERE, "Error at line " + this.parser.getRecordNumber() + ". Last record was " +
							record, e);
		}
		this.parser.close();
		this.printer.close();
	}

	@Override
	public void close() throws IOException {
		this.modifiedStreamReader.close();
	}

	@Override
	public int read(final char[] cbuf, final int off, final int len) throws IOException {
		return this.modifiedStreamReader.read(cbuf, off, len);
	}
}
