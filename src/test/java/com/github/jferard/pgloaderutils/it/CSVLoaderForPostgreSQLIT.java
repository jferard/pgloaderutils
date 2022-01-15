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

import com.github.jferard.pgloaderutils.CSVData;
import com.github.jferard.pgloaderutils.loader.CSVBulkLoader;
import com.github.jferard.pgloaderutils.loader.CSVRegularLoader;
import com.github.jferard.pgloaderutils.reader.SimpleFileReader;
import com.github.jferard.pgloaderutils.sql.Column;
import com.github.jferard.pgloaderutils.sql.DataType;
import com.github.jferard.pgloaderutils.sql.GeneralDataType;
import com.github.jferard.pgloaderutils.sql.Normalizer;
import com.github.jferard.pgloaderutils.sql.Table;
import com.google.common.io.Resources;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

public class CSVLoaderForPostgreSQLIT {
    public final void test() throws IOException, InterruptedException {
        try {
            Class.forName("org.postgresql.Driver");
            try {
                final Connection connection = DriverManager.getConnection(
                        "jdbc:postgresql://127.0.0.1:5432/testdb", "postgres",
                        "postgres");
                try {
                    final Statement statement = connection.createStatement();
                    statement.executeUpdate(
                            "DROP TABLE IF EXISTS testtable");
                    statement.executeUpdate(
                            "CREATE TABLE testtable ("
                                    + "col1 text," + "col2 decimal,"
                                    + "col3 text)");

                    final CSVBulkLoader loader = CSVBulkLoader
                            .toTable("testtable");
                    final StringReader stringReader = new StringReader("\"a\", 1.0, \"b,c\"\n"
                            + "\"d\", 2.0, \"f,g\"\n");
                    final SimpleFileReader csvReader = new SimpleFileReader(
                            stringReader, Logger.getLogger(""), 16);
                    loader.populate(connection, csvReader, false);
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

    @Test
    public final void testResourceBulk() throws IOException, InterruptedException {
        try {
            Class.forName("org.postgresql.Driver");
            try {
                final Connection connection = DriverManager.getConnection(
                        "jdbc:postgresql://127.0.0.1:5432/sirene", "postgres",
                        "postgres");
                try {
                    final Statement statement = connection.createStatement();
                    statement.executeUpdate(
                            "DROP TABLE IF EXISTS sirc");
                    statement.executeUpdate(
                            "CREATE TABLE sirc (\n" +
                                    "	siren text,\n" +
                                    "	nic text,\n" +
                                    "	l1_normalisee text,\n" +
                                    "	l2_normalisee text,\n" +
                                    "	l3_normalisee text,\n" +
                                    "	l4_normalisee text,\n" +
                                    "	l5_normalisee text,\n" +
                                    "	l6_normalisee text,\n" +
                                    "	l7_normalisee text,\n" +
                                    "	l1_declaree text,\n" +
                                    "	l2_declaree text,\n" +
                                    "	l3_declaree text,\n" +
                                    "	l4_declaree text,\n" +
                                    "	l5_declaree text,\n" +
                                    "	l6_declaree text,\n" +
                                    "	l7_declaree text,\n" +
                                    "	numvoie text,\n" +
                                    "	indrep text,\n" +
                                    "	typvoie text,\n" +
                                    "	libvoie text,\n" +
                                    "	codpos text,\n" +
                                    "	cedex text,\n" +
                                    "	rpet text,\n" +
                                    "	libreg text,\n" +
                                    "	depet text,\n" +
                                    "	arronet text,\n" +
                                    "	ctonet text,\n" +
                                    "	comet text,\n" +
                                    "	libcom text,\n" +
                                    "	du text,\n" +
                                    "	tu text,\n" +
                                    "	uu text,\n" +
                                    "	epci text,\n" +
                                    "	tcd text,\n" +
                                    "	zemet text,\n" +
                                    "	siege text,\n" +
                                    "	enseigne text,\n" +
                                    "	ind_publipo text,\n" +
                                    "	diffcom text,\n" +
                                    "	amintret text,\n" +
                                    "	natetab text,\n" +
                                    "	libnatetab text,\n" +
                                    "	apet700 text,\n" +
                                    "	libapet text,\n" +
                                    "	dapet text,\n" +
                                    "	tefet text,\n" +
                                    "	libtefet text,\n" +
                                    "	efetcent text,\n" +
                                    "	defet text,\n" +
                                    "	origine text,\n" +
                                    "	dcret text,\n" +
                                    "	date_deb_etat_adm_et text,\n" +
                                    "	activnat text,\n" +
                                    "	lieuact text,\n" +
                                    "	actisurf text,\n" +
                                    "	saisonat text,\n" +
                                    "	modet text,\n" +
                                    "	prodet text,\n" +
                                    "	prodpart text,\n" +
                                    "	auxilt text,\n" +
                                    "	nomen_long text,\n" +
                                    "	sigle text,\n" +
                                    "	nom text,\n" +
                                    "	prenom text,\n" +
                                    "	civilite text,\n" +
                                    "	rna text,\n" +
                                    "	nicsiege text,\n" +
                                    "	rpen text,\n" +
                                    "	depcomen text,\n" +
                                    "	adr_mail text,\n" +
                                    "	nj text,\n" +
                                    "	libnj text,\n" +
                                    "	apen700 text,\n" +
                                    "	libapen text,\n" +
                                    "	dapen text,\n" +
                                    "	aprm text,\n" +
                                    "	essen text,\n" +
                                    "	dateess text,\n" +
                                    "	tefen text,\n" +
                                    "	libtefen text,\n" +
                                    "	efencent text,\n" +
                                    "	defen text,\n" +
                                    "	categorie text,\n" +
                                    "	dcren text,\n" +
                                    "	amintren text,\n" +
                                    "	monoact text,\n" +
                                    "	moden text,\n" +
                                    "	proden text,\n" +
                                    "	esaann text,\n" +
                                    "	tca text,\n" +
                                    "	esaapen text,\n" +
                                    "	esasec1n text,\n" +
                                    "	esasec2n text,\n" +
                                    "	esasec3n text,\n" +
                                    "	esasec4n text,\n" +
                                    "	vmaj text,\n" +
                                    "	vmaj1 text,\n" +
                                    "	vmaj2 text,\n" +
                                    "	vmaj3 text,\n" +
                                    "	datemaj text\n" +
                                    ")");

                    final CSVBulkLoader loader = CSVBulkLoader
                            .toTable("sirc", ';', '"');
                    final Reader reader = new InputStreamReader(Resources.getResource
                                    ("sirc-17804_9075_14209_201612_L_M_20170104_171522721-part.csv")
                            .openStream(),
                            "ISO-8859-1");
                    final SimpleFileReader csvReader = new SimpleFileReader(
                            reader, Logger.getLogger(""), 16);
                    loader.populate(connection, csvReader, false);
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

    @Test
    public final void testResourceRegular()
			throws IOException, InterruptedException, SQLException, ParseException {
        final PGSimpleDataSource source = new PGSimpleDataSource();
        source.setServerNames(new String[]{"localhost"});
        source.setDatabaseName("postgres");
        source.setUser("postgres");
        source.setPassword("postgres");
        try (final Connection connection = source.getConnection()) {
            final Table table = new Table("sirc", Arrays.asList(
                    new Column("siren", GeneralDataType.TEXT),
                    new Column("nic", GeneralDataType.TEXT),
                    new Column("l1_normalisee", GeneralDataType.TEXT),
                    new Column("l2_normalisee", GeneralDataType.TEXT),
                    new Column("l3_normalisee", GeneralDataType.TEXT),
                    new Column("l4_normalisee", GeneralDataType.TEXT),
                    new Column("l5_normalisee", GeneralDataType.TEXT),
                    new Column("l6_normalisee", GeneralDataType.TEXT),
                    new Column("l7_normalisee", GeneralDataType.TEXT),
                    new Column("l1_declaree", GeneralDataType.TEXT),
                    new Column("l2_declaree", GeneralDataType.TEXT),
                    new Column("l3_declaree", GeneralDataType.TEXT),
                    new Column("l4_declaree", GeneralDataType.TEXT),
                    new Column("l5_declaree", GeneralDataType.TEXT),
                    new Column("l6_declaree", GeneralDataType.TEXT),
                    new Column("l7_declaree", GeneralDataType.TEXT),
                    new Column("numvoie", GeneralDataType.TEXT),
                    new Column("indrep", GeneralDataType.TEXT),
                    new Column("typvoie", GeneralDataType.TEXT),
                    new Column("libvoie", GeneralDataType.TEXT),
                    new Column("codpos", GeneralDataType.TEXT),
                    new Column("cedex", GeneralDataType.TEXT),
                    new Column("rpet", GeneralDataType.TEXT),
                    new Column("libreg", GeneralDataType.TEXT),
                    new Column("depet", GeneralDataType.TEXT),
                    new Column("arronet", GeneralDataType.TEXT),
                    new Column("ctonet", GeneralDataType.TEXT),
                    new Column("comet", GeneralDataType.TEXT),
                    new Column("libcom", GeneralDataType.TEXT),
                    new Column("du", GeneralDataType.TEXT),
                    new Column("tu", GeneralDataType.TEXT),
                    new Column("uu", GeneralDataType.TEXT),
                    new Column("epci", GeneralDataType.TEXT),
                    new Column("tcd", GeneralDataType.TEXT),
                    new Column("zemet", GeneralDataType.TEXT),
                    new Column("siege", GeneralDataType.TEXT),
                    new Column("enseigne", GeneralDataType.TEXT),
                    new Column("ind_publipo", GeneralDataType.TEXT),
                    new Column("diffcom", GeneralDataType.TEXT),
                    new Column("amintret", GeneralDataType.TEXT),
                    new Column("natetab", GeneralDataType.TEXT),
                    new Column("libnatetab", GeneralDataType.TEXT),
                    new Column("apet700", GeneralDataType.TEXT),
                    new Column("libapet", GeneralDataType.TEXT),
                    new Column("dapet", GeneralDataType.TEXT),
                    new Column("tefet", GeneralDataType.TEXT),
                    new Column("libtefet", GeneralDataType.TEXT),
                    new Column("efetcent", GeneralDataType.TEXT),
                    new Column("defet", GeneralDataType.TEXT),
                    new Column("origine", GeneralDataType.TEXT),
                    new Column("dcret", GeneralDataType.TEXT),
                    new Column("date_deb_etat_adm_et", GeneralDataType.TEXT),
                    new Column("activnat", GeneralDataType.TEXT),
                    new Column("lieuact", GeneralDataType.TEXT),
                    new Column("actisurf", GeneralDataType.TEXT),
                    new Column("saisonat", GeneralDataType.TEXT),
                    new Column("modet", GeneralDataType.TEXT),
                    new Column("prodet", GeneralDataType.TEXT),
                    new Column("prodpart", GeneralDataType.TEXT),
                    new Column("auxilt", GeneralDataType.TEXT),
                    new Column("nomen_long", GeneralDataType.TEXT),
                    new Column("sigle", GeneralDataType.TEXT),
                    new Column("nom", GeneralDataType.TEXT),
                    new Column("prenom", GeneralDataType.TEXT),
                    new Column("civilite", GeneralDataType.TEXT),
                    new Column("rna", GeneralDataType.TEXT),
                    new Column("nicsiege", GeneralDataType.TEXT),
                    new Column("rpen", GeneralDataType.TEXT),
                    new Column("depcomen", GeneralDataType.TEXT),
                    new Column("adr_mail", GeneralDataType.TEXT),
                    new Column("nj", GeneralDataType.TEXT),
                    new Column("libnj", GeneralDataType.TEXT),
                    new Column("apen700", GeneralDataType.TEXT),
                    new Column("libapen", GeneralDataType.TEXT),
                    new Column("dapen", GeneralDataType.TEXT),
                    new Column("aprm", GeneralDataType.TEXT),
                    new Column("essen", GeneralDataType.TEXT),
                    new Column("dateess", GeneralDataType.TEXT),
                    new Column("tefen", GeneralDataType.TEXT),
                    new Column("libtefen", GeneralDataType.TEXT),
                    new Column("efencent", GeneralDataType.TEXT),
                    new Column("defen", GeneralDataType.TEXT),
                    new Column("categorie", GeneralDataType.TEXT),
                    new Column("dcren", GeneralDataType.TEXT),
                    new Column("amintren", GeneralDataType.TEXT),
                    new Column("monoact", GeneralDataType.TEXT),
                    new Column("moden", GeneralDataType.TEXT),
                    new Column("proden", GeneralDataType.TEXT),
                    new Column("esaann", GeneralDataType.TEXT),
                    new Column("tca", GeneralDataType.TEXT),
                    new Column("esaapen", GeneralDataType.TEXT),
                    new Column("esasec1n", GeneralDataType.TEXT),
                    new Column("esasec2n", GeneralDataType.TEXT),
                    new Column("esasec3n", GeneralDataType.TEXT),
                    new Column("esasec4n", GeneralDataType.TEXT),
                    new Column("vmaj", GeneralDataType.TEXT),
                    new Column("vmaj1", GeneralDataType.TEXT),
                    new Column("vmaj2", GeneralDataType.TEXT),
                    new Column("vmaj3", GeneralDataType.TEXT),
                    new Column("datemaj", GeneralDataType.TEXT)
            ));
            final Statement statement = connection.createStatement();
            statement.executeUpdate(table.dropTableIfExistsQuery());
            statement.executeUpdate(table.createTableQuery(false));
			final Reader reader = new InputStreamReader(Resources.getResource
							("sirc-17804_9075_14209_201612_L_M_20170104_171522721-part.csv")
					.openStream(),
					"ISO-8859-1");
			final CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withDelimiter(';'));
			final CSVData data = new CSVData(parser, Collections.emptyList(), 1, new Normalizer() {
				@Override
				public Object normalize(final String value, final DataType type) throws ParseException {
					return value;
				}
			});
			final CSVRegularLoader loader =
					data.toRegularLoader(table);
			loader.load(connection, 100);
        }
    }
}
