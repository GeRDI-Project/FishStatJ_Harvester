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
package de.gerdiproject.harvest.etls.transformers;

import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Element;

import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.etls.FishStatJETL;
import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;
import de.gerdiproject.harvest.fishstatj.parsers.ContributorsParser;
import de.gerdiproject.harvest.fishstatj.parsers.DatesParser;
import de.gerdiproject.harvest.fishstatj.parsers.DescriptionsParser;
import de.gerdiproject.harvest.fishstatj.parsers.RightsParser;
import de.gerdiproject.harvest.fishstatj.parsers.SubjectParser;
import de.gerdiproject.harvest.fishstatj.parsers.TitlesParser;
import de.gerdiproject.harvest.fishstatj.parsers.WeblinksParser;
import de.gerdiproject.harvest.fishstatj.utils.UtilZip;
import de.gerdiproject.json.datacite.Creator;
import de.gerdiproject.json.datacite.DataCiteJson;

/**
 * @author Robin Weiss
 *
 */
public class FishStatJTransformer extends AbstractIteratorTransformer<Element, DataCiteJson>
{
    private final SubjectParser subjectParser = new SubjectParser();
    private final ContributorsParser contributorsParser = new ContributorsParser();
    private final TitlesParser titlesParser = new TitlesParser();
    private final DescriptionsParser descriptionsParser = new DescriptionsParser();
    private final WeblinksParser weblinksParser = new WeblinksParser();
    private final RightsParser rightsParser = new RightsParser();
    private final DatesParser datesParser = new DatesParser();

    private String language;




    @Override
    public void init(AbstractETL<?, ?> etl)
    {
        super.init(etl);
        this.language = ((FishStatJETL) etl).getLanguage();
    }




    @Override
    protected DataCiteJson transformElement(Element source) throws TransformerException
    {
        final String url = source.attr(FishstatjParameterConstants.ATTRIBUTE_HREF);

        DataCiteJson document = new DataCiteJson(url);
        document.setLanguage(language);

        //parse titles
        document.addTitles(titlesParser.titleParser(url));

        //parse description
        document.addDescriptions(descriptionsParser.descriptionParser(url));

        //parse rights
        document.addRights(rightsParser.rightsParser(url));

        //parse weblinks
        document.addWebLinks(weblinksParser.weblinksParser(url));

        //parse contributors

        document.addContributors(contributorsParser.contributorsParser(url));

        //add creators, same as PROVIDER
        Creator creator = new Creator(FishstatjParameterConstants.PROVIDER);
        List<Creator> creatorList = new LinkedList<>();
        creatorList.add(creator);
        document.addCreators(creatorList);
        document.setPublisher(FishstatjParameterConstants.PROVIDER);
        document.addResearchDisciplines(FishstatjParameterConstants.DISCIPLINES);
        document.setRepositoryIdentifier(FishstatjParameterConstants.REPOSITORY_ID);

        document.addDates(datesParser.datesParser(url));
        document.addSubjects(subjectParser.getSubjectFromUrl(url));

        //if we found link for download, we add subjects
        if (!UtilZip.findLinkForDownload(url).equals(""))
            document.addSubjects(subjectParser.getSubjectFromUrl(url));

        return document;
    }

}
