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
import de.gerdiproject.json.datacite.Description;
import de.gerdiproject.json.datacite.Title;
import de.gerdiproject.json.datacite.enums.DescriptionType;
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
import org.jsoup.select.Elements;


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
    private final static String SITE_URL = "http://www.fao.org%s";
    private static final String PROVIDER = "Food and Agriculture Organization of the United Nations (FAO)";
    public static final String REPOSITORY_ID = "FAOSTAT";
    public static final List<String> DISCIPLINES = Collections.unmodifiableList(Arrays.asList("Statistics"));
    public static final String LOGO_URL = "http://www.fao.org/figis/website/assets/images/templates/shared/fao_logo.gif";
    public static final String DESCRIPTION_FORMAT = "%s : %s";
    public static final String ATTRIBUTE_HREF = "href";


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
            String url = attributes.get(ATTRIBUTE_HREF);
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


    public List<Description> descriptionParser(String url)
    {
        List<Description> descriptions = new LinkedList<>();
        Document doc = httpRequester.getHtmlFromUrl(url);
        //choose from webpage element with class allWidth and all his children
        Elements descriptionNames = doc.select("#allWidth").first().children();
        //define previousTextOfItem as zero because for first entry it will be empty
        String previousTextOfItem = "";

        for (Element item : descriptionNames) {
            //compare previousTextOfItem with all text that define description, if true, add to the description text after previousTextOfItem

            if (previousTextOfItem.equals("Collection Overview   ")) {
                Description description = new Description(String.format(DESCRIPTION_FORMAT, previousTextOfItem, item.text()), DescriptionType.Abstract);
                descriptions.add(description);
            }

            if (previousTextOfItem.contains("Status")) {
                Description description = new Description(String.format(DESCRIPTION_FORMAT, previousTextOfItem, item.text()), DescriptionType.Abstract);
                descriptions.add(description);

            }

            if (previousTextOfItem.contains("Typical Usage")) {
                Description description = new Description(String.format(DESCRIPTION_FORMAT, previousTextOfItem, item.text()), DescriptionType.Abstract);
                descriptions.add(description);

            }

            if (previousTextOfItem.contains("Audience")) {
                Description description = new Description(String.format(DESCRIPTION_FORMAT, previousTextOfItem, item.text()), DescriptionType.Abstract);
                descriptions.add(description);

            }

            if (previousTextOfItem.contains("Data Security Access Rules")) {

                Description description = new Description(String.format(DESCRIPTION_FORMAT, previousTextOfItem, item.text()), DescriptionType.Abstract);
                descriptions.add(description);

            }

            if (previousTextOfItem.contains("Dataset Overview")) {
                Description description = new Description(String.format(DESCRIPTION_FORMAT, previousTextOfItem, item.text()), DescriptionType.Abstract);
                descriptions.add(description);

            }

            previousTextOfItem = item.text();
            //logger.info("prvText: "+previousTextOfItem);

        }


        return descriptions;
    }





    public List<WebLink> weblinksParser(String url)
    {
        List<WebLink> weblinks = new LinkedList<>();

        Document doc = httpRequester.getHtmlFromUrl(url);
        //choose from webpage element with class allWidth and all his children
        Elements webPage = doc.select("#allWidth").first().children();
        String previousTextOfItem = "";

        //iterate all elements
        for (Element itemwebPage : webPage) {
            //find element with text what we need
            if (previousTextOfItem.contains("Available Formats & Information Products")) {
                Elements children = itemwebPage.children().select("a");

                for (Element itemChildren : children) {
                    Attributes attributes = itemChildren.attributes();

                    //need to check does this link work or not, some links not absolute, but relative, check it, if it is relative link, add SITE_URL
                    if (attributes.get(ATTRIBUTE_HREF).contains("www")) {
                        WebLink Link = new WebLink(attributes.get(ATTRIBUTE_HREF));
                        Link.setName(itemChildren.text());
                        Link.setType(WebLinkType.SourceURL);
                        weblinks.add(Link);
                    } else {
                        WebLink Link = new WebLink(String.format(SITE_URL, attributes.get(ATTRIBUTE_HREF)));
                        Link.setName(itemChildren.text());
                        Link.setType(WebLinkType.SourceURL);
                        weblinks.add(Link);
                    }


                }

            }

            previousTextOfItem = itemwebPage.text();


        }

        WebLink viewLink = new WebLink(url);
        viewLink.setName("View website");
        viewLink.setType(WebLinkType.ViewURL);
        weblinks.add(viewLink);


        WebLink logoLink = new WebLink(LOGO_URL);
        logoLink.setName("Logo");
        logoLink.setType(WebLinkType.ProviderLogoURL);
        weblinks.add(logoLink);



        return weblinks;
    }




    protected List<IDocument> harvestEntry(Element entry)
    {
        String language = getProperty(FishstatjParameterConstants.LANGUAGE_KEY);
        String version = getProperty(FishstatjParameterConstants.VERSION_KEY);
        Attributes attributes = entry.attributes();
        String url = attributes.get(ATTRIBUTE_HREF);

        DataCiteJson document = new DataCiteJson();
        document.setVersion(version);
        document.setLanguage(language);

        //parse titles
        document.setTitles(titleParser(url));

        //parse description
        document.setDescriptions(descriptionParser(url));

        document.setPublisher(PROVIDER);
        document.setResearchDisciplines(DISCIPLINES);
        document.setRepositoryIdentifier(REPOSITORY_ID);

        //parse weblinks
        document.setWebLinks(weblinksParser(url));

        return Arrays.asList(document);
    }





}
