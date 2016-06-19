package com.github.jferard.pgloaderutils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

/**
 * Format is (@see https://www.postgresql.org/docs/9.1/static/populate.html,
 * 14.4.2 & 14.4.8):
 * 
 * <pre>
 * {@code
 * TRUNCATE table
 * COPY FROM stdin
 * ANALYZE table
 * }
 * </pre>
 * 
 * @author Julien Férard
 *
 * @see https://www.postgresql.org/docs/9.1/static/populate.html
 */
public class CSVLoaderTopgSQL {
	private final String truncateQuery;
	private final String copyQuery;
	private final String analyzeQuery;

	/**
	 * @param truncateQuery
	 * @param copyQuery
	 * @param analyzeQuery
	 */
	CSVLoaderTopgSQL(String truncateQuery, final String copyQuery,
			String analyzeQuery) {
		this.truncateQuery = truncateQuery;
		this.copyQuery = copyQuery;
		this.analyzeQuery = analyzeQuery;
	}

	public void populate(Connection connection, final CSVFileReader reader)
			throws SQLException {
		boolean autoCommit = connection.getAutoCommit();
		connection.setAutoCommit(false);

		Statement statement = connection.createStatement();
		statement.executeUpdate(this.truncateQuery);
		statement.close();

		final String copyQuery = this.copyQuery;

		ExecutorService threadExecutor = Executors.newFixedThreadPool(2);
		final BaseConnection c = (BaseConnection) connection;
		threadExecutor.execute(new Runnable() {

			@Override
			public void run() {
				CopyManager copyManager;
				try {
					copyManager = new CopyManager(c);
					copyManager.copyIn(copyQuery, reader);
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});

		try {
			reader.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		threadExecutor.shutdown();
		try {
			threadExecutor.awaitTermination(Long.MAX_VALUE,
					TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		statement = connection.createStatement();
		statement.executeUpdate(this.analyzeQuery);
		statement.close();
		connection.commit();
		connection.setAutoCommit(autoCommit);
	}

}
