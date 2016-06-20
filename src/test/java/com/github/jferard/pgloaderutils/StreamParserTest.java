package com.github.jferard.pgloaderutils;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class StreamParserTest {

	@Test
	public final void test() throws UnsupportedEncodingException {
		StreamParser streamParser = new StreamParser(1024);
		for (byte b : "line1\nline2\r\nline3\n\rline4".getBytes("ASCII"))
			streamParser.put(b);
		
		List<Line> lines = streamParser.getLines();
		System.out.println(lines);
		
		Assert.assertEquals(5, lines.size());
		Assert.assertEquals("line1", lines.get(0).toString());
		Assert.assertEquals("line2", lines.get(1).toString());
		Assert.assertEquals("line3", lines.get(2).toString());
		Assert.assertEquals("", lines.get(3).toString());
		Assert.assertEquals("", lines.get(4).toString());
	}

}
