/**
 * Copyright © 2017 Bohdan Tkachuk (http://www.gerdi-project.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.gerdiproject.harvest.fishstatj.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

/**
 * This utility class offers static methods for manipulating CSV files.
 *
 * @author Robin Weiss
 */
public class CsvUtils
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvUtils.class);
    private static final String CSV_GET_COLUMN_ERROR = "Could not retrieve column %d from %s!";
    private static final String CSV_GET_ROW_ERROR = "Could not retrieve row %d from %s!";


    /**
     * Private constructor, because only static methods are provided.
     */
    private CsvUtils()
    {
    }


    /**
     * Retrieves a row from a specified csv file.
     *
     * @param rowIndex the index of the row to be retrieved
     * @param csvFile the csv file that is to be read
     * @param charset the charset that is used to read the csv file
     *
     * @return the row with the specified index, or null if it could not be retrieved
     */
    public static List<String> getRow(int rowIndex, File csvFile, Charset charset)
    {
        try
            (CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(csvFile), charset))) {
            reader.skip(rowIndex);
            return Arrays.asList(reader.readNext());

        } catch (IOException e) {
            LOGGER.error(String.format(CSV_GET_ROW_ERROR, rowIndex, csvFile.toString()), e);
            return null;
        }
    }


    /**
     * Retrieves a column from a specified csv file.
     *
     * @param columnIndex the index of the column to be retrieved
     * @param csvFile the csv file that is to be read
     * @param charset the charset that is used to read the csv file
     *
     * @return the column with the specified index, or null if it could not be retrieved
     */
    public static List<String> getColumn(int columnIndex, File csvFile, Charset charset)
    {
        final List<String> column = new LinkedList<>();

        try
            (CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(csvFile), charset))) {
            String [] row;

            while ((row = reader.readNext()) != null)
                column.add(row[columnIndex]);

        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            LOGGER.error(String.format(CSV_GET_COLUMN_ERROR, columnIndex, csvFile.toString()), e);
            return null;
        }

        return column;

    }
}
