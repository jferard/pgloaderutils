/*******************************************************************************
 * pgLoader Utils - Some utillities for loading csv data into a PosgtreSQL database
 *    Copyright (C) 2016 J. Férard <https://github.com/jferard>
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
 ******************************************************************************/
package com.github.jferard.pgloaderutils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * Normalized File Reader
 * @author Julien Férard
 */
public class CSVCleanerFileReader extends OpenableReader {
	private static final int BUFFER_SIZE = 4096;
	private Reader modifiedStreamReader;
	private Iterator<CSVRecord> iterator;
	private CSVPrinter printer;
	private CSVRecordCleaner recordCleaner;
	private CSVParser parser;
	
	public CSVCleanerFileReader(Reader reader, CSVFormat format, CSVRecordCleaner recordCleaner) throws IOException {
		this.recordCleaner = recordCleaner;
		PipedWriter pipedWriter = new PipedWriter();
		this.modifiedStreamReader = new PipedReader(pipedWriter, BUFFER_SIZE);
		
		this.parser = new CSVParser(reader, format);
		this.printer = new CSVPrinter(pipedWriter, CSVFormat.RFC4180);
	}
	
	public static CSVCleanerFileReader fromStream(InputStream stream, Charset charset, CSVFormat format, CSVRecordCleaner recordCleaner) throws IOException {
		Reader streamReader = new InputStreamReader(stream, charset);
		return new CSVCleanerFileReader(streamReader, format, recordCleaner); 
	}
	
	@Override
	public void open() throws IOException {
		this.iterator = this.parser.iterator();
		while (this.iterator.hasNext()) {
			CSVRecord record = this.iterator.next();
			List<String> l = this.recordCleaner.cleanRecord(record);
			this.printer.printRecord(l);
		}
		this.parser.close();
		this.printer.close();
	}

	@Override
	public void close() throws IOException {
		this.modifiedStreamReader.close();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		return this.modifiedStreamReader.read(cbuf, off, len);
	}
}
