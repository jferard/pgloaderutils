package com.github.jferard.pgloaderutils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Joiner;

public class ParallelSnifferTest {
	private static final Charset ASCII = Charset.forName("US-ASCII");
	private Joiner joiner;

	@Before
	public void setUp() {
		this.joiner = Joiner.on('\n');
	}

	@Test
	public final void test() throws IOException {
		CSVFormatSniffer csvSniffer = new CSVFormatSniffer(
				CSVConstraints.builder().build());
		EncodingSniffer encodingSniffer = new EncodingSniffer();
		ParallelSniffer parallelSniffer = new ParallelSniffer(csvSniffer,
				encodingSniffer);

		final byte[] bytes = this.joiner
				.join("Year,Make,Model,Description,Price",
						"1997,Ford,E350,\"ac, abs, moon\",3000.00",
						"1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00",
						"1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",,5000.00",
						"1996,Jeep,Grand Cherokee,\"MUST SELL!\n air, moon roof, loaded\",4799.00")
				.getBytes(ASCII);
		InputStream stream = new ByteArrayInputStream(bytes);

		parallelSniffer.sniff(stream, 1000);
		Assert.assertEquals(',', (char) csvSniffer.getFinalDelimiter());
		Assert.assertEquals('"', (char) csvSniffer.getFinalQuote());
		Assert.assertEquals('"', (char) csvSniffer.getFinalEscape());
		Assert.assertEquals(ASCII, encodingSniffer.getCharset());
	}
}
