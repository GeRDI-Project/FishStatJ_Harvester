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

import org.jsoup.nodes.Document;

import de.gerdiproject.json.datacite.DataCiteJson;
import lombok.Value;

/**
 * This value object contains extracted FishStatJ data which can be used
 * to generate a {@linkplain DataCiteJson} object.
 *
 * @author Robin Weiss
 */
@Value
public class FishStatJCollectionVO
{
    private final String collectionUrl;
    private final Document collectionPage;
    private final Document contactsPage;
    private final File downloadFolder;
}
