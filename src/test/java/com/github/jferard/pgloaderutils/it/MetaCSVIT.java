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
                try (final Connection connection = DriverManager.getConnection(
                        "jdbc:postgresql://127.0.0.1:5432/testdb", "postgres",
                        "postgres")) {
                    final Statement statement = connection.createStatement();
                    final String tableName = "testtable2";
                    statement.executeUpdate(
                            "DROP TABLE IF EXISTS " + tableName);
                    // read first line;
                    // data.getDescription(c);
                    final String sql = fromMetaCSVFileReader.createSQL(tableName);
                    Assert.assertEquals("CREATE TABLE testtable2 (\n" +
                            "    siren TEXT,\n" +
                            "    nic TEXT,\n" +
                            "    l1_normalisee TEXT,\n" +
                            "    l2_normalisee TEXT,\n" +
                            "    l3_normalisee TEXT,\n" +
                            "    l4_normalisee TEXT,\n" +
                            "    l5_normalisee TEXT,\n" +
                            "    l6_normalisee TEXT,\n" +
                            "    l7_normalisee TEXT,\n" +
                            "    l1_declaree TEXT,\n" +
                            "    l2_declaree TEXT,\n" +
                            "    l3_declaree TEXT,\n" +
                            "    l4_declaree TEXT,\n" +
                            "    l5_declaree TEXT,\n" +
                            "    l6_declaree TEXT,\n" +
                            "    l7_declaree TEXT,\n" +
                            "    numvoie TEXT,\n" +
                            "    indrep TEXT,\n" +
                            "    typvoie TEXT,\n" +
                            "    libvoie TEXT,\n" +
                            "    codpos TEXT,\n" +
                            "    cedex TEXT,\n" +
                            "    rpet TEXT,\n" +
                            "    libreg TEXT,\n" +
                            "    depet TEXT,\n" +
                            "    arronet TEXT,\n" +
                            "    ctonet TEXT,\n" +
                            "    comet TEXT,\n" +
                            "    libcom TEXT,\n" +
                            "    du TEXT,\n" +
                            "    tu TEXT,\n" +
                            "    uu TEXT,\n" +
                            "    epci TEXT,\n" +
                            "    tcd TEXT,\n" +
                            "    zemet TEXT,\n" +
                            "    siege BOOLEAN,\n" +
                            "    enseigne TEXT,\n" +
                            "    ind_publipo INTEGER,\n" +
                            "    diffcom TEXT,\n" +
                            "    amintret DATE,\n" +
                            "    natetab TEXT,\n" +
                            "    libnatetab TEXT,\n" +
                            "    apet700 TEXT,\n" +
                            "    libapet TEXT,\n" +
                            "    dapet TEXT,\n" +
                            "    tefet TEXT,\n" +
                            "    libtefet TEXT,\n" +
                            "    efetcent TEXT,\n" +
                            "    defet DATE,\n" +
                            "    origine TEXT,\n" +
                            "    dcret DATE,\n" +
                            "    date_deb_etat_adm_et DATE,\n" +
                            "    activnat TEXT,\n" +
                            "    lieuact TEXT,\n" +
                            "    actisurf TEXT,\n" +
                            "    saisonat TEXT,\n" +
                            "    modet TEXT,\n" +
                            "    prodet BOOLEAN,\n" +
                            "    prodpart TEXT,\n" +
                            "    auxilt TEXT,\n" +
                            "    nomen_long TEXT,\n" +
                            "    sigle TEXT,\n" +
                            "    nom TEXT,\n" +
                            "    prenom TEXT,\n" +
                            "    civilite INTEGER,\n" +
                            "    rna TEXT,\n" +
                            "    nicsiege TEXT,\n" +
                            "    rpen TEXT,\n" +
                            "    depcomen TEXT,\n" +
                            "    adr_mail TEXT,\n" +
                            "    nj TEXT,\n" +
                            "    libnj TEXT,\n" +
                            "    apen700 TEXT,\n" +
                            "    libapen TEXT,\n" +
                            "    dapen DATE,\n" +
                            "    aprm TEXT,\n" +
                            "    essen BOOLEAN,\n" +
                            "    dateess DATE,\n" +
                            "    tefen TEXT,\n" +
                            "    libtefen TEXT,\n" +
                            "    efencent TEXT,\n" +
                            "    defen DATE,\n" +
                            "    categorie TEXT,\n" +
                            "    dcren DATE,\n" +
                            "    amintren DATE,\n" +
                            "    monoact TEXT,\n" +
                            "    moden TEXT,\n" +
                            "    proden BOOLEAN,\n" +
                            "    esaann DATE,\n" +
                            "    tca TEXT,\n" +
                            "    esaapen TEXT,\n" +
                            "    esasec1n TEXT,\n" +
                            "    esasec2n TEXT,\n" +
                            "    esasec3n TEXT,\n" +
                            "    esasec4n TEXT,\n" +
                            "    vmaj TEXT,\n" +
                            "    vmaj1 TEXT,\n" +
                            "    vmaj2 TEXT,\n" +
                            "    vmaj3 TEXT,\n" +
                            "    datemaj TIMESTAMP\n" +
                            ")", sql);
                    statement.executeUpdate(sql);
                    final CSVBulkLoader loader = CSVBulkLoader
                            .toTable(tableName);
                    loader.populate(connection, fromMetaCSVFileReader, false);
                }
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
