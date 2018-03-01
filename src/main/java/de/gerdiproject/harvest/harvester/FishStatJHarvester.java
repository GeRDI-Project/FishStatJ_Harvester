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
import de.gerdiproject.json.datacite.Contributor;
import de.gerdiproject.json.datacite.Creator;
import de.gerdiproject.json.datacite.DataCiteJson;
import de.gerdiproject.json.datacite.Description;
import de.gerdiproject.json.datacite.Rights;
import de.gerdiproject.json.datacite.Title;
import de.gerdiproject.json.datacite.enums.ContributorType;
import de.gerdiproject.json.datacite.enums.DescriptionType;
import de.gerdiproject.json.datacite.enums.NameType;
import de.gerdiproject.json.datacite.extension.WebLink;
import de.gerdiproject.json.datacite.extension.enums.WebLinkType;
import de.gerdiproject.json.datacite.nested.PersonName;
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

        List<Title> listOfTitle = new ArrayList<Title>();
        Document doc = httpRequester.getHtmlFromUrl(url);

        Element titleFirst = doc.select("#head_title_class").first();
        String title1 = titleFirst.text();
        Title firstTitle = new Title(title1);
        listOfTitle.add(firstTitle);
        //logger.info("Title "+title1);
        Element title_second = doc.select("#head_title_instance").first();
        String title2 = title_second.text();

        //logger.info("Title "+title2);
        Title secondTitle = new Title(title2);
        listOfTitle.add(secondTitle);

        return listOfTitle;

    }
    //need to parse name and surname, and type of contributors
    public List<Contributor> contributorsParser(String url)
    {
        List<Contributor> listOfContributor = new LinkedList<>();
        Document doc = httpRequester.getHtmlFromUrl(url);
        //find element on page with "contact"
        Element linkToTheContact = doc.select("a:contains(Contact)").first();

        if (linkToTheContact != null) {
            String urlToContact = String.format(SITE_URL, linkToTheContact.attr(ATTRIBUTE_HREF));

            Document contactDoc = httpRequester.getHtmlFromUrl(urlToContact);
            //problem - we can have two contact person
            Elements contactElements = contactDoc.select(".padBottom");

            for (Element item : contactElements) {

                //we need to parse name and surname and recognise where is organisation where is person
                // for recognition we just check manually through all list of family name
                //  ,, , ,
                PersonName person = new PersonName(item.text(), NameType.Organisational);
                Contributor contributor = new Contributor(person, ContributorType.ContactPerson);

                if (item.text().contains("Valerio Crespi") || item.text().contains("Dr Jacek Majkowski") || item.text().contains("Ms Rossi Taddei") || item.text().contains("Mr Fabio Carocci") || item.text().contains("José Aguilar-Manjarrez") || item.text().contains("Francesco Cardia") || item.text().contains("Karn Sukwong") || item.text().contains("Devin Bartley") || item.text().contains("Blaise Kuemlangan") || item.text().contains("Ms. Marianne Guyonnet")) {
                    person.setNameType(NameType.Personal);
                    contributor.setName(person);
                }

                listOfContributor.add(contributor);
            }

        }



        return listOfContributor;
    }

    /*   public List<A> nameParser (String name)
       {
           List<String> name = new LinkedList
           return
       }
    */
    public List<Rights> rightsParser(String url)
    {
        List<Rights> Rights = new LinkedList<>();
        Document doc = httpRequester.getHtmlFromUrl(url);
        //choose from webpage element with class allWidth and all his children
        Elements descriptionNames = doc.select("#allWidth").first().children();
        //define previousTextOfItem as zero because for first entry it will be empty
        String previousTextOfItem = "";
        String hrefLink = "";

        for (Element item : descriptionNames) {

            if (previousTextOfItem.contains("Data Security Access Rules")) {
                //add all text of rights
                Rights right = new Rights(item.text());
                Element children = item.children().first();
                hrefLink = children.attr(ATTRIBUTE_HREF);

                // if we have some url, we add this url to the Rights
                if (!hrefLink.equals(""))
                    right.setURI(String.format(SITE_URL, hrefLink));

                Rights.add(right);
            }

            previousTextOfItem = item.text();
        }


        return Rights;
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
        String hrefLink = "";

        //iterate all elements
        for (Element itemwebPage : webPage) {
            //find element with text what we need
            if (previousTextOfItem.contains("Available Formats & Information Products")) {
                Elements children = itemwebPage.children().select("a");

                for (Element itemChildren : children) {
                    Attributes attributes = itemChildren.attributes();
                    hrefLink = attributes.get(ATTRIBUTE_HREF);

                    WebLink Link = new WebLink(hrefLink);

                    //need to check does this link work or not, some links not absolute, but relative, check it, if it is relative link, add SITE_URL
                    // first of at all cut /javascript:new_window('/fishery/statistics/global-production/query/en','biblio',1,1,1,1,1,1,1,700,650);
                    // and have only /fishery/statistics/global-production/query/en
                    if (hrefLink.contains("javascript:new_window"))
                        hrefLink = hrefLink.substring(hrefLink.indexOf('\'') + 1, hrefLink.indexOf(',') - 1);


                    //check this link absolute or relative
                    if (hrefLink.contains("www")) {
                        //WebLink Link = new WebLink(hrefLink);

                        //links on publication haven't any text, only picture, for this case, set name as "Publication"
                        if (itemChildren.text().equals("")) {
                            Link.setName("Publication");
                            Link.setType(WebLinkType.Related);
                        } else {
                            Link.setType(WebLinkType.SourceURL);
                            Link.setName(itemChildren.text());
                        }

                        Link.setUrl(hrefLink);
                        weblinks.add(Link);
                    }

                    else {
                        Link.setUrl(String.format(SITE_URL, hrefLink));
                        Link.setName(itemChildren.text());
                        Link.setType(WebLinkType.SourceURL);
                        weblinks.add(Link);
                    }
                }
            }

            previousTextOfItem = itemwebPage.text();
        }

        //links to "coverage", Str and etc
        //WebLink sideBar = new WebLink(); Element linkToTheContact = doc.select().first();

        Element sideBar = doc.select("a:contains(Coverage)").first();

        //if this element exist
        if (sideBar != null) {
            WebLink coverageWebLink = new WebLink(String.format(SITE_URL, sideBar.attr(ATTRIBUTE_HREF)));
            coverageWebLink.setName(sideBar.text());
            coverageWebLink.setType(WebLinkType.Related);
            weblinks.add(coverageWebLink);
        }

        sideBar = doc.select("a:contains(Structure)").first();

        if (sideBar != null) {
            WebLink StructureWebLink = new WebLink(String.format(SITE_URL, sideBar.attr(ATTRIBUTE_HREF)));
            StructureWebLink.setName(sideBar.text());
            StructureWebLink.setType(WebLinkType.Related);
            weblinks.add(StructureWebLink);
        }

        sideBar = doc.select("a:contains(Data Source)").first();

        if (sideBar != null) {
            WebLink DataSourceWebLink = new WebLink(String.format(SITE_URL, sideBar.attr(ATTRIBUTE_HREF)));
            DataSourceWebLink.setName(sideBar.text());
            DataSourceWebLink.setType(WebLinkType.Related);
            weblinks.add(DataSourceWebLink);
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
        String url = entry.attr(ATTRIBUTE_HREF);

        DataCiteJson document = new DataCiteJson();
        document.setVersion(version);
        document.setLanguage(language);

        //parse titles
        document.setTitles(titleParser(url));

        //parse description
        document.setDescriptions(descriptionParser(url));

        //parse rights
        document.setRightsList(rightsParser(url));

        //parse weblinks
        document.setWebLinks(weblinksParser(url));

        //parse contributors

        document.setContributors(contributorsParser(url));

        //add creators, same as PROVIDER
        Creator creator = new Creator(PROVIDER);
        List<Creator> Creators = new LinkedList<>();
        Creators.add(creator);
        document.setCreators(Creators);

        document.setPublisher(PROVIDER);
        document.setResearchDisciplines(DISCIPLINES);
        document.setRepositoryIdentifier(REPOSITORY_ID);


        return Arrays.asList(document);
    }

}
