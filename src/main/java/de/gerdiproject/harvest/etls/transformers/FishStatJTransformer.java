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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.etls.FishStatJETL;
import de.gerdiproject.harvest.etls.FishStatJLanguageVO;
import de.gerdiproject.harvest.etls.extractors.FishStatJCollectionVO;
import de.gerdiproject.harvest.fishstatj.constants.FishStatJDataCiteConstants;
import de.gerdiproject.harvest.fishstatj.constants.FishStatJFileConstants;
import de.gerdiproject.harvest.fishstatj.constants.FishStatJLanguageConstants;
import de.gerdiproject.harvest.fishstatj.constants.FishStatJSourceConstants;
import de.gerdiproject.harvest.fishstatj.utils.CsvUtils;
import de.gerdiproject.harvest.utils.data.DiskIO;
import de.gerdiproject.harvest.utils.file.FileUtils;
import de.gerdiproject.json.datacite.Contributor;
import de.gerdiproject.json.datacite.DataCiteJson;
import de.gerdiproject.json.datacite.Date;
import de.gerdiproject.json.datacite.Description;
import de.gerdiproject.json.datacite.Rights;
import de.gerdiproject.json.datacite.Subject;
import de.gerdiproject.json.datacite.Title;
import de.gerdiproject.json.datacite.abstr.AbstractDate;
import de.gerdiproject.json.datacite.enums.ContributorType;
import de.gerdiproject.json.datacite.enums.DateType;
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
    private final DiskIO diskReader = new DiskIO(new Gson(), StandardCharsets.UTF_8);

    private FishStatJLanguageVO languageVo;


    @Override
    public void init(AbstractETL<?, ?> etl)
    {
        super.init(etl);
        this.languageVo = ((FishStatJETL) etl).getLanguageVO();
    }


    @Override
    protected DataCiteJson transformElement(FishStatJCollectionVO source) throws TransformerException
    {
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
        final File downloadFolder = source.getDownloadedFiles();

        if (downloadFolder != null) {
            document.addRights(getRightsFromDownloadedFiles(downloadFolder));
            document.addDates(getDatesFromDownloadedFiles(downloadFolder));
            document.addSubjects(getSubjectsFromDownloadedFiles(downloadFolder));

            // clean up unzipped files
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
    private List<Title> getTitles(FishStatJCollectionVO source)
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
    private List<Description> getDescriptions(FishStatJCollectionVO source)
    {
        final List<Description> descriptionList = new LinkedList<>();

        final Map<String, Element> sections = getSections(source);

        // retrieve interesting section text
        for (String sectionTitle : languageVo.getValidDescriptionMap().keySet()) {
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
    private List<Rights> getRights(FishStatJCollectionVO source)
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
    private List<WebLink> getLogoAndViewWebLinks(FishStatJCollectionVO source)
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
    private List<WebLink> getSectionWebLinks(FishStatJCollectionVO source)
    {
        // parse links from relevant section
        final List<WebLink> weblinks = new LinkedList<>();
        final Map<String, Element> sections = getSections(source);

        // parse dataset links
        final Element datasetSection = sections.get(languageVo.getWebLinksSectionTitle());

        if (datasetSection != null) {
            final Elements infoLinks = datasetSection.children().select(FishStatJSourceConstants.LINKS_AND_CAPTIONS_SELECTION);
            String titleAboveLink = "";

            for (Element ele : infoLinks) {
                // if the element is a title before a link, store it
                if (ele.hasClass(FishStatJSourceConstants.CAPTION_CLASS))
                    titleAboveLink = ele.text().trim();
                else {
                    final WebLink link = parseWebLink(ele, titleAboveLink);

                    if (link != null) {
                        if (titleAboveLink.equals(languageVo.getDatasetSubTitle()))
                            link.setType(WebLinkType.SourceURL);

                        weblinks.add(link);
                    }
                }
            }
        }

        // parse other related web links
        for (Entry<String, Element> section : sections.entrySet()) {
            if (!section.getKey().equals(languageVo.getWebLinksSectionTitle())) {
                final Elements infoLinks = section.getValue().select(FishStatJSourceConstants.LINKS_SELECTION);

                for (Element ele : infoLinks)
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
    private List<ResearchData> getResearchData(FishStatJCollectionVO source)
    {
        // parse links from relevant section
        final List<ResearchData> downloads = new LinkedList<>();
        final Element linkSection = getSections(source).
                                    get(languageVo.getWebLinksSectionTitle());

        // if the section does not exist, there are no links
        if (linkSection == null)
            return downloads;

        final Elements infoLinks = linkSection.children().select(FishStatJSourceConstants.LINKS_SELECTION);

        for (Element linkElement : infoLinks) {
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
    private List<WebLink> getSideBarWebLinks(FishStatJCollectionVO source)
    {
        final List<WebLink> weblinks = new LinkedList<>();

        // add side bar links
        for (String sideBarTitle : languageVo.getSidebarTitles()) {
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
    private List<Contributor> getContributors(FishStatJCollectionVO source)
    {
        final List<Contributor> contributorList = new LinkedList<>();

        if (source.getContactsPage() != null) {
            final Elements contactElements = source.getContactsPage().select(FishStatJSourceConstants.CONTACTS_AND_MAILS_SELECTION);

            String fullName = null;

            for (Element item : contactElements) {

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
                            final String firstName = nameMatcher.group(1) != null ? nameMatcher.group(1) : nameMatcher.group(4);
                            final String lastName = nameMatcher.group(2) != null ? nameMatcher.group(2) : nameMatcher.group(3);

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
    private Collection<Subject> getSubjects(FishStatJCollectionVO source)
    {
        final List<Subject> subjectList = new LinkedList<Subject>();

        final Element subjectSection = getSections(source).get(languageVo.getSubjectsSectionTitle());

        if (subjectSection != null) {
            final String[] usages = subjectSection.text().split(", ");

            for (int i = 0, len = usages.length; i < len; i++)
                subjectList.add(new Subject(usages[i], languageVo.getApiName()));
        }

        return subjectList;
    }


    /**
     * Reads the Notes.txt from the downloaded collection archive and extracts
     * copyright information.
     *
     * @param unzippedFolder the folder containing unzipped collection archive files
     *
     * @return a {@linkplain List} of {@linkplain Rights}
     */
    private List<Rights> getRightsFromDownloadedFiles(File unzippedFolder)
    {
        final List<Rights> rightsList = new LinkedList<>();

        // prepare file reader
        final File readmeFile = new File(unzippedFolder, FishStatJFileConstants.README_FILE_NAME);

        if (readmeFile.exists()) {
            // read file
            final String text = new DiskIO(new Gson(), StandardCharsets.UTF_8).getString(readmeFile);
            final int subStringFrom = text.indexOf(FishStatJFileConstants.RIGHTS_TEXT_EXCLUDED_PREFIX);

            // check if the file contains the required text area
            if (subStringFrom != -1) {
                final int subStringTo = text.indexOf(FishStatJFileConstants.RIGHTS_TEXT_EXCLUDED_SUFFIX, subStringFrom);
                final String rightsText = text.substring(
                                              subStringFrom + FishStatJFileConstants.RIGHTS_TEXT_EXCLUDED_PREFIX.length(),
                                              subStringTo);

                // this file is always in English
                rightsList.add(new Rights(rightsText, FishStatJLanguageConstants.ENGLISH_API_NAME));
            }
        }

        return rightsList;
    }


    /**
     * Reads csv files from the downloaded collection archive and extracts
     * metadata from them.
     *
     * @param unzippedFolder the folder containing unzipped collection archive files
     *
     * @return a {@linkplain List} of {@linkplain Subject}s
     */
    private Collection<Subject> getSubjectsFromDownloadedFiles(File unzippedFolder)
    {
        final Set<Subject> subjectSet = new HashSet<Subject>();

        final File[] csvFiles = unzippedFolder.listFiles(FishStatJFileConstants.CSV_FILE_FILTER);

        if (csvFiles != null) {

            for (int i = 0, len = csvFiles.length; i < len; i++) {

                // calculate column shift
                final int headerShift = csvFiles[i].getName().contains(FishStatJFileConstants.CSV_FILE_WITH_SHIFTED_HEADER) ? 1 : 0;

                // retrieve title row
                final List<String> titleRow = CsvUtils.getRow(0, csvFiles[i], StandardCharsets.UTF_8);

                if (titleRow != null) {

                    // retrieve interesting columns
                    for (String colTitle : FishStatJSourceConstants.VALID_SUBJECTS) {
                        final int columnIndex = titleRow.indexOf(colTitle) + headerShift;

                        if (columnIndex != -1) {
                            final List<String> column = CsvUtils.getColumn(columnIndex, csvFiles[i], StandardCharsets.UTF_8);

                            if (column != null)
                                column.forEach((String element) -> subjectSet.add(new Subject(element)));
                        }
                    }
                }
            }
        }

        return  subjectSet;
    }


    /**
     * Reads the Notes.txt from the downloaded collection archive and extracts
     * date information.
     *
     * @param unzippedFolder the folder containing unzipped collection archive files
     *
     * @return a {@linkplain List} of {@linkplain AbstractDate}s
     */
    private List<AbstractDate> getDatesFromDownloadedFiles(File unzippedFolder)
    {
        final List<AbstractDate> dateList = new LinkedList<>();

        // prepare file reader
        final File readmeFile = new File(unzippedFolder, FishStatJFileConstants.README_FILE_NAME);

        if (readmeFile.exists()) {
            // read file
            final String text = diskReader.getString(readmeFile);
            final int subStringFrom = text.indexOf(FishStatJFileConstants.DATES_TEXT_EXCLUDED_PREFIX);

            // check if the file contains the required text area
            if (subStringFrom != -1) {
                final int subStringTo = text.indexOf(FishStatJFileConstants.DATES_TEXT_EXCLUDED_SUFFIX, subStringFrom);

                final String[] dateTextLines = text
                                               .substring(subStringFrom + FishStatJFileConstants.DATES_TEXT_EXCLUDED_PREFIX.length(), subStringTo)
                                               .split("\n");

                for (int i = 0, len = dateTextLines.length; i < len; i++) {
                    final String[] dateInfo = dateTextLines[i].split("  ");
                    final String version = dateInfo[0];
                    final String dateString = dateInfo[1];
                    final String description = dateInfo[2];

                    final String dateInformation = String.format(FishStatJDataCiteConstants.DATE_INFORMATION, version, description);
                    final DateType dateType = description.contains(FishStatJFileConstants.ISSUED_DATE_KEYWORD)
                                              ? DateType.Issued
                                              : DateType.Updated;

                    final Date date = new Date(dateString, dateType);
                    date.setDateInformation(dateInformation);
                    dateList.add(date);
                }
            }
        }

        return dateList;
    }


    /**
     * Retrieves a map of collection section titles to corresponding body elements.
     *
     * @param source the value object that is to be transformed to a document
     *
     * @return a map of collection section titles to corresponding body elements
     */
    private static Map<String, Element> getSections(FishStatJCollectionVO source)
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
    private static String getUrlFromLink(Element linkElement)
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
    private WebLink parseWebLink(Element linkElement, String alternativeTitle)
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
        final WebLinkType type = url.endsWith(FishStatJSourceConstants.GIF_EXTENSION)
                                 ? WebLinkType.ThumbnailURL
                                 : WebLinkType.Related;

        return new WebLink(url, title, type);
    }
}
