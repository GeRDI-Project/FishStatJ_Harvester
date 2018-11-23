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
package de.gerdiproject.harvest.etls;

import org.jsoup.nodes.Element;

import de.gerdiproject.harvest.config.Configuration;
import de.gerdiproject.harvest.config.parameters.StringParameter;
import de.gerdiproject.harvest.etls.extractors.FishStatJExtractor;
import de.gerdiproject.harvest.etls.transformers.FishStatJTransformer;
import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;
import de.gerdiproject.json.datacite.DataCiteJson;


/**
 * A harvester for FishStatJ (http://www.fao.org/fishery/statistics/collections/en).
 *
 * @author Bohdan Tkachuk
 */
public class FishStatJETL extends StaticIteratorETL<Element, DataCiteJson>
{
    private StringParameter languageParam;


    /**
     * Default Constructor that is called by the MainContext.
     */
    public FishStatJETL()
    {
        super(new FishStatJExtractor(), new FishStatJTransformer());
    }


    @Override
    protected void registerParameters()
    {
        super.registerParameters();

        this.languageParam = Configuration.registerParameter(
                                 new StringParameter(
                                     FishstatjParameterConstants.LANGUAGE_KEY,
                                     getName(),
                                     FishstatjParameterConstants.LANGUAGE_DEFAULT));
    }


    /**
     * Retrieves the language in which the documents are to be harvested.
     *
     * @return the language in which the documents are to be harvested
     */
    public String getLanguage()
    {
        return languageParam.getValue();
    }
}
