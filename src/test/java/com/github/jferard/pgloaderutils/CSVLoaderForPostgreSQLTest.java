package com.github.jferard.pgloaderutils;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;

public class CSVLoaderForPostgreSQLTest {

	@Test
	public final void test() throws IOException, InterruptedException {
		try {
			Class.forName("org.postgresql.Driver");
			try {
				Connection connection = DriverManager.getConnection(
						"jdbc:postgresql://127.0.0.1:5432/testdb", "postgres",
						"postgres");
				try {
					Statement statement = connection.createStatement();
					statement.executeUpdate(
							"DROP TABLE IF EXISTS testtable");
					statement.executeUpdate(
							"CREATE TABLE testtable ("
									+ "col1 text," + "col2 decimal,"
									+ "col3 text)");

					CSVLoaderForPostgreSQL loader = CSVLoaderForPostgreSQL
							.fromTableName("testtable");
					final StringReader stringReader = new StringReader("\"a\", 1.0, \"b,c\"\n"
							+ "\"d\", 2.0, \"f,g\"\n");
					final CSVSimpleFileReader csvReader = new CSVSimpleFileReader(
							stringReader);
					loader.populate(connection,
							csvReader);
				} finally {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
