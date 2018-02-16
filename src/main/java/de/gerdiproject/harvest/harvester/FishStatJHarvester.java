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


import de.gerdiproject.json.datacite.DataCiteJson;
import de.gerdiproject.json.datacite.Title;
import de.gerdiproject.json.datacite.extension.WebLink;
import de.gerdiproject.json.datacite.extension.enums.WebLinkType;
//import de.gerdiproject.harvest.fishstatj.constants.FishstatjDataCiteConstants;
import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
    /**
     * Default Constructor that is called by the MainContext.
     */
    private final static String BASE_URL = "http://www.fao.org/fishery/statistics/collections/en";
    private static final String PROVIDER = "Food and Agriculture Organization of the United Nations (FAO)";
    public static final String REPOSITORY_ID = "FAOSTAT";
    public static final List<String> DISCIPLINES = Collections.unmodifiableList(Arrays.asList("Statistics"));
    public static final String LOGO_URL = "http://www.fao.org/figis/website/assets/images/templates/shared/fao_logo.gif";


    public FishStatJHarvester()
    {
        super(1);//
        // TODO initialize final fields
    }

    @Override
    //for entry we have list of html documents
    protected Collection<Element> loadEntries()
    {

        Document doc = httpRequester.getHtmlFromUrl(BASE_URL);

        Collection<Element> entries_primary = doc.select("a[title=\"data collection\"], a[title=\"search interface\"], a[title=\"webpage\"], a[title=\"website; map\"]");
        Collection<Element> entries = new  ArrayList<Element>();

        //Check if url link in html document doesn't return empty http request, add this document for entries
        for (Element item : entries_primary) {
            Attributes attributes = item.attributes();
            String url = attributes.get("href");
            Document doc1 = httpRequester.getHtmlFromUrl(url);

            if (doc1.hasText())
                entries.add(item);
        }

        return entries;
    }


    public List<Title> titleParser(String url)
    {

        List<Title> list_of_title = new ArrayList<Title>();
        Document doc = httpRequester.getHtmlFromUrl(url);

        Element title_first = doc.select("#head_title_class").first();
        String title1 = title_first.text();
        Title firstTitle = new Title(title1);
        list_of_title.add(firstTitle);

        Element title_second = doc.select("#head_title_instance").first();
        String title2 = title_second.text();
        Title secondTitle = new Title(title2);
        list_of_title.add(secondTitle);

        return list_of_title;

    }




    protected List<IDocument> harvestEntry(Element entry)
    {
        String language = getProperty(FishstatjParameterConstants.LANGUAGE_KEY);
        String version = getProperty(FishstatjParameterConstants.VERSION_KEY);
        Attributes attributes = entry.attributes();
        String url = attributes.get("href");

        DataCiteJson document = new DataCiteJson();
        document.setVersion(version);
        document.setLanguage(language);

        //parse titles
        document.setTitles(titleParser(url));


        document.setPublisher(PROVIDER);
        document.setResearchDisciplines(DISCIPLINES);
        document.setRepositoryIdentifier(REPOSITORY_ID);

        List<WebLink> links = new LinkedList<>();

        WebLink viewLink = new WebLink(url);
        viewLink.setName("View website");
        viewLink.setType(WebLinkType.ViewURL);
        links.add(viewLink);


        WebLink logoLink = new WebLink(LOGO_URL);
        logoLink.setName("Logo");
        logoLink.setType(WebLinkType.ProviderLogoURL);
        links.add(logoLink);
        document.setWebLinks(links);

        return Arrays.asList(document);
    }





}
