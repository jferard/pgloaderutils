package com.github.jferard.pgloaderutils;

import java.util.List;

import org.apache.commons.csv.CSVRecord;

/**
 * The class CSVRecordCleaner clean and transforms a CSVRecord to a List.
 * 
 * @author Julien Férard
 */
public interface CSVRecordCleaner {

	/**
	 * @param record the commons csv record
	 * @return ths list of strings
	 */
	List<String> cleanRecord(CSVRecord record);

}