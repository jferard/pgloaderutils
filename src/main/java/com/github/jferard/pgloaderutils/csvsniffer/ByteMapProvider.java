package com.github.jferard.pgloaderutils.csvsniffer;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class ByteMapProvider {
	public ByteMapProvider() {}
	
	public char[] get(Charset charset) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(256);
		for (int c = 0; c<256; c++) {
			byteBuffer.put((byte) c);
		}
		return new String(byteBuffer.array(), charset).toCharArray();
	}

}
