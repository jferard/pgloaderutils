/*
 * Some utilities for loading csv data into a PosgtreSQL database:
 * detect file encoding, CSV format and populate database
 *
 *     Copyright (C) 2016, 2018, 2020-2022 J. Férard <https://github.com/jferard>
 *
 * This file is part of pgLoader Utils.
 *
 * pgLoader Utils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pgLoader Utils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.jferard.pgloaderutils.sniffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.ParseException;

public class ParallelSniffer implements Sniffer {
	private Sniffer[] sniffers;

	public ParallelSniffer(final Sniffer... sniffers) {
		this.sniffers = sniffers;
	}

	@Override
	public void sniff(final InputStream inputStream, final int size) throws IOException {
		final int length = this.sniffers.length;
		final PipedOutputStream[] pipedOutputStreams = new PipedOutputStream[length];
		final Thread[] threads = new Thread[length];

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
					} catch (final IOException e) {
						e.printStackTrace();
					} catch (final ParseException e) {
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
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
