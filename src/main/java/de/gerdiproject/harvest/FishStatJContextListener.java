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
package de.gerdiproject.harvest;

import de.gerdiproject.harvest.config.parameters.AbstractParameter;
import de.gerdiproject.harvest.config.parameters.StringParameter;
import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;
import de.gerdiproject.harvest.harvester.FishStatJHarvester;

import java.util.Arrays;
import java.util.List;

import javax.servlet.annotation.WebListener;

/**
 * This class initializes the {@linkplain FishStatJHarvester} and all required classes.
 *
 * @author Robin Weiss
 */
@WebListener
public class FishStatJContextListener extends ContextListener<FishStatJHarvester>
{

	@Override
	protected List<AbstractParameter<?>> getHarvesterSpecificParameters() {
		StringParameter versionParam = new StringParameter(FishstatjParameterConstants.VERSION_KEY, FishstatjParameterConstants.VERSION_DEFAULT);
        StringParameter languageParam = new StringParameter(FishstatjParameterConstants.LANGUAGE_KEY, FishstatjParameterConstants.LANGUAGE_DEFAULT);

        return Arrays.asList(versionParam, languageParam);
	}
	
}
