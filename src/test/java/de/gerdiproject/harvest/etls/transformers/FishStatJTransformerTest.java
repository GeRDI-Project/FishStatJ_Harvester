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

import java.nio.charset.StandardCharsets;
import java.util.Map;

import de.gerdiproject.harvest.AbstractIteratorTransformerTest;
import de.gerdiproject.harvest.FishStatJContextListener;
import de.gerdiproject.harvest.application.ContextListener;
import de.gerdiproject.harvest.etls.FishStatJETL;
import de.gerdiproject.harvest.etls.extractors.FishStatJCollectionVO;
import de.gerdiproject.harvest.utils.data.DiskIO;
import de.gerdiproject.json.GsonUtils;
import de.gerdiproject.json.datacite.DataCiteJson;

/**
 * This class provides Unit Tests for the {@linkplain FaoStatTransformer}.
 *
 * @author Robin Weiss
 */
public class FishStatJTransformerTest extends AbstractIteratorTransformerTest<FishStatJTransformer, FishStatJCollectionVO, DataCiteJson>
{
    final DiskIO diskReader = new DiskIO(GsonUtils.createGerdiDocumentGsonBuilder().create(), StandardCharsets.UTF_8);


    /**
     * Default Test Constructor.
     */
    public FishStatJTransformerTest()
    {
        super(new FishStatJETL(), new FishStatJTransformer());
    }


    @Override
    protected Map<String, String> getParameterValues()
    {
        return null;
    }


    @Override
    protected ContextListener getContextListener()
    {
        return new FishStatJContextListener();
    }


    @Override
    protected FishStatJCollectionVO getMockedInput()
    {
        return new FishStatJCollectionVO(
                "http://www.mock.ed/collection", 
                diskReader.getHtml(getResource("mockedHttpResponses/collection.html").toString()), 
                diskReader.getHtml(getResource("mockedHttpResponses/contacts.html").toString()),
                getResource("mockedDownloads"));
    }


    @Override
    protected DataCiteJson getExpectedOutput()
    {
        return diskReader.getObject(getResource("output.json"), DataCiteJson.class);
    }
}
