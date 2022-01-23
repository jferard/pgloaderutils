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
package com.github.jferard.pgloaderutils.reader;

import com.github.jferard.pgloaderutils.CSVRecordProcessor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Normalized File Reader. An OpenableReader that cleans records on the fly.
 *
 * @author Julien Férard
 */
public class CSVProcessorFileReader extends OpenableReader {
    private static final int BUFFER_SIZE = 4096;

    public static CSVProcessorFileReader fromStream(final InputStream stream, final Charset charset,
                                                    final CSVFormat format,
                                                    final CSVRecordProcessor recordCleaner)
            throws IOException {
        final Reader streamReader = new InputStreamReader(stream, charset);
        final CSVParser parser = new CSVParser(streamReader, format);
        return new CSVProcessorFileReader(parser, recordCleaner);
    }

    public static CSVProcessorFileReader fromReader(final Reader reader, final CSVFormat format,
                                                    final CSVRecordProcessor recordCleaner)
            throws IOException {
        final CSVParser parser = new CSVParser(reader, format);
        return new CSVProcessorFileReader(parser, recordCleaner);
    }

    private final Logger logger;
    private final Reader modifiedStreamReader;
    private final CSVPrinter printer;
    private final CSVRecordProcessor recordProcessor;
    private final CSVParser parser;
    private final List<CSVRecord> ignoredRecords;

    public CSVProcessorFileReader(final CSVParser parser, final CSVRecordProcessor recordProcessor)
            throws IOException {
        this.recordProcessor = recordProcessor;
        final PipedWriter pipedWriter = new PipedWriter();
        this.modifiedStreamReader = new PipedReader(pipedWriter, BUFFER_SIZE);

        this.parser = parser;
        this.printer = new CSVPrinter(pipedWriter, CSVFormat.RFC4180);
        this.logger = Logger.getLogger("Cleaner");
        this.ignoredRecords = new ArrayList<>();
    }

    @Override
    public void open() throws IOException {
        int i = 0;
        CSVRecord record = null;
        final Iterator<CSVRecord> iterator = this.parser.iterator();
        try {
            while (iterator.hasNext()) {
                record = iterator.next();
                try {
                    final Iterable<String> l = this.recordProcessor.cleanRecord(record);
                    this.printer.printRecord(l);
                } catch (final ParseException | RuntimeException e) {
                    this.logger.log(Level.SEVERE, String.format("Error at line %s. Last record was %s",
                            this.parser.getRecordNumber(), record), e);
                    this.ignoredRecords.add(record);
                }
                if (i % 100000 == 0) {
                    this.logger.info("Lines written:" + i);
                }
                i++;
            }
        } catch (final IOException e) {
            this.logger.log(Level.SEVERE, String.format("Error at line %s. Last record was %s",
                    this.parser.getRecordNumber(), record), e);
        }
        this.parser.close();
        this.printer.close();
    }

    @Override
    public void close() throws IOException {
        this.modifiedStreamReader.close();
    }

    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        return this.modifiedStreamReader.read(cbuf, off, len);
    }

    public List<CSVRecord> getIgnoredRecords() {
        return this.ignoredRecords;
    }
}
