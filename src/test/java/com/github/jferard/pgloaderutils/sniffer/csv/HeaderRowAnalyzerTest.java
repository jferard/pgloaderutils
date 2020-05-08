/*
 * Some utilities for loading csv data into a PosgtreSQL database:
 * detect file encoding, CSV format and populate database
 *
 *     Copyright (C) 2016, 2018 J. Férard <https://github.com/jferard>
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

package com.github.jferard.pgloaderutils.sniffer.csv;

import java.io.IOException;
import java.util.Arrays;

import com.github.jferard.pgloaderutils.sniffer.csv.HeaderRowAnalyzer;
import org.apache.commons.csv.CSVFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HeaderRowAnalyzerTest {

	private HeaderRowAnalyzer h;

	@Before
	public void setUp() throws Exception {
		this.h = new HeaderRowAnalyzer();
	}

	@Test
	public final void test() throws IOException {
		CSVFormat format = this.h.analyze(Arrays.asList("a", "b"), "a;b;c");
		Assert.assertEquals(';', format.getDelimiter());
		format = this.h.analyze(Arrays.asList("a", "b"), "a,b,c");
		Assert.assertEquals(',', format.getDelimiter());
	}
	
	@Test
	public final void test2() throws IOException {
		final CSVFormat format = this.h.analyze(Arrays.asList("a'b", "b"), "'a&'b';b;c");
		Assert.assertEquals(';', format.getDelimiter());
		Assert.assertEquals('&', (char) format.getEscapeCharacter());
		Assert.assertEquals('\'', (char) format.getQuoteCharacter());
	}

}
