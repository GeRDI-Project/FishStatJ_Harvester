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
import java.util.Set;
import java.util.regex.Matcher;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.etls.FishStatJETL;
import de.gerdiproject.harvest.etls.extractors.FishStatJCollectionVO;
import de.gerdiproject.harvest.fishstatj.constants.FishStatJDataCiteConstants;
import de.gerdiproject.harvest.fishstatj.constants.FishStatJFileConstants;
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
import de.gerdiproject.json.datacite.enums.DateType;
import de.gerdiproject.json.datacite.enums.DescriptionType;
import de.gerdiproject.json.datacite.enums.TitleType;
import de.gerdiproject.json.datacite.extension.ResearchData;
import de.gerdiproject.json.datacite.extension.WebLink;
import de.gerdiproject.json.datacite.extension.enums.WebLinkType;

/**
 * This {@linkplain AbstractIteratorTransformer} implementation transforms FishStatJ collections to
 * {@linkplain DataCiteJson} objects.

 * @author Robin Weiss, Bohdan Tkachuk
 */
public class FishStatJTransformer extends AbstractIteratorTransformer<FishStatJCollectionVO, DataCiteJson>
{
    private final DiskIO diskReader = new DiskIO(new Gson(), StandardCharsets.UTF_8);

    private String language;


    @Override
    public void init(AbstractETL<?, ?> etl)
    {
        super.init(etl);
        this.language = ((FishStatJETL) etl).getLanguage();
    }


    @Override
    protected DataCiteJson transformElement(FishStatJCollectionVO source) throws TransformerException
    {
        final DataCiteJson document = new DataCiteJson(source.getCollectionUrl());
        document.setLanguage(language);

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
        titleList.add(new Title(titleText));

        // add category as sub-title
        final String categoryText = source.getCollectionPage()
                                    .selectFirst(FishStatJSourceConstants.SUB_TITLE_SELECTION)
                                    .text();
        final Title categoryTitle = new Title(categoryText);
        categoryTitle.setType(TitleType.Subtitle);
        titleList.add(categoryTitle);

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
        for (String sectionTitle : FishStatJSourceConstants.VALID_DESCRIPTIONS) {
            final Element sectionBody = sections.get(sectionTitle);

            if (sectionBody != null)
                descriptionList.add(new Description(sectionBody.text().trim(), DescriptionType.Abstract));
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
        final Element rightsSection = getSections(source).get(FishStatJSourceConstants.SECTION_TITLE_CONTAINING_RIGHTS);

        if (rightsSection != null) {
            final Element rightsLink = rightsSection.selectFirst(FishStatJSourceConstants.LINKS_SELECTION);
            final String rightsUrl = getUrlFromLink(rightsLink);

            final Rights rights = new Rights(rightsSection.text().trim());
            rights.setURI(rightsUrl);
            rightsList.add(rights);
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
        final WebLink viewLink = new WebLink(source.getCollectionUrl());
        viewLink.setName(FishStatJDataCiteConstants.VIEW_URL_TITLE);
        viewLink.setType(WebLinkType.ViewURL);

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
        final Element linkSection = getSections(source)
                                    .get(FishStatJSourceConstants.SECTION_TITLE_CONTAINING_LINKS);

        if (linkSection == null)
            return weblinks;

        final Elements infoLinks = linkSection.children().select(FishStatJSourceConstants.LINKS_AND_CAPTIONS_SELECTION);

        int i = 0;
        int len = infoLinks.size();

        Element titleElement = null;

        while (i < len) {
            final Element ele = infoLinks.get(i++);

            if (ele.hasClass("subtitle")) {
                titleElement = ele;
                continue;
            }

            final Element linkElement = ele;
            final String linkUrl = getUrlFromLink(linkElement);

            // if this is a downloadable file, skip the link
            if (FishStatJSourceConstants.DOWNLOADABLE_FILE_PATTERN.matcher(linkUrl).matches())
                continue;

            final String linkName;

            if (linkElement.text().isEmpty() && titleElement != null)
                linkName = titleElement.text();
            else
                linkName = linkElement.text();

            final WebLinkType linkType = linkName.equals(FishStatJSourceConstants.PUBLICATION_TITLE)
                                         ?  WebLinkType.Related
                                         : WebLinkType.SourceURL;

            final WebLink textLink = new WebLink(linkUrl);
            textLink.setName(linkName);
            textLink.setType(linkType);
            weblinks.add(textLink);
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
                                    get(FishStatJSourceConstants.SECTION_TITLE_CONTAINING_LINKS);

        // if the section does not exist, there are no links
        if (linkSection == null)
            return downloads;

        final Elements infoLinks = linkSection.children().select(FishStatJSourceConstants.LINKS_SELECTION);

        int i = 0;
        int len = infoLinks.size();

        while (i < len) {
            // skip the title
            final Element linkElement = infoLinks.get(i++);
            final String fileUrl = getUrlFromLink(linkElement);

            // if this is a downloadable file, skip the link
            final Matcher matcher = FishStatJSourceConstants.DOWNLOADABLE_FILE_PATTERN.matcher(fileUrl);

            if (matcher.find()) {
                final String fileName = matcher.group(1);
                final String fileExtension = linkElement.text().isEmpty() ? matcher.group(2) : linkElement.text();

                final ResearchData download = new ResearchData(fileUrl, fileName);
                download.setType(fileExtension);
                downloads.add(download);
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
        for (String sideBarSelection : FishStatJSourceConstants.SIDEBAR_SELECTIONS) {
            final Element sideBar = source.getCollectionPage().selectFirst(sideBarSelection);

            if (sideBar != null) {
                final String sideBarUrl = String.format(
                                              FishStatJSourceConstants.SITE_URL,
                                              sideBar.attr(FishStatJSourceConstants.HREF_ATTRIBUTE)
                                          );
                final WebLink sideBarLink = new WebLink(sideBarUrl);
                sideBarLink.setName(sideBar.text());
                sideBarLink.setType(WebLinkType.Related);
                weblinks.add(sideBarLink);
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
            final Elements contactElements = source.getContactsPage().select(FishStatJSourceConstants.CONTACT_SELECTION);

            for (Element item : contactElements) {
                final String contributorName = item.text().trim();
                final Contributor contributor = FishStatJSourceConstants.VALID_CONTRIBUTOR_MAP.get(contributorName);
                contributorList.add(contributor);
            }
        }

        return contributorList;
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

                rightsList.add(new Rights(
                                   text.substring(
                                       subStringFrom + FishStatJFileConstants.RIGHTS_TEXT_EXCLUDED_PREFIX.length(),
                                       subStringTo)));
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
        final Set<Subject> subjectList = new HashSet<Subject>();

        final File[] csvFiles = unzippedFolder.listFiles(FishStatJFileConstants.CSV_FILE_FILTER);

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
                            column.forEach((String element) -> subjectList.add(new Subject(element)));
                    }
                }
            }
        }

        return  subjectList;
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
    private Map<String, Element> getSections(FishStatJCollectionVO source)
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
     * @param linkElement the a-element of which the link is to be retrieved
     *
     * @return a formatted URL or null, if the linkElement is empty or lacks a href attribute
     */
    private String getUrlFromLink(Element linkElement)
    {
        if (linkElement == null || !linkElement.hasAttr(FishStatJSourceConstants.HREF_ATTRIBUTE))
            return null;
        else
            return linkElement.attr(FishStatJSourceConstants.HREF_ATTRIBUTE)
                   .replaceAll(FishStatJSourceConstants.LINK_REGEX,
                               FishStatJSourceConstants.LINK_REGEX_REPLACE);
    }
}
