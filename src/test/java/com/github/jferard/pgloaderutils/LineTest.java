package com.github.jferard.pgloaderutils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class LineTest {

	private static final Charset CS = Charset.forName("ASCII");;

	@Test
	public final void test() throws UnsupportedEncodingException {
		Line line = new Line(1);
		for (byte b : "a|b|c".getBytes(CS))
			line.append(b);
		Assert.assertEquals(2, line.getCount('|'));
		List<Part> parts = line.asParts('|');
		Assert.assertEquals(3, parts.size());
		Assert.assertEquals('a', parts.get(0).getFirstChar());
		Assert.assertEquals('a', parts.get(0).getLastChar());
		Assert.assertEquals('b', parts.get(1).getFirstChar());
		Assert.assertEquals('b', parts.get(1).getLastChar());
		Assert.assertEquals('c', parts.get(2).getFirstChar());
		Assert.assertEquals('c', parts.get(2).getLastChar());
	}
	
	@Test
	public final void test2() throws UnsupportedEncodingException {
		Line line = new Line(1);
		for (byte b : "a|b|c".getBytes(CS))
			line.append(b);
		Assert.assertEquals(0, line.getCount(','));
		List<Part> parts = line.asParts(',');
		Assert.assertEquals(1, parts.size());
		Assert.assertEquals('a', parts.get(0).getFirstChar());
		Assert.assertEquals('c', parts.get(0).getLastChar());
	}
	
	@Test
	public final void test3() throws UnsupportedEncodingException {
		Line line = new Line(1);
		for (byte b : "'a' | 'b | c' | 'c''d'".getBytes(CS))
			line.append(b);
		List<Part> parts = line.asParts('|', '\'' , '\'');
		Assert.assertEquals(3, parts.size());
		Assert.assertEquals("a", parts.get(0).toString());
		Assert.assertEquals("b | c", parts.get(1).toString());
		Assert.assertEquals("c''d", parts.get(2).toString());
	}
	
}
