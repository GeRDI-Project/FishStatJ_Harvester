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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.etls.FishStatJETL;
import de.gerdiproject.harvest.etls.FishStatJLanguageVO;
import de.gerdiproject.harvest.etls.extractors.FishStatJCollectionVO;
import de.gerdiproject.harvest.fishstatj.constants.FishStatJDataCiteConstants;
import de.gerdiproject.harvest.fishstatj.constants.FishStatJSourceConstants;
import de.gerdiproject.harvest.fishstatj.utils.FishStatJFileParser;
import de.gerdiproject.harvest.utils.file.FileUtils;
import de.gerdiproject.json.datacite.Contributor;
import de.gerdiproject.json.datacite.DataCiteJson;
import de.gerdiproject.json.datacite.Description;
import de.gerdiproject.json.datacite.Rights;
import de.gerdiproject.json.datacite.Subject;
import de.gerdiproject.json.datacite.Title;
import de.gerdiproject.json.datacite.enums.ContributorType;
import de.gerdiproject.json.datacite.enums.NameType;
import de.gerdiproject.json.datacite.enums.TitleType;
import de.gerdiproject.json.datacite.extension.generic.ResearchData;
import de.gerdiproject.json.datacite.extension.generic.WebLink;
import de.gerdiproject.json.datacite.extension.generic.enums.WebLinkType;
import de.gerdiproject.json.datacite.nested.PersonName;

/**
 * This {@linkplain AbstractIteratorTransformer} implementation transforms FishStatJ collections to
 * {@linkplain DataCiteJson} objects.

 * @author Robin Weiss, Bohdan Tkachuk
 */
public class FishStatJTransformer extends AbstractIteratorTransformer<FishStatJCollectionVO, DataCiteJson>
{
    private final FishStatJFileParser fileParser = new FishStatJFileParser();
    private FishStatJLanguageVO languageVo;
    private File downloadFolder;


    @Override
    public void init(final AbstractETL<?, ?> etl)
    {
        this.languageVo = ((FishStatJETL) etl).getLanguageVO();
    }


    @Override
    protected DataCiteJson transformElement(final FishStatJCollectionVO source) throws TransformerException
    {
        this.downloadFolder = source.getDownloadFolder();
        
        final DataCiteJson document = new DataCiteJson(source.getCollectionUrl());
        document.setLanguage(languageVo.getApiName());

        // add static metadata
        document.setRepositoryIdentifier(FishStatJDataCiteConstants.REPOSITORY_ID);
        document.addCreators(FishStatJDataCiteConstants.CREATORS);
        document.setPublisher(FishStatJDataCiteConstants.PROVIDER);
        document.addResearchDisciplines(FishStatJDataCiteConstants.RESEARCH_DISCIPLINES);

        // retrieve metadata by parsing web pages
        document.addTitles(getTitles(source));
        document.addDescriptions(getDescriptions(source));
        document.addContributors(getContributors(source));
        document.addResearchData(getResearchData(source));
        document.addRights(getRights(source));
        document.addWebLinks(getLogoAndViewWebLinks(source));
        document.addWebLinks(getSideBarWebLinks(source));
        document.addWebLinks(getSectionWebLinks(source));
        document.addSubjects(getSubjects(source));

        // retrieve metadata from downloaded zip archive
        if (downloadFolder != null) {
            document.addRights(fileParser.getRights(downloadFolder));
            document.addDates(fileParser.getDates(downloadFolder));
            document.addSubjects(fileParser.getSubjects(downloadFolder));

            // remove unzipped files
            FileUtils.deleteFile(downloadFolder);
        }

        return document;
    }


    /**
     * Retrieves a {@linkplain List} of {@linkplain Title}s of the FishStatJ collection.
     *
     * @param source the value object that is to be transformed to a document
     *
     * @return a {@linkplain List} of FishStatJ {@linkplain Title}s
     */
    private List<Title> getTitles(final FishStatJCollectionVO source)
    {
        final List<Title> titleList = new LinkedList<Title>();

        // add main title
        final String titleText = source.getCollectionPage()
                                 .selectFirst(FishStatJSourceConstants.MAIN_TITLE_SELECTION)
                                 .text();
        titleList.add(new Title(titleText, null, languageVo.getApiName()));

        // add category as sub-title
        final String categoryText = source.getCollectionPage()
                                    .selectFirst(FishStatJSourceConstants.SUB_TITLE_SELECTION)
                                    .text();
        titleList.add(new Title(categoryText, TitleType.Subtitle, languageVo.getApiName()));

        return titleList;
    }


    /**
     * Retrieves a {@linkplain List} of {@linkplain Description}s of the FishStatJ collection.
     *
     * @param source the value object that is to be transformed to a document
     *
     * @return a {@linkplain List} of FishStatJ {@linkplain Description}s
     */
    private List<Description> getDescriptions(final FishStatJCollectionVO source)
    {
        final List<Description> descriptionList = new LinkedList<>();

        final Map<String, Element> sections = getSections(source);

        // retrieve interesting section text
        for (final String sectionTitle : languageVo.getValidDescriptionMap().keySet()) {
            final Element sectionBody = sections.get(sectionTitle);

            if (sectionBody != null) {
                final Description desc = new Description(
                    sectionBody.text().trim(),
                    languageVo.getValidDescriptionMap().get(sectionTitle),
                    languageVo.getApiName());
                descriptionList.add(desc);
            }
        }

        return descriptionList;
    }


    /**
     * Retrieves a {@linkplain List} of {@linkplain Rights} of the FishStatJ collection page.
     *
     * @param source the value object that is to be transformed to a document
     *
     * @return a {@linkplain List} of FishStatJ {@linkplain Rights}
     */
    private List<Rights> getRights(final FishStatJCollectionVO source)
    {
        final List<Rights> rightsList = new LinkedList<>();

        // check if one of the sections can be parsed as Rights
        final Element rightsSection = getSections(source).get(languageVo.getRightsSectionTitle());

        if (rightsSection != null) {
            final Element rightsLink = rightsSection.selectFirst(FishStatJSourceConstants.LINKS_SELECTION);
            final String rightsText = rightsSection.text().trim();
            final String rightsUrl = getUrlFromLink(rightsLink);

            rightsList.add(new Rights(rightsText, languageVo.getApiName(), rightsUrl));
        }

        return rightsList;
    }


    /**
     * Retrieves the logo and view {@linkplain WebLink}s of the FishStatJ collection.
     *
     * @param source the value object that is to be transformed to a document
     *
     * @return a {@linkplain List} of logo and view {@linkplain WebLink}s
     */
    private List<WebLink> getLogoAndViewWebLinks(final FishStatJCollectionVO source)
    {
        final WebLink viewLink = new WebLink(
            source.getCollectionUrl(),
            FishStatJDataCiteConstants.VIEW_URL_TITLE,
            WebLinkType.ViewURL);

        // search for the subsecti
        return Arrays.asList(viewLink, FishStatJDataCiteConstants.LOGO_LINK);
    }


    /**
     * Retrieves a {@linkplain List} of {@linkplain WebLink}s of the FishStatJ collection.
     *
     * @param source the value object that is to be transformed to a document
     *
     * @return a {@linkplain List} of FishStatJ {@linkplain WebLink}s
     */
    private List<WebLink> getSectionWebLinks(final FishStatJCollectionVO source)
    {
        // parse links from relevant section
        final List<WebLink> weblinks = new LinkedList<>();
        final Map<String, Element> sections = getSections(source);

        // parse dataset links
        final Element datasetSection = sections.get(languageVo.getWebLinksSectionTitle());

        if (datasetSection != null) {
            final Elements infoLinks = datasetSection.children().select(FishStatJSourceConstants.LINKS_AND_CAPTIONS_SELECTION);
            String titleAboveLink = "";

            for (final Element ele : infoLinks) {
                // if the element is a title before a link, store it
                if (ele.hasClass(FishStatJSourceConstants.CAPTION_CLASS))
                    titleAboveLink = ele.text().trim();
                else
                    weblinks.add(parseWebLink(ele, titleAboveLink));
            }
        }

        // parse other related web links
        for (final Entry<String, Element> section : sections.entrySet()) {
            if (!section.getKey().equals(languageVo.getWebLinksSectionTitle())) {
                final Elements infoLinks = section.getValue().select(FishStatJSourceConstants.LINKS_SELECTION);

                for (final Element ele : infoLinks)
                    weblinks.add(parseWebLink(ele, null));
            }
        }

        return weblinks;
    }


    /**
     * Retrieves a {@linkplain List} of {@linkplain ResearchData} of the FishStatJ collection.
     *
     * @param source the value object that is to be transformed to a document
     *
     * @return a {@linkplain List} of FishStatJ {@linkplain ResearchData}
     */
    private List<ResearchData> getResearchData(final FishStatJCollectionVO source)
    {
        // parse links from relevant section
        final List<ResearchData> downloads = new LinkedList<>();
        final Element linkSection = getSections(source).
                                    get(languageVo.getWebLinksSectionTitle());

        // if the section does not exist, there are no links
        if (linkSection == null)
            return downloads;

        final Elements infoLinks = linkSection.children().select(FishStatJSourceConstants.LINKS_SELECTION);

        for (final Element linkElement : infoLinks) {
            final String fileUrl = getUrlFromLink(linkElement);

            // if this is a downloadable file, skip the link
            final Matcher matcher = FishStatJSourceConstants.DOWNLOADABLE_FILE_PATTERN.matcher(fileUrl);

            if (matcher.find()) {
                final String fileName = matcher.group(1);
                final String fileExtension = linkElement.text().isEmpty() ? matcher.group(2) : linkElement.text();

                downloads.add(new ResearchData(fileUrl, fileName, fileExtension));
            }
        }

        return downloads;
    }


    /**
     * Retrieves side bar {@linkplain WebLink}s of the FishStatJ collection.
     *
     * @param source the value object that is to be transformed to a document
     *
     * @return a {@linkplain List} of side bar {@linkplain WebLink}s
     */
    private List<WebLink> getSideBarWebLinks(final FishStatJCollectionVO source)
    {
        final List<WebLink> weblinks = new LinkedList<>();

        // add side bar links
        for (final String sideBarTitle : languageVo.getSidebarTitles()) {
            final String sideBarSelection = String.format(
                                                FishStatJSourceConstants.CONTAINS_TEXT_SELECTION,
                                                sideBarTitle);
            final Element sideBar = source.getCollectionPage().selectFirst(sideBarSelection);

            if (sideBar != null) {
                final String sideBarUrl = String.format(
                                              FishStatJSourceConstants.SITE_URL,
                                              sideBar.attr(FishStatJSourceConstants.HREF_ATTRIBUTE)
                                          );
                weblinks.add(new WebLink(sideBarUrl, sideBar.text(), WebLinkType.Related));
            }
        }

        return weblinks;
    }


    /**
     * Retrieves a {@linkplain List} of {@linkplain Contributor}s of the FishStatJ collection.
     *
     * @param source the value object that is to be transformed to a document
     *
     * @return a {@linkplain List} of FishStatJ {@linkplain Contributor}s
     */
    private List<Contributor> getContributors(final FishStatJCollectionVO source)
    {
        final List<Contributor> contributorList = new LinkedList<>();

        if (source.getContactsPage() != null) {
            final Elements contactElements = source.getContactsPage().select(FishStatJSourceConstants.CONTACTS_AND_MAILS_SELECTION);

            String fullName = null;

            for (final Element item : contactElements) {

                // if the element is a div, it can be the name of the upcoming mailto link
                if (item.tagName().equals(FishStatJSourceConstants.DIV))
                    fullName = item.text().trim();

                // if the element is a mailto-link, we know the previous div is the contact's name
                else if (fullName != null) {

                    // the contact is an organisation
                    if (fullName.matches(FishStatJSourceConstants.ORGANISATION_NAME_REGEX)) {
                        contributorList.add(new Contributor(
                                                new PersonName(fullName, NameType.Organisational),
                                                ContributorType.ContactPerson));
                    }
                    // the contatct is a person
                    else {
                        // retrieve first- and last name and create a contact
                        final Matcher nameMatcher = FishStatJSourceConstants.PERSON_NAME_PATTERN.matcher(fullName);

                        if (nameMatcher.find()) {
                            // either groups 1&2 are matching or groups 3&4, two distinct notations of names, both of which are handled here
                            final String firstName = nameMatcher.group(1) == null ? nameMatcher.group(4) : nameMatcher.group(1);
                            final String lastName = nameMatcher.group(2) == null ? nameMatcher.group(3) : nameMatcher.group(2);

                            final Contributor contributor = new Contributor(
                                new PersonName(fullName, NameType.Personal),
                                ContributorType.ContactPerson);
                            contributor.setGivenName(firstName);
                            contributor.setFamilyName(lastName);
                            contributorList.add(contributor);
                        }
                    }
                }
            }
        }

        return contributorList;
    }


    /**
     * Retrieves a {@linkplain List} of {@linkplain Subject}s of the FishStatJ collection.
     *
     * @param source the value object that is to be transformed to a document
     *
     * @return a {@linkplain List} of FishStatJ {@linkplain Subject}s
     */
    private Collection<Subject> getSubjects(final FishStatJCollectionVO source)
    {
        final List<Subject> subjectList = new LinkedList<Subject>();

        final Element subjectSection = getSections(source).get(languageVo.getSubjectsSectionTitle());

        if (subjectSection != null) {
            final String[] usages = subjectSection.text().split(", ");
            final int len = usages.length;

            for (int i = 0; i < len; i++)
                subjectList.add(new Subject(usages[i], languageVo.getApiName()));
        }

        return subjectList;
    }





    /**
     * Retrieves a map of collection section titles to corresponding body elements.
     *
     * @param source the value object that is to be transformed to a document
     *
     * @return a map of collection section titles to corresponding body elements
     */
    private static Map<String, Element> getSections(final FishStatJCollectionVO source)
    {
        final Map<String, Element> map = new HashMap<>();
        final Elements subSections = source.getCollectionPage().select(FishStatJSourceConstants.ALL_SECTIONS_SELECTION);

        // disregard the first element, because it's uninteresting
        int i = 0;
        final int subSectionCount = subSections.size();

        while (i < subSectionCount) {
            final String sectionTitle = subSections.get(i++).text().trim();
            final Element sectionBody = subSections.get(i++);
            map.put(sectionTitle, sectionBody);
        }

        return map;
    }


    /**
     * Retrieves the href attribute from an a-tag and formats it properly
     * prior to returning it.
     *
     * @param linkElement the a-tag of which the link is to be retrieved
     *
     * @return a formatted URL or null, if the linkElement is empty or lacks a href attribute
     */
    private static String getUrlFromLink(final Element linkElement)
    {
        if (linkElement == null || !linkElement.hasAttr(FishStatJSourceConstants.HREF_ATTRIBUTE))
            return null;
        else
            return linkElement.attr(FishStatJSourceConstants.HREF_ATTRIBUTE)
                   .replaceAll(FishStatJSourceConstants.NEW_WINDOW_LINK_REGEX, FishStatJSourceConstants.NEW_WINDOW_LINK_REPLACE)
                   .replaceAll(FishStatJSourceConstants.RELATIVE_LINK_REGEX, FishStatJSourceConstants.RELATIVE_LINK_REPLACE);
    }


    /**
     * Parses a link element and generates a {@linkplain WebLink} from it.
     *
     * @param linkElement the a-tag of which the link is to be retrieved
     * @param alternativeTitle if the link has no text, this string is used instead
     *
     * @return a {@linkplain WebLink} or null, if the element is a download link
     */
    private WebLink parseWebLink(final Element linkElement, final String alternativeTitle)
    {
        final String url = getUrlFromLink(linkElement);

        // make sure the title is not a download link
        if (FishStatJSourceConstants.DOWNLOADABLE_FILE_PATTERN.matcher(url).matches())
            return null;

        // retrieve the title of the web link
        String title = linkElement.text().isEmpty() ? alternativeTitle : linkElement.text();

        // remove the "Click here" part of the title, if applicable
        if (title != null)
            title = title.replace(languageVo.getWebLinkTitleRegex(), FishStatJSourceConstants.CLICK_HERE_REPLACE);

        // determine the type of the web link
        final WebLinkType type;

        if (alternativeTitle != null && alternativeTitle.equals(languageVo.getDatasetSubTitle()))
            type = WebLinkType.SourceURL;
        else if (url.endsWith(FishStatJSourceConstants.GIF_EXTENSION))
            type = WebLinkType.ThumbnailURL;
        else
            type = WebLinkType.Related;

        return new WebLink(url, title, type);
    }


    @Override
    public void clear()
    {
        // remove temporarily unzipped files
        if(downloadFolder != null) {
            FileUtils.deleteFile(downloadFolder);
            downloadFolder = null;
        }
    }
}
