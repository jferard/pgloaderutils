package com.github.jferard.pgloaderutils;

import java.io.IOException;
import java.io.InputStream;

public interface Sniffer {
	void sniff(InputStream inputStream, int size) throws IOException;
}
