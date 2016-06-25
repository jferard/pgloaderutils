package com.github.jferard.pgloaderutils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Joiner;

public class CSVHeaderSnifferTest {
	private static final Charset ASCII = Charset.forName("US-ASCII");
	private Joiner joiner;

	@Before
	public void setUp() {
		this.joiner = Joiner.on('\n');
	}

	@Test
	public final void testWithHeader() throws IOException {
		CSVHeaderSniffer csvHeaderSniffer = new CSVHeaderSniffer((byte) ',',
				(byte) 0, (byte) 0, ASCII);
		InputStream stream = new ByteArrayInputStream(
				this.joiner.join("Year,Make,Model", "1997,Ford,E350",
						"2000,Mercury,Cougar").getBytes(ASCII));

		csvHeaderSniffer.sniff(stream, 10000);
		Assert.assertEquals(Arrays.asList("Year", "Make", "Model"),
				csvHeaderSniffer.getHeader());

	}

	@Test
	public final void testWithoutHeader() throws IOException {
		CSVHeaderSniffer csvHeaderSniffer = new CSVHeaderSniffer((byte) ',',
				(byte) 0, (byte) 0, ASCII);
		InputStream stream = new ByteArrayInputStream(this.joiner
				.join("1997,Ford,E350", "2000,Mercury,Cougar").getBytes(ASCII));

		csvHeaderSniffer.sniff(stream, 10000);
		Assert.assertNull(csvHeaderSniffer.getHeader());

	}

	@Test
	public final void testWithHeader2() throws IOException {
		CSVHeaderSniffer csvHeaderSniffer = new CSVHeaderSniffer((byte) ',',
				(byte)'"', (byte)'"', ASCII);
		final String s = this.joiner
				.join("Year,Make,Model,Description,Price",
						"1997,Ford,E350,\"ac, abs, moon\",3000.00",
						"1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00",
						"1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",,5000.00",
						"1996,Jeep,Grand Cherokee,\"MUST SELL!\n air, moon roof, loaded\",4799.00");
		InputStream stream = new ByteArrayInputStream(s
				.getBytes(ASCII));

		csvHeaderSniffer.sniff(stream, 10000);
		Assert.assertEquals(
				Arrays.asList("Year", "Make", "Model", "Description", "Price"),
				csvHeaderSniffer.getHeader());

	}

	@Test
	public final void testWithoutHeader2() throws IOException {
		
		CSVHeaderSniffer csvHeaderSniffer = new CSVHeaderSniffer((byte) ',',
				(byte)'"', (byte) '"', ASCII);
		final String s = this.joiner
				.join("1997,Ford,E350,\"ac, abs, moon\",3000.00",
						"1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00",
						"1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",,5000.00",
						"1996,Jeep,Grand Cherokee,\"MUST SELL!\n air, moon roof, loaded\",4799.00");
		InputStream stream = new ByteArrayInputStream(s
				.getBytes(ASCII));

		csvHeaderSniffer.sniff(stream, 10000);
		Assert.assertNull(csvHeaderSniffer.getHeader());

	}

}
