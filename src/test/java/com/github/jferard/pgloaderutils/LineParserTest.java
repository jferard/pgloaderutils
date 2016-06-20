package com.github.jferard.pgloaderutils;

import java.nio.charset.Charset;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class LineParserTest {
	private static final byte BACKSLASH = (byte)'\\';
	private static final byte SINGLE_QUOTE = (byte) '\'';
	private static final byte PIPE = (byte) '|';
	private static final Charset CS = Charset.forName("ASCII");

	@Test
	public final void test() {
		byte[] bytes = "'a' | 'b | c' | 'c''d'".getBytes(CS);
		LineParser parser = new LineParser(bytes, bytes.length);
		List<Part> parts = parser.parse(PIPE, SINGLE_QUOTE);
		Assert.assertEquals(3, parts.size());
		Assert.assertEquals("a", parts.get(0).toString());
		Assert.assertEquals("b | c", parts.get(1).toString());
		Assert.assertEquals("c''d", parts.get(2).toString());
	}

	@Test
	public final void test1() {
		byte[] bytes = "'a' | 'b | c'".getBytes(CS);
		LineParser parser = new LineParser(bytes, bytes.length);
		List<Part> parts = parser.parse(PIPE, SINGLE_QUOTE, BACKSLASH);
		Assert.assertEquals(2, parts.size());
		Assert.assertEquals("a", parts.get(0).toString());
		Assert.assertEquals("b | c", parts.get(1).toString());
	}

	@Test
	public final void test2() {
		byte[] bytes = "'a' | 'b | c' | 'c\\'d'".getBytes(CS);
		LineParser parser = new LineParser(bytes, bytes.length);
		List<Part> parts = parser.parse(PIPE, SINGLE_QUOTE, BACKSLASH);
		Assert.assertEquals(3, parts.size());
		Assert.assertEquals("a", parts.get(0).toString());
		Assert.assertEquals("b | c", parts.get(1).toString());
		Assert.assertEquals("c\\'d", parts.get(2).toString());
	}

	@Test
	public final void test3() {
		byte[] bytes = "a||c".getBytes(CS);
		LineParser parser = new LineParser(bytes, bytes.length);
		List<Part> parts = parser.parse(PIPE);
		Assert.assertEquals("a", parts.get(0).toString());
		Assert.assertEquals("", parts.get(1).toString());
		Assert.assertEquals("c", parts.get(2).toString());
	}
}
