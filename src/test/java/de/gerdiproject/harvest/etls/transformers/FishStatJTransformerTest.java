/*
 *  Copyright © 2019 Robin Weiss (http://www.gerdi-project.de/)
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

import de.gerdiproject.harvest.FishStatJContextListener;
import de.gerdiproject.harvest.application.ContextListener;
import de.gerdiproject.harvest.application.MainContextUtils;
import de.gerdiproject.harvest.etls.AbstractIteratorETL;
import de.gerdiproject.harvest.etls.FishStatJETL;
import de.gerdiproject.harvest.etls.extractors.FishStatJCollectionVO;
import de.gerdiproject.harvest.fishstatj.constants.FishStatJFileConstants;
import de.gerdiproject.harvest.utils.data.DiskIO;
import de.gerdiproject.harvest.utils.data.constants.DataOperationConstants;
import de.gerdiproject.harvest.utils.file.FileUtils;
import de.gerdiproject.json.GsonUtils;
import de.gerdiproject.json.datacite.DataCiteJson;

/**
 * This class provides Unit Tests for the {@linkplain FishStatJTransformer}.
 *
 * @author Robin Weiss
 */
public class FishStatJTransformerTest extends AbstractIteratorTransformerTest<FishStatJCollectionVO, DataCiteJson>
{
    final DiskIO diskReader = new DiskIO(GsonUtils.createGerdiDocumentGsonBuilder().create(), StandardCharsets.UTF_8);

    private static final String MOCKED_RESPONSE_FOLDER = "mockedHttpResponses";
    private static final String CACHED_COLLECTION_URL = MOCKED_RESPONSE_FOLDER + "/collection.html";
    private static final String CACHED_CONTACTS_URL = MOCKED_RESPONSE_FOLDER + "/contacts.html";
    private static final String MOCKED_ZIP_FOLDER_PATH = "mockedUnzipFolder";


    @Override
    protected AbstractIteratorETL<FishStatJCollectionVO, DataCiteJson> getEtl()
    {
        return new FishStatJETL();
    }


    @Override
    protected AbstractIteratorTransformer<FishStatJCollectionVO, DataCiteJson> setUpTestObjects()
    {
        // copy mocked HTTP responses to the cache folder to drastically speed up the testing
        final File httpCacheFolder = new File(
            MainContextUtils.getCacheDirectory(FishStatJTransformerTest.class),
            DataOperationConstants.CACHE_FOLDER_PATH);
        FileUtils.copyFile(getResource(MOCKED_RESPONSE_FOLDER), httpCacheFolder);

        // copy mocked zip file content
        FileUtils.copyFile(getResource(MOCKED_ZIP_FOLDER_PATH), getTemporaryUnzipFolder());

        return super.setUpTestObjects();
    }


    @Override
    protected FishStatJCollectionVO getMockedInput()
    {
        return new FishStatJCollectionVO(
                   "http://www.mock.ed/collection",
                   diskReader.getHtml(getResource(CACHED_COLLECTION_URL).toString()),
                   diskReader.getHtml(getResource(CACHED_CONTACTS_URL).toString()),
                   getTemporaryUnzipFolder());
    }


    @Override
    protected DataCiteJson getExpectedOutput()
    {
        return diskReader.getObject(getResource("output.json"), DataCiteJson.class);
    }


    @Override
    protected ContextListener getContextListener()
    {
        return new FishStatJContextListener();
    }


    private File getTemporaryUnzipFolder()
    {
        return new File(
                   MainContextUtils.getCacheDirectory(FishStatJTransformerTest.class),
                   FishStatJFileConstants.UNZIP_FOLDER);
    }
}
