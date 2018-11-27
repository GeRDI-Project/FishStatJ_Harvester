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

/**
 * @author Robin Weiss
 *
 */
public class FishStatJFileConstants
{
    public static final String RIGHTS_TEXT_EXCLUDED_PREFIX = "COPYRIGHT & DISCLAIMER CLAUSES\n\n";
    public static final String RIGHTS_TEXT_EXCLUDED_SUFFIX = "For comments, views and suggestions";

    public static final String DATES_TEXT_EXCLUDED_PREFIX = "Version History:\\n";
    public static final String DATES_TEXT_EXCLUDED_SUFFIX = "\\n\\n© FAO";

    public static final String UNZIP_ERROR = "Could not unzip file: %s";
    public static final String DOWNLOAD_ERROR = "Could not download file: %s";

    public static final String README_FILE_NAME = "Notes.txt";
    public static final String ISSUED_DATE_KEYWORD = "release";

    public static final List<String> LIST_OF_SUBJECTS = Collections.unmodifiableList(Arrays.asList("Name_en", "Scientific_Name"));
    public static final String CSV_FILE_WITH_SHIFTED_HEADER = "COUNTRY";

    public static final FilenameFilter CSV_FILE_FILTER = new FilenameFilter()
    {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".csv");
        }
    };


    /**
     * Private constructor, because only constants are provided.
     */
    private FishStatJFileConstants()
    {

    }
}
