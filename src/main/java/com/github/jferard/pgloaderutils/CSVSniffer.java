package com.github.jferard.pgloaderutils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class CSVSniffer implements Sniffer {

	private CSVConstraints csvConstraints;
	private CSVFormatSniffer csvSniffer;
	private EncodingSniffer encodingSniffer;
	private CSVHeaderSniffer headerSniffer;

	CSVSniffer(CSVConstraints csvConstraints) {
		this.csvConstraints = csvConstraints;
	}

	@Override
	public void sniff(InputStream inputStream, int size) throws IOException {
		byte[] bytes = new byte[size];

		int c = inputStream.read();
		int i = 0;
		while (c != -1 && i++ < size) {
			bytes[i] = (byte) c;
			c = inputStream.read();
		}

		this.csvSniffer = new CSVFormatSniffer(this.csvConstraints);
		this.encodingSniffer = new EncodingSniffer();
		ParallelSniffer parallelSniffer = new ParallelSniffer(this.csvSniffer,
				this.encodingSniffer);

		InputStream stream = new ByteArrayInputStream(bytes);
		parallelSniffer.sniff(stream, size);

		this.headerSniffer = new CSVHeaderSniffer(
				this.csvSniffer.getFinalDelimiter(), this.csvSniffer.getFinalQuote(),
				this.csvSniffer.getFinalEscape(), this.encodingSniffer.getCharset());

		stream = new ByteArrayInputStream(bytes);
		this.headerSniffer.sniff(stream, size);
	}

	public byte getFinalDelimiter() {
		return this.csvSniffer.getFinalDelimiter();
	}

	public byte getFinalEscape() {
		return this.csvSniffer.getFinalEscape();
	}

	public byte getFinalQuote() {
		return this.csvSniffer.getFinalQuote();
	}

	public Charset getCharset() {
		return this.encodingSniffer.getCharset();
	}

}
