package com.github.jferard.pgloaderutils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class ParallelSniffer implements Sniffer {
	private Sniffer[] sniffers;

	public ParallelSniffer(Sniffer... sniffers) {
		this.sniffers = sniffers;
	}

	@Override
	public void sniff(InputStream inputStream, final int size) throws IOException {
		final int length = this.sniffers.length;
		PipedOutputStream[] pipedOutputStreams = new PipedOutputStream[length];
		Thread[] threads = new Thread[length];

		for (int i = 0; i < length; i++) {
			final Sniffer sniffer = this.sniffers[i];
			pipedOutputStreams[i] = new PipedOutputStream();
			final PipedInputStream pipedInputStream = new PipedInputStream(
					pipedOutputStreams[i]);

			threads[i] = new Thread() {
				@Override
				public void run() {
					try {
						sniffer.sniff(pipedInputStream, size);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};

			threads[i].start();
		}

		int c = inputStream.read();
		while (c != -1) {
			for (int i = 0; i < length; i++) {
				pipedOutputStreams[i].write(c);
			}
			c = inputStream.read();
		}
		for (int i = 0; i < length; i++) {
			pipedOutputStreams[i].close();
		}
		
		try {
			for (int i = 0; i < length; i++) {
				threads[i].join();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
