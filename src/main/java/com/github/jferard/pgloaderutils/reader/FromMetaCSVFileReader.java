/*
 * Some utilities for loading csv data into a PostgreSQL database:
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

import com.github.jferard.javamcsv.DataType;
import com.github.jferard.javamcsv.MetaCSVMetaData;
import com.github.jferard.javamcsv.MetaCSVReadException;
import com.github.jferard.javamcsv.MetaCSVReader;
import com.github.jferard.javamcsv.MetaCSVRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FromMetaCSVFileReader
        extends OpenableReader {
    private static final int BUFFER_SIZE = 4096;
    private final Logger logger;
    private final Reader modifiedStreamReader;
    private final MetaCSVReader metaReader;
    private final CSVPrinter printer;
    private Iterator<MetaCSVRecord> iterator;

    public FromMetaCSVFileReader(final MetaCSVReader metaReader) throws IOException {
        this.metaReader = metaReader;
        final PipedWriter pipedWriter = new PipedWriter();
        this.modifiedStreamReader = new PipedReader(pipedWriter, BUFFER_SIZE);
        this.printer = new CSVPrinter(pipedWriter, CSVFormat.RFC4180);
        this.logger = Logger.getLogger("Cleaner");
    }

    @Override
    public void open() throws IOException {
        int i = 0;
        MetaCSVRecord record = null;
        try {
            while (this.iterator.hasNext()) {
                record = this.iterator.next();
                this.printer.printRecord(record.toCanonicalList());
                if (i % 100000 == 0) {
                    this.logger.info("Lines written:" + i);
                }
                i++;
            }
        } catch (final IOException e) {
            this.logger
                    .log(Level.SEVERE, "Error at line " + i + ". Last record was " +
                            record, e);
        }
        this.metaReader.close();
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

    @Override
    public int read() throws IOException {
        return this.modifiedStreamReader.read();
    }

    public String createSQL(final String tableName) throws MetaCSVReadException {
        this.iterator = this.metaReader.iterator();
        final MetaCSVRecord record = this.iterator.next();
        final MetaCSVMetaData metaData = this.metaReader.getMetaData();
        final List<String> fields = new ArrayList<String>(record.size());
        for (int i=0; i<record.size(); i++) {
            final String fieldName = (String) record.getObject(i);
            // TODO: process fieldName
            final DataType dataType = metaData.getDataType(i);
            final String sqlType = this.dataTypeToSQLType(dataType);
            final String field = String.format("\"%s\" %s", fieldName, sqlType);
            fields.add(field);
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE \"").append(tableName).append("\" (\n");
        final Iterator<String> it = fields.iterator();
        sb.append("    ").append(it.next());
        while (it.hasNext()) {
            sb.append(",\n    ").append(it.next());
        }
        sb.append("\n)");
        return sb.toString();
    }

    private String dataTypeToSQLType(final DataType dataType) {
        final String sqlType;
        switch (dataType) {
            case BOOLEAN:
                sqlType = "BOOLEAN";
                break;
            case CURRENCY_DECIMAL:
            case PERCENTAGE_DECIMAL:
            case DECIMAL:
                sqlType = "DECIMAL";
                break;
            case CURRENCY_INTEGER:
            case INTEGER:
                sqlType = "INTEGER";
                break;
            case DATE:
                sqlType = "DATE";
                break;
            case DATETIME:
                sqlType = "TIMESTAMP";
                break;
            case PERCENTAGE_FLOAT:
            case FLOAT:
                sqlType = "FLOAT";
                break;
            default:
                sqlType = "TEXT";
                break;
        }
        return sqlType;
    }
}
