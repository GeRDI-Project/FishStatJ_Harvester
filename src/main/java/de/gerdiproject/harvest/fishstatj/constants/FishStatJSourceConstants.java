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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * This class contains constants that are used to retrieve source data from FishStatJ.
 *
 * @author Robin Weiss
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FishStatJSourceConstants
{
    public final static String MAIN_PAGE_URL = "http://www.fao.org/fishery/statistics/collections/%s";
    public static final String MAIN_PAGE_LINKS_SELECTION = "div.textDir a";

    public final static String SITE_URL = "http://www.fao.org%s";

    public final static String CONTAINS_TEXT_SELECTION = "a:contains(%s)";

    public static final String MAIN_TITLE_SELECTION = "#head_title_instance";
    public static final String SUB_TITLE_SELECTION = "#head_title_class";

    public static final String CONTACTS_AND_MAILS_SELECTION = "#allWidth div.padBottom, #allWidth a[href^=\"mailto\"]";
    public static final String DIV = "div";

    public static final String ALL_SECTIONS_SELECTION = "#allWidth > [class=tableHead], #allWidth > div:not(#pageHeader)";

    public static final String LINKS_SELECTION = "a";
    public static final String CAPTION_CLASS = "subtitle";
    public static final String LINKS_AND_CAPTIONS_SELECTION = "a, span." + CAPTION_CLASS;
    public static final String ZIP_LINKS_SELECTION  = "a[href$=.zip]";

    public static final List<String> VALID_SUBJECTS = Collections.unmodifiableList(Arrays.asList(
            "Name_en",
            "Scientific_Name"));

    public static final Pattern DOWNLOADABLE_FILE_PATTERN = Pattern.compile("(?:.+)/([^/]+)\\.(zip)$");
    public static final String GIF_EXTENSION = ".gif";
    public static final String NEW_WINDOW_LINK_REGEX = "^javascript:new_window\\('([^']+)'.+\\);$";
    public static final String NEW_WINDOW_LINK_REPLACE = "$1";
    public static final String RELATIVE_LINK_REGEX = "^(/.+)$";
    public static final String RELATIVE_LINK_REPLACE = "http://www.fao.org$1";
    public static final String CLICK_HERE_REPLACE = "$1";

    public static final String HREF_ATTRIBUTE = "href";

    public static final String ORGANISATION_NAME_REGEX = "^(?:Fish[a-z]+|HSVAR|Statistics|Food|Marine) .+$";
    public static final Pattern PERSON_NAME_PATTERN = Pattern.compile("^(?:(?:Dr|Mr|Ms|Mrs).? )?(?:([^ ]+?) ([^ ]+?)|([^,]+?), ([^,]+?))(?: \\(.*\\))?$");
    public static final String FISHSTAT_TIMEOUT_ERROR = "Could not reach FishStatJ! Wait a few minutes and /reset the harvester.";
}
