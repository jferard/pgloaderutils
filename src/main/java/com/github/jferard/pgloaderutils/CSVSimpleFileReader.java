/*******************************************************************************
 * pgLoader Utils - Some utillities for loading csv data into a PosgtreSQL database
 *    Copyright (C) 2016 J. Férard <https://github.com/jferard>
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
 ******************************************************************************/
package com.github.jferard.pgloaderutils;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.util.logging.Logger;

/**
 * Normalized File Reader
 * @author Julien Férard
 */
public class CSVSimpleFileReader extends OpenableReader {
	private static final int BUFFER_SIZE = 4096;
	private Reader modifiedStreamReader;
	private Reader reader;
	private PipedWriter pipedWriter;
	private static final int ONE_MB = 1024 * 1024;

	public CSVSimpleFileReader(Reader reader) throws IOException {
		this.reader = reader;
		this.pipedWriter = new PipedWriter();
		this.modifiedStreamReader = new PipedReader(this.pipedWriter, BUFFER_SIZE);
	}
	
	@Override
	public void open() throws IOException {
		int i = 0;
		int c = this.reader.read();
		while (c != -1) {
			this.pipedWriter.write(c);
			c = this.reader.read();
			i++;
			if (i % (16*ONE_MB) == 0) {
				Logger.getLogger("").info("MBytes read: "+i/ ONE_MB);
			}
		}
		this.reader.close();
		this.pipedWriter.close();
	}

	@Override
	public void close() throws IOException {
		this.modifiedStreamReader.close();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		return this.modifiedStreamReader.read(cbuf, off, len);
	}
	
	@Override
	public int read() throws IOException {
		return this.modifiedStreamReader.read();
	}
	
}
