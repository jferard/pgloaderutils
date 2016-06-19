package com.github.jferard.pgloaderutils;

import java.io.IOException;
import java.io.InputStream;

public interface AsciiSniffer {
	void sniff(InputStream inputStream) throws IOException;
}
