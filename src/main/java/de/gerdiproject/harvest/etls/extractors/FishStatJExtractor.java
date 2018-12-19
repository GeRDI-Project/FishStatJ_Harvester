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
package de.gerdiproject.harvest.etls.extractors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.etls.ETLPreconditionException;
import de.gerdiproject.harvest.etls.FishStatJETL;
import de.gerdiproject.harvest.etls.FishStatJLanguageVO;
import de.gerdiproject.harvest.fishstatj.constants.FishStatJFileConstants;
import de.gerdiproject.harvest.fishstatj.constants.FishStatJSourceConstants;
import de.gerdiproject.harvest.utils.data.HttpRequester;
import de.gerdiproject.harvest.utils.file.FileUtils;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * This {@linkplain AbstractIteratorExtractor} implementation retrieves FishStatJ collections
 * and gathers related web pages in order to create {@linkplain FishStatJCollectionVO}s.
 *
 * @author Robin Weiss
 */
public class FishStatJExtractor extends AbstractIteratorExtractor<FishStatJCollectionVO>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FishStatJExtractor.class);

    private final HttpRequester httpRequester = new HttpRequester();

    private FishStatJLanguageVO languageVo;
    private Iterator<Element> sourceIterator;
    private String version = null;
    private int size = -1;


    @Override
    public void init(AbstractETL<?, ?> etl)
    {
        super.init(etl);
        this.languageVo = ((FishStatJETL) etl).getLanguageVO();
        this.httpRequester.setCharset(etl.getCharset());

        final String mainUrl = String.format(FishStatJSourceConstants.MAIN_PAGE_URL, languageVo.getApiName());
        final Document baseWebsite = httpRequester.getHtmlFromUrl(mainUrl);

        if (baseWebsite == null)
            throw new ETLPreconditionException(FishStatJSourceConstants.FISHSTAT_TIMEOUT_ERROR);

        final Elements fishStatJSources = baseWebsite.select(FishStatJSourceConstants.MAIN_PAGE_LINKS_SELECTION);

        this.size = fishStatJSources.size();

        // TODO replace with something better
        this.version = String.valueOf(fishStatJSources.size());

        this.sourceIterator = fishStatJSources.iterator();
    }


    @Override
    public String getUniqueVersionString()
    {
        return version;
    }


    @Override
    public int size()
    {
        return size;
    }


    @Override
    protected Iterator<FishStatJCollectionVO> extractAll() throws ExtractorException
    {
        return new FishStatJIterator();
    }


    /**
     * This {@linkplain Iterator} iterates through FishStatJ collections and
     * generates a {@linkplain FishStatJCollectionVO} for each of them.
     *
     * @author Robin Weiss
     */
    private class FishStatJIterator implements Iterator<FishStatJCollectionVO>
    {
        @Override
        public boolean hasNext()
        {
            return sourceIterator.hasNext();
        }

        @Override
        public FishStatJCollectionVO next()
        {
            final Element nextSource = sourceIterator.next();
            final String url = nextSource.attr(FishStatJSourceConstants.HREF_ATTRIBUTE);
            final Document collectionPage = httpRequester.getHtmlFromUrl(url);

            if (collectionPage != null && collectionPage.hasText())
                return new FishStatJCollectionVO(
                           url,
                           collectionPage,
                           getContactsPage(collectionPage),
                           downloadAndUnzipCollection(collectionPage));
            else
                return null;
        }


        /**
         * Retrieves a web page that contains contact information regarding a collection.
         *
         * @param collectionPage the main web page of the FishStatJ collection
         *
         * @return a web page concerning contact information of a collection
         */
        private Document getContactsPage(Document collectionPage)
        {
            // find the "Contact" element on the web page
            final String contactsSelection = String.format(
                                                 FishStatJSourceConstants.CONTAINS_TEXT_SELECTION,
                                                 languageVo.getContactsTabTitle());
            final Element contactsLink = collectionPage.selectFirst(contactsSelection);

            if (contactsLink == null)
                return null;

            final String contactsUrl = String.format(
                                           FishStatJSourceConstants.SITE_URL,
                                           contactsLink.attr(FishStatJSourceConstants.HREF_ATTRIBUTE));

            return httpRequester.getHtmlFromUrl(contactsUrl);
        }


        /**
         * Checks if the collection web page offers a zip file download, and downloads
         * and deflates the zip file if available.
         *
         * @param collectionPage the main web page of the FishStatJ collection
         *
         * @return a local directory containing the deflated zip archive, or null if there
         *          were problems downloading or deflating the archive
         */
        private File downloadAndUnzipCollection(Document collectionPage)
        {
            // look for a link-element for downloading a zip-file
            final Element zipLinkElement = collectionPage.selectFirst(FishStatJSourceConstants.ZIP_LINKS_SELECTION);

            if (zipLinkElement == null)
                return null;

            // retrieve URL from link-element
            final String zipUrl = zipLinkElement.attr(FishStatJSourceConstants.HREF_ATTRIBUTE);

            if (zipUrl == null)
                return null;

            // download zip file
            final boolean isDownloaded = downloadZipFromUrl(zipUrl, FishStatJFileConstants.DOWNLOADED_ZIP_FILE);

            if (!isDownloaded)
                return null;

            // unzip file
            final File unzipFolder = new File(FishStatJFileConstants.UNZIP_FOLDER + zipLinkElement.text().replaceAll("\\W", ""));
            final boolean isUnzipped = unZip(FishStatJFileConstants.DOWNLOADED_ZIP_FILE, unzipFolder);

            if (!isUnzipped)
                return null;

            return unzipFolder;
        }

        /**
         * Downloads a zip file to a specified path.
         *
         * @param downloadLink the URL that points to a zip file
         * @param destination a local zip file path
         *
         * @return true if the download was successful
         */
        private boolean downloadZipFromUrl(String downloadLink, File destination)
        {
            FileUtils.deleteFile(destination);

            try {
                final URL downloadUrl = new URL(downloadLink);

                try
                    (FileOutputStream fileOutputStream = new FileOutputStream(destination)) {
                    final ReadableByteChannel readableByteChannel = Channels.newChannel(downloadUrl.openStream());
                    fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                }
            } catch (IOException e) {
                LOGGER.error(String.format(FishStatJFileConstants.DOWNLOAD_ERROR, downloadLink), e);
                return false;
            }

            return true;
        }


        /**
         * Attempts to extract the content of a zip file to a specified folder.
         *
         * @param zipFile the zip file that is to be extracted
         * @param unzipFolder the folder to which the files are to be extracted to
         *
         * @return true if the extraction was successful
         */
        private boolean unZip(File zipFile, File unzipFolder)
        {
            // cleanup left over files
            FileUtils.deleteFile(unzipFolder);
            FileUtils.createDirectories(unzipFolder);

            try {
                new ZipFile(zipFile).extractAll(unzipFolder.toString());
                return true;
            } catch (ZipException e) {
                LOGGER.error(String.format(FishStatJFileConstants.UNZIP_ERROR, zipFile.toString()), e);
                FileUtils.deleteFile(unzipFolder);
                return false;
            }
        }
    }
}
