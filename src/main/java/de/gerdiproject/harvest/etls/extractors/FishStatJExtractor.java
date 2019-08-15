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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

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

/**
 * This {@linkplain AbstractIteratorExtractor} implementation retrieves FishStatJ collections
 * and gathers related web pages in order to create {@linkplain FishStatJCollectionVO}s.
 *
 * @author Robin Weiss
 */
public class FishStatJExtractor extends AbstractIteratorExtractor<FishStatJCollectionVO>
{
    protected static final Logger LOGGER = LoggerFactory.getLogger(FishStatJExtractor.class);

    protected final HttpRequester httpRequester = new HttpRequester();

    protected FishStatJLanguageVO languageVo;
    protected Iterator<Element> sourceIterator;
    private String version;
    private int fishStatJPageCount = -1;


    @Override
    public void init(final AbstractETL<?, ?> etl)
    {
        super.init(etl);
        this.languageVo = ((FishStatJETL) etl).getLanguageVO();
        this.httpRequester.setCharset(etl.getCharset());

        final String mainUrl = String.format(FishStatJSourceConstants.MAIN_PAGE_URL, languageVo.getApiName());
        final Document baseWebsite = httpRequester.getHtmlFromUrl(mainUrl);

        if (baseWebsite == null)
            throw new ETLPreconditionException(FishStatJSourceConstants.FISHSTAT_TIMEOUT_ERROR);

        final Elements fishStatJSources = baseWebsite.select(FishStatJSourceConstants.MAIN_PAGE_LINKS_SELECTION);

        this.fishStatJPageCount = fishStatJSources.size();

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
        return fishStatJPageCount;
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
        private Document getContactsPage(final Document collectionPage)
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
        private File downloadAndUnzipCollection(final Document collectionPage)
        {
            // look for a link-element for downloading a zip-file
            final Element zipLinkElement = collectionPage.selectFirst(FishStatJSourceConstants.ZIP_LINKS_SELECTION);

            if (zipLinkElement == null)
                return null;

            // retrieve URL from link-element
            final String zipUrl = zipLinkElement.attr(FishStatJSourceConstants.HREF_ATTRIBUTE);

            if (zipUrl == null)
                return null;

            // unzip file
            final File unzipFolder = new File(FishStatJFileConstants.UNZIP_FOLDER + zipLinkElement.text().replaceAll("\\W", ""));
            final boolean isUnzipped = unZipFileFromUrl(zipUrl, unzipFolder);

            if (!isUnzipped)
                return null;

            return unzipFolder;
        }


        /**
         * Reads a file stream from a URL that points to a zip file and extracts the content
         * to a specified folder.
         *
         * @param zipUrl a URL that points to a zip file
         * @param unzipFolder the folder to which the zip is to be extracted to
         *
         * @return true if the zip was extracted successfully
         */
        private boolean unZipFileFromUrl(final String zipUrl, final File unzipFolder)
        {
            // cleanup left over files
            FileUtils.deleteFile(unzipFolder);
            FileUtils.createDirectories(unzipFolder);

            final URL downloadUrl;

            try {
                downloadUrl = new URL(zipUrl);

            } catch (final MalformedURLException e) {
                LOGGER.error(String.format(FishStatJFileConstants.DOWNLOAD_ERROR, zipUrl), e);
                return false;
            }

            try
                (InputStream urlInputStream = downloadUrl.openStream();
                 ZipInputStream zipStream = new ZipInputStream(urlInputStream)) {

                // iterate through the files of the zip input stream
                while (true) {
                    final ZipEntry entry = zipStream.getNextEntry();

                    if (entry == null)
                        break;

                    // open streams for writing files to disk
                    try
                        (OutputStream fileOut = Files.newOutputStream(new File(unzipFolder, entry.getName()).toPath());
                         BufferedOutputStream bufferedFileOut = new BufferedOutputStream(fileOut, FishStatJFileConstants.ZIP_EXTRACT_BUFFER_SIZE)) {

                        // write the file from the stream to disk
                        final byte[] fileOutBuffer = new byte[FishStatJFileConstants.ZIP_EXTRACT_BUFFER_SIZE];

                        while (true) {
                            final int readByteCount = zipStream.read(fileOutBuffer, 0, fileOutBuffer.length);

                            if (readByteCount == -1)
                                break;
                            else
                                bufferedFileOut.write(fileOutBuffer, 0, readByteCount);
                        }

                        bufferedFileOut.flush();
                    }
                }

                return true;

            } catch (final ZipException e) {
                LOGGER.error(String.format(FishStatJFileConstants.UNZIP_ERROR, zipUrl), e);

            } catch (final IOException e) {
                LOGGER.error(String.format(FishStatJFileConstants.DOWNLOAD_ERROR, zipUrl), e);
            }

            return false;
        }
    }


    @Override
    public void clear()
    {
        // nothing to clean up

    }
}
