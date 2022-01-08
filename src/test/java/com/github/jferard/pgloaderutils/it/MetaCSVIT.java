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

package com.github.jferard.pgloaderutils.it;

import com.github.jferard.javamcsv.MetaCSVDataException;
import com.github.jferard.javamcsv.MetaCSVParseException;
import com.github.jferard.javamcsv.MetaCSVReadException;
import com.github.jferard.javamcsv.MetaCSVReader;
import com.github.jferard.pgloaderutils.loader.CSVBulkLoader;
import com.github.jferard.pgloaderutils.reader.FromMetaCSVFileReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MetaCSVIT {
    @Test
    public void test()
            throws MetaCSVReadException, MetaCSVDataException, MetaCSVParseException, IOException,
            InterruptedException {
        final InputStream is =
                MetaCSVIT.class.getClassLoader().getResourceAsStream(
                        "sirc-17804_9075_14209_201612_L_M_20170104_171522721-part.csv");
        final InputStream metaIs =
                MetaCSVIT.class.getClassLoader().getResourceAsStream(
                        "sirc-17804_9075_14209_201612_L_M_20170104_171522721-part.mcsv");
        final MetaCSVReader reader = MetaCSVReader.create(is, metaIs);
        final FromMetaCSVFileReader fromMetaCSVFileReader = new FromMetaCSVFileReader(reader);
        try {
            Class.forName("org.postgresql.Driver");
            try {
                final Connection connection = DriverManager.getConnection(
                        "jdbc:postgresql://127.0.0.1:5432/testdb", "postgres",
                        "postgres");
                try {
                    final Statement statement = connection.createStatement();
                    final String tableName = "testtable2";
                    statement.executeUpdate(
                            "DROP TABLE IF EXISTS "+ tableName);
                    // read first line;
                    // data.getDescription(c);
                    final String sql = fromMetaCSVFileReader.createSQL(tableName);
                    Assert.assertEquals("CREATE TABLE \"testtable2\" (\n" +
                            "    \"SIREN\" TEXT,\n" +
                            "    \"NIC\" TEXT,\n" +
                            "    \"L1_NORMALISEE\" TEXT,\n" +
                            "    \"L2_NORMALISEE\" TEXT,\n" +
                            "    \"L3_NORMALISEE\" TEXT,\n" +
                            "    \"L4_NORMALISEE\" TEXT,\n" +
                            "    \"L5_NORMALISEE\" TEXT,\n" +
                            "    \"L6_NORMALISEE\" TEXT,\n" +
                            "    \"L7_NORMALISEE\" TEXT,\n" +
                            "    \"L1_DECLAREE\" TEXT,\n" +
                            "    \"L2_DECLAREE\" TEXT,\n" +
                            "    \"L3_DECLAREE\" TEXT,\n" +
                            "    \"L4_DECLAREE\" TEXT,\n" +
                            "    \"L5_DECLAREE\" TEXT,\n" +
                            "    \"L6_DECLAREE\" TEXT,\n" +
                            "    \"L7_DECLAREE\" TEXT,\n" +
                            "    \"NUMVOIE\" TEXT,\n" +
                            "    \"INDREP\" TEXT,\n" +
                            "    \"TYPVOIE\" TEXT,\n" +
                            "    \"LIBVOIE\" TEXT,\n" +
                            "    \"CODPOS\" TEXT,\n" +
                            "    \"CEDEX\" TEXT,\n" +
                            "    \"RPET\" TEXT,\n" +
                            "    \"LIBREG\" TEXT,\n" +
                            "    \"DEPET\" TEXT,\n" +
                            "    \"ARRONET\" TEXT,\n" +
                            "    \"CTONET\" TEXT,\n" +
                            "    \"COMET\" TEXT,\n" +
                            "    \"LIBCOM\" TEXT,\n" +
                            "    \"DU\" TEXT,\n" +
                            "    \"TU\" TEXT,\n" +
                            "    \"UU\" TEXT,\n" +
                            "    \"EPCI\" TEXT,\n" +
                            "    \"TCD\" TEXT,\n" +
                            "    \"ZEMET\" TEXT,\n" +
                            "    \"SIEGE\" BOOLEAN,\n" +
                            "    \"ENSEIGNE\" TEXT,\n" +
                            "    \"IND_PUBLIPO\" INTEGER,\n" +
                            "    \"DIFFCOM\" TEXT,\n" +
                            "    \"AMINTRET\" DATE,\n" +
                            "    \"NATETAB\" TEXT,\n" +
                            "    \"LIBNATETAB\" TEXT,\n" +
                            "    \"APET700\" TEXT,\n" +
                            "    \"LIBAPET\" TEXT,\n" +
                            "    \"DAPET\" TEXT,\n" +
                            "    \"TEFET\" TEXT,\n" +
                            "    \"LIBTEFET\" TEXT,\n" +
                            "    \"EFETCENT\" TEXT,\n" +
                            "    \"DEFET\" DATE,\n" +
                            "    \"ORIGINE\" TEXT,\n" +
                            "    \"DCRET\" DATE,\n" +
                            "    \"DATE_DEB_ETAT_ADM_ET\" DATE,\n" +
                            "    \"ACTIVNAT\" TEXT,\n" +
                            "    \"LIEUACT\" TEXT,\n" +
                            "    \"ACTISURF\" TEXT,\n" +
                            "    \"SAISONAT\" TEXT,\n" +
                            "    \"MODET\" TEXT,\n" +
                            "    \"PRODET\" BOOLEAN,\n" +
                            "    \"PRODPART\" TEXT,\n" +
                            "    \"AUXILT\" TEXT,\n" +
                            "    \"NOMEN_LONG\" TEXT,\n" +
                            "    \"SIGLE\" TEXT,\n" +
                            "    \"NOM\" TEXT,\n" +
                            "    \"PRENOM\" TEXT,\n" +
                            "    \"CIVILITE\" INTEGER,\n" +
                            "    \"RNA\" TEXT,\n" +
                            "    \"NICSIEGE\" TEXT,\n" +
                            "    \"RPEN\" TEXT,\n" +
                            "    \"DEPCOMEN\" TEXT,\n" +
                            "    \"ADR_MAIL\" TEXT,\n" +
                            "    \"NJ\" TEXT,\n" +
                            "    \"LIBNJ\" TEXT,\n" +
                            "    \"APEN700\" TEXT,\n" +
                            "    \"LIBAPEN\" TEXT,\n" +
                            "    \"DAPEN\" DATE,\n" +
                            "    \"APRM\" TEXT,\n" +
                            "    \"ESSEN\" BOOLEAN,\n" +
                            "    \"DATEESS\" DATE,\n" +
                            "    \"TEFEN\" TEXT,\n" +
                            "    \"LIBTEFEN\" TEXT,\n" +
                            "    \"EFENCENT\" TEXT,\n" +
                            "    \"DEFEN\" DATE,\n" +
                            "    \"CATEGORIE\" TEXT,\n" +
                            "    \"DCREN\" DATE,\n" +
                            "    \"AMINTREN\" DATE,\n" +
                            "    \"MONOACT\" TEXT,\n" +
                            "    \"MODEN\" TEXT,\n" +
                            "    \"PRODEN\" BOOLEAN,\n" +
                            "    \"ESAANN\" DATE,\n" +
                            "    \"TCA\" TEXT,\n" +
                            "    \"ESAAPEN\" TEXT,\n" +
                            "    \"ESASEC1N\" TEXT,\n" +
                            "    \"ESASEC2N\" TEXT,\n" +
                            "    \"ESASEC3N\" TEXT,\n" +
                            "    \"ESASEC4N\" TEXT,\n" +
                            "    \"VMAJ\" TEXT,\n" +
                            "    \"VMAJ1\" TEXT,\n" +
                            "    \"VMAJ2\" TEXT,\n" +
                            "    \"VMAJ3\" TEXT,\n" +
                            "    \"DATEMAJ\" TIMESTAMP\n" +
                            ")", sql);
                    statement.executeUpdate(sql);
                    final CSVBulkLoader loader = CSVBulkLoader
                            .toTable(tableName);
                    loader.populate(connection, fromMetaCSVFileReader, false);
                } finally {
                    connection.close();
                }
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
