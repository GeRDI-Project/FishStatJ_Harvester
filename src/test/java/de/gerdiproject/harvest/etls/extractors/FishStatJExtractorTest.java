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

import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import de.gerdiproject.harvest.FishStatJContextListener;
import de.gerdiproject.harvest.application.ContextListener;
import de.gerdiproject.harvest.application.MainContextUtils;
import de.gerdiproject.harvest.etls.AbstractIteratorETL;
import de.gerdiproject.harvest.etls.FishStatJETL;
import de.gerdiproject.harvest.fishstatj.constants.FishStatJFileConstants;
import de.gerdiproject.harvest.utils.data.DiskIO;
import de.gerdiproject.harvest.utils.file.FileUtils;
import de.gerdiproject.json.GsonUtils;
import de.gerdiproject.json.datacite.DataCiteJson;

/**
 * This class provides Unit Tests for the {@linkplain FishStatJExtractor}.
 *
 * @author Robin Weiss
 */
public class FishStatJExtractorTest extends AbstractIteratorExtractorTest<FishStatJCollectionVO>
{
    private static final String COLLECTION_URL = "http://www.fao.org/fi/website/FIRetrieveAction.do?dom=collection&xml=mocked.xml&lang=en";

    private static final String MOCKED_RESPONSE_FOLDER = "mockedHttpResponses";
    private static final String CACHED_COLLECTION_URL = MOCKED_RESPONSE_FOLDER + "/www.fao.org/fi/website/FIRetrieveAction.do%query%/dom=collection&xml=mocked.xml&lang=en.response";
    private static final String CACHED_CONTACTS_URL = MOCKED_RESPONSE_FOLDER + "/www.fao.org/mocked/Contacts.response";
    private static final String MOCKED_ZIP_PATH = "mockedZip.zip";
    private static final String MOCKED_UNZIP_PATH = FishStatJFileConstants.UNZIP_FOLDER + "MockedDatasetCSV";


    private final DiskIO diskReader = new DiskIO(GsonUtils.createGerdiDocumentGsonBuilder().create(), StandardCharsets.UTF_8);


    @Override
    protected AbstractIteratorExtractor<FishStatJCollectionVO> setUpTestObjects()
    {
        // copy mocked zip file
        final File copiedZip = new File(MainContextUtils.getCacheDirectory(getClass()), MOCKED_ZIP_PATH);
        FileUtils.copyFile(getResource(MOCKED_ZIP_PATH), copiedZip);

        return super.setUpTestObjects();
    }


    @Override
    protected ContextListener getContextListener()
    {
        return new FishStatJContextListener();
    }


    @Override
    protected AbstractIteratorETL<FishStatJCollectionVO, DataCiteJson> getEtl()
    {
        return new FishStatJETL();
    }


    @Override
    protected File getConfigFile()
    {
        return getResource("config.json");
    }


    @Override
    protected File getMockedHttpResponseFolder()
    {
        return getResource(MOCKED_RESPONSE_FOLDER);
    }


    @Override
    protected FishStatJCollectionVO getExpectedOutput()
    {
        return new FishStatJCollectionVO(
                   COLLECTION_URL,
                   diskReader.getHtml(getResource(CACHED_COLLECTION_URL).toString()),
                   diskReader.getHtml(getResource(CACHED_CONTACTS_URL).toString()),
                   new File(MainContextUtils.getCacheDirectory(getClass()), MOCKED_UNZIP_PATH));
    }


    /**
     * Tests the unzip functionality of the {@linkplain FishStatJExtractor} by validating
     * that at least one file is extracted to an expected folder.
     */
    @Test
    public void testUnZipFileFromUrl()
    {
        // extract test object
        testedObject.extract().next();

        // check the folder in which the mocked zip file is supposed to be extracted to
        final File unzipFolder = new File(MainContextUtils.getCacheDirectory(getClass()), MOCKED_UNZIP_PATH);
        final File[] unzippedFiles = unzipFolder.listFiles();

        assertNotEquals("Expected at least one file to become unzipped!",
                        0,
                        unzippedFiles.length);
    }
}
