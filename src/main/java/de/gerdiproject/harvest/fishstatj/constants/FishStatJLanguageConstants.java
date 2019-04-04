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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.gerdiproject.harvest.etls.FishStatJLanguageVO;
import de.gerdiproject.json.datacite.enums.DescriptionType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * This class contains constants used for harvesting FishStatJ in specified languages.
 *
 * @author Robin Weiss
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FishStatJLanguageConstants
{
    public static final String ENGLISH_API_NAME = "en";
    public static final String ARABIAN_API_NAME = "ar";
    public static final String SPANISH_API_NAME = "es";
    public static final String FRENCH_API_NAME = "fr";
    public static final String RUSSIAN_API_NAME = "ru";
    public static final String SIMPLE_CHINESE_API_NAME = "zh";

    public static final Map<String, FishStatJLanguageVO> LANGUAGE_MAP = createLanguageMap();


    /**
     * Creates a map of language API names to value objects that contain
     * everything necessary for harvesting FishStatJ in a certain language.
     *
     * @return a language map
     */
    private static Map<String, FishStatJLanguageVO> createLanguageMap()
    {
        final Map<String, FishStatJLanguageVO> map = new HashMap<>();

        map.put(ENGLISH_API_NAME, createEnglishVO());

        return Collections.unmodifiableMap(map);
    }


    /**
     * Creates a {@linkplain FishStatJLanguageVO} for harvesting the English version of FishStatJ.
     *
     * @return a {@linkplain FishStatJLanguageVO}
     */
    private static FishStatJLanguageVO createEnglishVO()
    {
        final FishStatJLanguageVO vo = new FishStatJLanguageVO();

        // add valid descriptions
        final Map<String, DescriptionType> validDescriptionMap = new HashMap<>();
        validDescriptionMap.put("Collection Overview", DescriptionType.Abstract);
        validDescriptionMap.put("Status", DescriptionType.TechnicalInfo);
        validDescriptionMap.put("Audience", DescriptionType.Other);
        validDescriptionMap.put("Dataset Overview", DescriptionType.Abstract);
        vo.setValidDescriptionMap(Collections.unmodifiableMap(validDescriptionMap));

        // add sidebar titles
        final List<String> sideBarTitles = Arrays.asList(
                                               "Coverage",
                                               "Structure",
                                               "Data Source");
        vo.setSidebarTitles(Collections.unmodifiableList(sideBarTitles));

        // add other fields
        vo.setApiName(ENGLISH_API_NAME);
        vo.setWebLinksSectionTitle("Available Formats & Information Products");
        vo.setRightsSectionTitle("Data Security Access Rules");
        vo.setSubjectsSectionTitle("Typical Usage");
        vo.setContactsTabTitle("Contact");
        vo.setDatasetSubTitle("Dataset");
        vo.setWebLinkTitleRegex("(?:Click here to [^ ]+?|Explore )[a-z ]*?([A-Z].+?)(?: through .+)?$");

        return vo;
    }
}
