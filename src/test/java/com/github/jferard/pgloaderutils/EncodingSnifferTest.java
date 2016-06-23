package com.github.jferard.pgloaderutils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class EncodingSnifferTest {
	private static final String STR = "my first test é { à @";
	static final Charset UTF_8 = Charset.forName("UTF-8");
	static final Charset ISO8859_15 = Charset.forName("ISO8859_15");
	private static final Charset ASCII = Charset.forName("US-ASCII");

	@Test
	public final void testUTF_8() throws IOException {
		EncodingSniffer encodingSniffer = new EncodingSniffer(null);
		InputStream stream = new ByteArrayInputStream(
				STR.getBytes(UTF_8));
		encodingSniffer.sniff(stream, 1000);
		Assert.assertEquals(UTF_8, encodingSniffer.getCharset());
	}

	@Test
	public final void testUTF_8b() throws IOException {
		EncodingSniffer encodingSniffer = new EncodingSniffer(null);
		InputStream stream = new ByteArrayInputStream(
				STR.getBytes(UTF_8));
		encodingSniffer.sniff(stream, 10);
		Assert.assertEquals(ASCII, encodingSniffer.getCharset());
	}
	
	@Test
	public final void testISO() throws IOException {
		EncodingSniffer encodingSniffer = new EncodingSniffer(null);
		InputStream stream = new ByteArrayInputStream(
				STR.getBytes(ISO8859_15));
		encodingSniffer.sniff(stream, 1000);
		Assert.assertEquals(null, encodingSniffer.getCharset());
	}

	@Test
	public final void testASCII() throws IOException {
		EncodingSniffer encodingSniffer = new EncodingSniffer(null);
		InputStream stream = new ByteArrayInputStream(
				STR.getBytes(ASCII));
		encodingSniffer.sniff(stream, 1000);
		Assert.assertEquals(ASCII, encodingSniffer.getCharset());
	}
}
