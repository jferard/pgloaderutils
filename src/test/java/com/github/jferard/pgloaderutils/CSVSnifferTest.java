package com.github.jferard.pgloaderutils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;

public class CSVSnifferTest {
	private static final Charset ASCII = Charset.forName("US-ASCII");

	/**
	 * @throws IOException 
	 * @see https://en.wikipedia.org/wiki/Comma-separated_values
	 */
	@Test
	public final void test() throws IOException {
		CSVSniffer csvSniffer = new CSVSniffer(new CSVParams(
				new byte[] { ',', ';', '|' }, new byte[] { '\'', '"' },
				new byte[] { '\'', '"', '\\' }, 0));
		InputStream stream = new ByteArrayInputStream(
				"Year,Make,Model\n1997,Ford,E350\n2000,Mercury,Cougar"
						.getBytes(ASCII));
		
		csvSniffer.sniff(stream);
		Assert.assertEquals((byte) ',', csvSniffer.getDelimiter());
		Assert.assertEquals((byte) '\'', csvSniffer.getQuote());
		Assert.assertEquals((byte) '\'', csvSniffer.getEscape());
	}

}
