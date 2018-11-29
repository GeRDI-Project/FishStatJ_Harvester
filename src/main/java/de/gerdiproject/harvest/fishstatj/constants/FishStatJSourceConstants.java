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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import de.gerdiproject.json.datacite.Contributor;
import de.gerdiproject.json.datacite.enums.ContributorType;
import de.gerdiproject.json.datacite.enums.NameType;
import de.gerdiproject.json.datacite.nested.PersonName;

/**
 * This class contains constants that are used to retrieve source data from FishStatJ.
 *
 * @author Robin Weiss
 */
public class FishStatJSourceConstants
{
    public final static String BASE_URL = "http://www.fao.org/fishery/statistics/collections/en";
    public final static String SITE_URL = "http://www.fao.org%s";

    public static final String MAIN_PAGE_SELECTION =
        "a[title=\"data collection\"], "
        + "a[title=\"search interface\"], "
        + "a[title=\"webpage\"], "
        + "a[title=\"website; map\"]";

    public static final String SECTION_TITLE_CONTAINING_LINKS = "Available Formats & Information Products";
    public static final String SECTION_TITLE_CONTAINING_RIGHTS = "Data Security Access Rules";


    public static final String MAIN_TITLE_SELECTION = "#head_title_instance";
    public static final String SUB_TITLE_SELECTION = "#head_title_class";

    public static final String ALL_CONTACTS_SELECTION = "a:contains(Contact)";
    public static final String CONTACT_SELECTION = ".padBottom";

    public static final String ALL_SECTIONS_SELECTION = "#allWidth > [class=tableHead], #allWidth > div:not(#pageHeader)";

    public static final String LINKS_SELECTION = "a";
    public static final String CAPTION_CLASS = "subtitle";
    public static final String LINKS_AND_CAPTIONS_SELECTION = "a, span[class=" + CAPTION_CLASS + "]";
    public static final String ZIP_LINKS_SELECTION  = "a[href$=.zip]";

    public static final List<String> SIDEBAR_SELECTIONS = Collections.unmodifiableList(Arrays.asList(
                                                              "a:contains(Coverage)",
                                                              "a:contains(Structure)",
                                                              "a:contains(Data Source)"));

    public static final List<String> VALID_DESCRIPTIONS = Collections.unmodifiableList(Arrays.asList(
                                                              "Collection Overview",
                                                              "Status",
                                                              "Typical Usage",
                                                              "Audience",
                                                              "Data Security Access Rules",
                                                              "Dataset Overview"));

    public static final List<String> VALID_SUBJECTS = Collections.unmodifiableList(Arrays.asList(
            "Name_en",
            "Scientific_Name"));


    public static final Map<String, Contributor> VALID_CONTRIBUTOR_MAP = createValidContributorMap();

    public static final Pattern DOWNLOADABLE_FILE_PATTERN = Pattern.compile("(?:.+)/([^/]+)\\.(\\w+)$");
    public static final String LINK_REGEX = "(?:javascript:new_window\\(')?(?:https?://www\\.fao\\.org)?([^']+)(?:'.+\\);)?";
    public static final String LINK_REGEX_REPLACE = "http://www.fao.org$1";

    public static final String HREF_ATTRIBUTE = "href";
    public static final String PUBLICATION_TITLE = "Publication";


    public static final File DOWNLOADED_ZIP_FILE = new File("downloadedCollection.zip");
    public static final String UNZIP_FOLDER = "unzipped/";


    /**
     * Private constructor, because only constants are provided.
     */
    private FishStatJSourceConstants()
    {
    }


    /**
     * Creates a map of valid FishStatJ contributors.
     *
     * @return a map of valid FishStatJ contributors
     */
    private static Map<String, Contributor> createValidContributorMap()
    {
        final Map<String, Contributor> map = new HashMap<>();
        map.put("Valerio Crespi", new Contributor(new PersonName("Valerio Crespi", NameType.Personal), ContributorType.ContactPerson));
        map.put("Dr Jacek Majkowski", new Contributor(new PersonName("Dr Jacek Majkowski", NameType.Personal), ContributorType.ContactPerson));
        map.put("Ms Rossi Taddei", new Contributor(new PersonName("Ms Rossi Taddei", NameType.Personal), ContributorType.ContactPerson));
        map.put("Mr Fabio Carocci", new Contributor(new PersonName("Mr Fabio Carocci", NameType.Personal), ContributorType.ContactPerson));
        map.put("José Aguilar-Manjarrez", new Contributor(new PersonName("José Aguilar-Manjarrez", NameType.Personal), ContributorType.ContactPerson));
        map.put("Francesco Cardia", new Contributor(new PersonName("Francesco Cardia", NameType.Personal), ContributorType.ContactPerson));
        map.put("Karn Sukwong", new Contributor(new PersonName("Karn Sukwong", NameType.Personal), ContributorType.ContactPerson));
        map.put("Devin Bartley", new Contributor(new PersonName("Devin Bartley", NameType.Personal), ContributorType.ContactPerson));
        map.put("Blaise Kuemlangan", new Contributor(new PersonName("Blaise Kuemlangan", NameType.Personal), ContributorType.ContactPerson));
        map.put("Ms. Marianne Guyonnet", new Contributor(new PersonName("Ms. Marianne Guyonnet", NameType.Personal), ContributorType.ContactPerson));
        return map;
    }
}
