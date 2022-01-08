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

import com.github.jferard.pgloaderutils.loader.CSVLoaderForPostgreSQL;
import com.github.jferard.pgloaderutils.loader.SimpleFileReader;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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

					final CSVLoaderForPostgreSQL loader = CSVLoaderForPostgreSQL
							.toTable("testtable");
					final StringReader stringReader = new StringReader("\"a\", 1.0, \"b,c\"\n"
							+ "\"d\", 2.0, \"f,g\"\n");
					final SimpleFileReader csvReader = new SimpleFileReader(
							stringReader, Logger.getLogger(""), 16);
					loader.populate(connection,
							csvReader);
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
	public final void testResource() throws IOException, InterruptedException {
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

					final CSVLoaderForPostgreSQL loader = CSVLoaderForPostgreSQL
							.toTable("sirc", ';', '"');
					final Reader reader = new InputStreamReader(Resources.getResource
							("sirc-17804_9075_14209_201612_L_M_20170104_171522721-part.csv").openStream(),
							"ISO-8859-1");
					final SimpleFileReader csvReader = new SimpleFileReader(
							reader, Logger.getLogger(""), 16);
					loader.populate(connection,
							csvReader);
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
