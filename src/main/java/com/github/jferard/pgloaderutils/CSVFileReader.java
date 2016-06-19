package com.github.jferard.pgloaderutils;

import java.io.BufferedInputStream;
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

@SuppressWarnings("unused")
public class CSVFileReader extends Reader {

	private boolean hasBOM;
	private Charset charset;
	private char sep;
	private Reader reader;
	private BufferedInputStream stream2;
	private Reader modifiedStreamReader;
	private Iterator<CSVRecord> iterator;
	private CSVPrinter printer;
	private CSVRecordCleaner recordCleaner;
	private CSVParser parser;

	public CSVFileReader(InputStream stream, char sep, Charset charset, CSVRecordCleaner recordCleaner) throws IOException {
		this.recordCleaner = recordCleaner;
		Reader streamReader = new InputStreamReader(stream, charset);
		PipedWriter pipedWriter = new PipedWriter();
		this.modifiedStreamReader = new PipedReader(pipedWriter, 4096);
		
		this.parser = new CSVParser(streamReader, CSVFormat.EXCEL.withDelimiter(sep));
		this.printer = new CSVPrinter(pipedWriter, CSVFormat.EXCEL.withDelimiter('|'));
		this.iterator = this.parser.iterator();
	}
	
	public void start() throws IOException {
		while (this.iterator.hasNext()) {
			CSVRecord record = this.iterator.next();
			List<String> l = this.recordCleaner.cleanRecord(record);
			this.printer.printRecord(l);
		}
		this.printer.close();
	}

	@Override
	public void close() throws IOException {
		this.modifiedStreamReader.close();
		this.parser.close();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		return this.modifiedStreamReader.read(cbuf, off, len);
	}
	
}
