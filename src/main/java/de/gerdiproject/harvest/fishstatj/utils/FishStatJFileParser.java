/*
 *  Copyright © 2019 Robin Weiss (http://www.gerdi-project.de/)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package de.gerdiproject.harvest.fishstatj.utils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;

import de.gerdiproject.harvest.fishstatj.constants.FishStatJDataCiteConstants;
import de.gerdiproject.harvest.fishstatj.constants.FishStatJFileConstants;
import de.gerdiproject.harvest.fishstatj.constants.FishStatJLanguageConstants;
import de.gerdiproject.harvest.fishstatj.constants.FishStatJSourceConstants;
import de.gerdiproject.harvest.utils.data.DiskIO;
import de.gerdiproject.json.datacite.Date;
import de.gerdiproject.json.datacite.Rights;
import de.gerdiproject.json.datacite.Subject;
import de.gerdiproject.json.datacite.abstr.AbstractDate;
import de.gerdiproject.json.datacite.enums.DateType;

/**
 * This class offers functions for parsing unzipped file content.
 *
 * @author Robin Weiss
 */
public class FishStatJFileParser
{
    private final DiskIO diskReader = new DiskIO(new Gson(), StandardCharsets.UTF_8);


    /**
     * Reads the Notes.txt from the downloaded collection archive and extracts
     * copyright information.
     *
     * @param unzippedFolder the folder containing unzipped collection archive files
     *
     * @return a {@linkplain List} of {@linkplain Rights}
     */
    public List<Rights> getRights(final File unzippedFolder)
    {
        final List<Rights> rightsList = new LinkedList<>();

        // prepare file reader
        final File readmeFile = new File(unzippedFolder, FishStatJFileConstants.README_FILE_NAME);

        if (readmeFile.exists()) {
            // read file
            final String text = new DiskIO(new Gson(), StandardCharsets.UTF_8).getString(readmeFile);
            final int subStringFrom = text.indexOf(FishStatJFileConstants.RIGHTS_TEXT_EXCLUDED_PREFIX);

            // check if the file contains the required text area
            if (subStringFrom != -1) {
                final int subStringTo = text.indexOf(FishStatJFileConstants.RIGHTS_TEXT_EXCLUDED_SUFFIX, subStringFrom);
                final String rightsText = text.substring(
                                              subStringFrom + FishStatJFileConstants.RIGHTS_TEXT_EXCLUDED_PREFIX.length(),
                                              subStringTo);

                // this file is always in English
                rightsList.add(new Rights(rightsText, FishStatJLanguageConstants.ENGLISH_API_NAME));
            }
        }

        return rightsList;
    }


    /**
     * Reads csv files from the downloaded collection archive and extracts
     * metadata from them.
     *
     * @param unzippedFolder the folder containing unzipped collection archive files
     *
     * @return a {@linkplain List} of {@linkplain Subject}s
     */
    public Collection<Subject> getSubjects(final File unzippedFolder)
    {
        final Set<Subject> subjectSet = new HashSet<Subject>();

        final File[] csvFiles = unzippedFolder.listFiles(FishStatJFileConstants.CSV_FILE_FILTER);

        if (csvFiles != null) {
            final int len = csvFiles.length;

            for (int i = 0; i < len; i++) {
                // retrieve title row
                final List<String> titleRow = CsvUtils.getRow(0, csvFiles[i], StandardCharsets.UTF_8);

                if (titleRow == null)
                    continue;

                // calculate column shift
                final int headerShift = csvFiles[i].getName().contains(FishStatJFileConstants.CSV_FILE_WITH_SHIFTED_HEADER) ? 1 : 0;

                // retrieve interesting columns
                for (final String colTitle : FishStatJSourceConstants.VALID_SUBJECTS) {
                    final int columnIndex = titleRow.indexOf(colTitle) + headerShift;

                    if (columnIndex == -1)
                        continue;

                    final List<String> column = CsvUtils.getColumn(columnIndex, csvFiles[i], StandardCharsets.UTF_8);

                    if (column != null)
                        column.forEach((final String element) -> subjectSet.add(new Subject(element)));
                }
            }
        }

        return  subjectSet;
    }


    /**
     * Reads the Notes.txt from the downloaded collection archive and extracts
     * date information.
     *
     * @param unzippedFolder the folder containing unzipped collection archive files
     *
     * @return a {@linkplain List} of {@linkplain AbstractDate}s
     */
    public List<AbstractDate> getDates(final File unzippedFolder)
    {
        final List<AbstractDate> dateList = new LinkedList<>();

        // prepare file reader
        final File readmeFile = new File(unzippedFolder, FishStatJFileConstants.README_FILE_NAME);

        if (readmeFile.exists()) {
            // read file
            final String text = diskReader.getString(readmeFile);
            final int subStringFrom = text.indexOf(FishStatJFileConstants.DATES_TEXT_EXCLUDED_PREFIX);

            // check if the file contains the required text area
            if (subStringFrom != -1) {
                final int subStringTo = text.indexOf(FishStatJFileConstants.DATES_TEXT_EXCLUDED_SUFFIX, subStringFrom);

                // retrieve the text lines that contain dates
                final String[] dateTextLines = text
                                               .substring(subStringFrom + FishStatJFileConstants.DATES_TEXT_EXCLUDED_PREFIX.length(), subStringTo)
                                               .trim()
                                               .split("\n");

                // parse each date line
                final int len = dateTextLines.length;

                for (int i = 0; i < len; i++) {
                    final String[] dateInfo = dateTextLines[i].split("  ");
                    final String version = dateInfo[0];
                    final String dateString = dateInfo[1];
                    final String description = dateInfo[2];

                    final String dateInformation = String.format(FishStatJDataCiteConstants.DATE_INFORMATION, version, description);
                    final DateType dateType = description.contains(FishStatJFileConstants.ISSUED_DATE_KEYWORD)
                                              ? DateType.Issued
                                              : DateType.Updated;

                    final Date date = new Date(dateString, dateType);
                    date.setInformation(dateInformation);
                    dateList.add(date);
                }
            }
        }

        return dateList;
    }
}
