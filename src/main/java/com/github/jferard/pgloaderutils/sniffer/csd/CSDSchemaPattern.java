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

package com.github.jferard.pgloaderutils.sniffer.csd;

import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A CSDSchemaPattern is a collection of CSDFields, with or without "wildcard" field.
 */
public class CSDSchemaPattern<F extends CSDFieldPattern> implements SizedIterable<F> {
    private Collection<F> fields;
    private boolean optionalHeader;

    public CSDSchemaPattern(Collection<F> fields, boolean optionalHeader) {
        this.fields = fields;
        this.optionalHeader = optionalHeader;
    }

    /**
     * @return the number of fields before a "wildcard" field, or the total number of fields.
     */
    public int size() {
        int schemaSize = 0;
        for (F field : fields) {
            if (field.isWildCard()) {
                break;
            }

            schemaSize++;
        }
        return schemaSize;
    }

    @Override
    public Iterator<F> iterator() {
        return this.fields.iterator();
    }

    @Override
    public String toString() {
        return this.fields.toString();
    }

    public boolean hasOptionalHeader() {
        return this.optionalHeader;
    }

    public CSDSchema<F> newSchema(CSDFieldFactory<F> factory, CSVRecord firstRecord) {
        List<F> newFields = new ArrayList<F>(this.fields.size());

        for (F field : this.fields) {
            if (field.isWildCard()) {
                this.addFields(factory, newFields, field, firstRecord, this.fields.size());
                break;
            }
            newFields.add(field);
        }
        System.out.println(newFields);
        return new CSDSchema<F>(newFields, this.optionalHeader);
    }

    private void addFields(CSDFieldFactory<F> factory, List<F> newFields, F field, CSVRecord firstRecord, int begin) {
        if (firstRecord == null) {
            return;
        }

        for (int i=begin; i<firstRecord.size(); i++) {
            String name = firstRecord.get(i);
            newFields.add(factory.create(field.getType(), name, name, true));
        }
    }
}
