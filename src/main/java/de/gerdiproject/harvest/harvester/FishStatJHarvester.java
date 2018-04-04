/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
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
package de.gerdiproject.harvest.harvester;

import de.gerdiproject.harvest.IDocument;
import de.gerdiproject.json.datacite.Creator;
import de.gerdiproject.json.datacite.DataCiteJson;
import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;
import de.gerdiproject.harvest.fishstatj.utils.ContributorsParser;
import de.gerdiproject.harvest.fishstatj.utils.DescriptionsParser;
import de.gerdiproject.harvest.fishstatj.utils.RightsParser;
import de.gerdiproject.harvest.fishstatj.utils.TitlesParser;
import de.gerdiproject.harvest.fishstatj.utils.WeblinksParser;
import de.gerdiproject.harvest.fishstatj.utils.ZipParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


/**
 * A harvester for FishStatJ (http://www.fao.org/fishery/statistics/collections/en).
 *
 * @author Bohdan Tkachuk
 */
public class FishStatJHarvester extends AbstractListHarvester<Element> // TODO choose an AbstractHarvester implementation that suits your needs
{

    private ZipParser zipParser;
    private ContributorsParser contributorsParser;
    private TitlesParser titlesParser;
    private DescriptionsParser descriptionsParser;
    private WeblinksParser weblinksParser;
    private RightsParser rightsParser;

    /**
     * Default Constructor that is called by the MainContext.
     */
    public FishStatJHarvester()
    {
        super(1);
        zipParser = new ZipParser();
        contributorsParser = new ContributorsParser();
        titlesParser = new TitlesParser();
        descriptionsParser = new DescriptionsParser();
        weblinksParser = new WeblinksParser();
        rightsParser = new RightsParser();

        //
        // TODO initialize final fields
    }

    @Override
    //for entry we have list of html documents
    protected Collection<Element> loadEntries()
    {

        Document doc = httpRequester.getHtmlFromUrl(FishstatjParameterConstants.BASE_URL);

        Collection<Element> entries_primary = doc.select("a[title=\"data collection\"], a[title=\"search interface\"], a[title=\"webpage\"], a[title=\"website; map\"]");
        Collection<Element> entries = new  ArrayList<Element>();

        //Check if url link in html document doesn't return empty http request, add this document for entries
        for (Element item : entries_primary) {
            Attributes attributes = item.attributes();
            String url = attributes.get(FishstatjParameterConstants.ATTRIBUTE_HREF);
            Document doc1 = httpRequester.getHtmlFromUrl(url);

            if (doc1.hasText())
                entries.add(item);
        }

        return entries;
    }



    protected List<IDocument> harvestEntry(Element entry)
    {
        String language = getProperty(FishstatjParameterConstants.LANGUAGE_KEY);
        String version = getProperty(FishstatjParameterConstants.VERSION_KEY);
        String url = entry.attr(FishstatjParameterConstants.ATTRIBUTE_HREF);

        DataCiteJson document = new DataCiteJson();
        document.setVersion(version);
        document.setLanguage(language);

        //parse titles
        document.setTitles(titlesParser.titleParser(url));

        //parse description
        document.setDescriptions(descriptionsParser.descriptionParser(url));

        //parse rights
        document.setRightsList(rightsParser.rightsParser(url));

        //parse weblinks
        document.setWebLinks(weblinksParser.weblinksParser(url));

        //parse contributors

        document.setContributors(contributorsParser.contributorsParser(url));

        //add creators, same as PROVIDER
        Creator creator = new Creator(FishstatjParameterConstants.PROVIDER);
        List<Creator> Creators = new LinkedList<>();
        Creators.add(creator);
        document.setCreators(Creators);
        document.setPublisher(FishstatjParameterConstants.PROVIDER);
        document.setResearchDisciplines(FishstatjParameterConstants.DISCIPLINES);
        document.setRepositoryIdentifier(FishstatjParameterConstants.REPOSITORY_ID);

        if (!ZipParser.findLinkForDownload(url).equals("")) {
            document.setSubjects(zipParser.getSubjectFromUrl(url));
            //logger.info("size"+subjectParser.getSubjectFromUrl(url).size());
        }

        return Arrays.asList(document);
    }

}
