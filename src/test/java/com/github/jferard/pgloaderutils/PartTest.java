package com.github.jferard.pgloaderutils;

import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Test;

public class PartTest {

	@Test
	public final void test1() throws UnsupportedEncodingException {
		byte[] arr = new String("abcde").getBytes("ASCII");
		Part part = new Part(arr, 0, arr.length);
		Assert.assertEquals('a', part.getFirstChar());
		Assert.assertEquals('e', part.getLastChar());
		
		Assert.assertEquals('c', part.findCharBefore('d'));
		Assert.assertEquals(-1, part.findCharBefore('z'));
	}
	
	@Test
	public final void test2() throws UnsupportedEncodingException {
		byte[] arr = new String("   abcde ").getBytes("ASCII");
		Part part = new Part(arr, 0, arr.length);
		part.trim();
		Assert.assertEquals('a', part.getFirstChar());
		Assert.assertEquals('e', part.getLastChar());
		
		Assert.assertEquals('c', part.findCharBefore('d'));
		Assert.assertEquals(-1, part.findCharBefore('z'));
	}
}
