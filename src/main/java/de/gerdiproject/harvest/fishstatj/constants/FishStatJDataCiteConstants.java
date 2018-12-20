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

import de.gerdiproject.json.datacite.Creator;
import de.gerdiproject.json.datacite.DataCiteJson;
import de.gerdiproject.json.datacite.enums.NameType;
import de.gerdiproject.json.datacite.extension.generic.AbstractResearch;
import de.gerdiproject.json.datacite.extension.generic.WebLink;
import de.gerdiproject.json.datacite.extension.generic.constants.ResearchDisciplineConstants;
import de.gerdiproject.json.datacite.extension.generic.enums.WebLinkType;
import de.gerdiproject.json.datacite.nested.PersonName;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * This class contains FishStatJ related constants that are used to enrich
 * the transformed {@linkplain DataCiteJson} documents.
 *
 * @author Robin Weiss
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FishStatJDataCiteConstants
{
    public static final String REPOSITORY_ID = "FishStatJ";
    public static final String PROVIDER = "Food and Agriculture Organization of the United Nations (FAO)";

    public static final String VIEW_URL_TITLE = "View Collection";

    public static final List<AbstractResearch> RESEARCH_DISCIPLINES = createResearchDisciplines();
    public static final List<Creator> CREATORS = createCreators();
    public static final WebLink LOGO_LINK = createLogoLink();

    public static final String DATE_INFORMATION = "Version %s - %s";


    /**
     * Creates a logo {@linkplain WebLink} for FishStatJ.
     *
     * @return a logo {@linkplain WebLink} for FishStatJ.
     */
    private static WebLink createLogoLink()
    {
        final String faoLogoUrl = "http://www.fao.org/figis/website/assets/images/templates/shared/fao_logo.gif";
        final WebLink logoLink = new WebLink(faoLogoUrl);
        logoLink.setType(WebLinkType.ProviderLogoURL);
        return logoLink;
    }


    /**
     * Creates a {@linkplain List} of {@linkplain AbstractResearch}es
     * that are the same for all documents.
     *
     * @return a {@linkplain List} of {@linkplain AbstractResearch}es
     */
    private static List<AbstractResearch> createResearchDisciplines()
    {
        return Collections.unmodifiableList(Arrays.asList(
                                                ResearchDisciplineConstants.STATISTICS_AND_ECONOMETRICS
                                            ));
    }


    /**
     * Creates a {@linkplain List} of {@linkplain Creator}s that only contains FAOSTAT
     * as a organisation.
     *
     * @return a {@linkplain List} of {@linkplain Creator}s
     */
    private static List<Creator> createCreators()
    {
        final Creator fishStatJCreator =
            new Creator(new PersonName(
                            PROVIDER,
                            NameType.Organisational));

        return Collections.unmodifiableList(Arrays.asList(fishStatJCreator));
    }
}
