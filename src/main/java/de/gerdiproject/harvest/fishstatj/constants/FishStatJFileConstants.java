/*
 *  Copyright © 2018 Robin Weiss (http://www.gerdi-project.de/)
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
package de.gerdiproject.harvest.fishstatj.constants;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * This class contains constants regarding the download and parsing of FishStatJ datasets.
 *
 * @author Robin Weiss
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FishStatJFileConstants
{
    public static final String RIGHTS_TEXT_EXCLUDED_PREFIX = "COPYRIGHT & DISCLAIMER CLAUSES\n\n";
    public static final String RIGHTS_TEXT_EXCLUDED_SUFFIX = "For comments, views and suggestions";

    public static final String DATES_TEXT_EXCLUDED_PREFIX = "Version History:\\n";
    public static final String DATES_TEXT_EXCLUDED_SUFFIX = "\\n\\n© FAO";

    public static final String UNZIP_ERROR = "Could not unzip file stream from: %s";
    public static final String DOWNLOAD_ERROR = "Could not download file: %s";

    public static final String README_FILE_NAME = "Notes.txt";
    public static final String ISSUED_DATE_KEYWORD = "release";

    public static final List<String> LIST_OF_SUBJECTS = Collections.unmodifiableList(Arrays.asList("Name_en", "Scientific_Name"));
    public static final String CSV_FILE_WITH_SHIFTED_HEADER = "COUNTRY";

    public static final File DOWNLOADED_ZIP_FILE = new File("downloadedCollection.zip");
    public static final String UNZIP_FOLDER = "unzipped/";

    public static final int ZIP_EXTRACT_BUFFER_SIZE = 2048;

    public static final FilenameFilter CSV_FILE_FILTER = new FilenameFilter()
    {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".csv");
        }
    };
}
