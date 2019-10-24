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


    /**
     * The original {@linkplain Document#equals(Object)} function does not
     * compare the actual content of the HTML objects. In order to compare
     * the {@linkplain FishStatJCollectionVO#collectionPage} and {@linkplain FishStatJCollectionVO#contactsPage},
     * the {@linkplain Document#hasSameValue(Object)} function is used instead..
     *
     * @param obj the object that is to be compared to the VO
     * @return true if the object is equal to this VO
     */
    @Override
    @SuppressWarnings("PMD.NPathComplexity")
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        final FishStatJCollectionVO other = (FishStatJCollectionVO) obj;

        if (collectionUrl == null) {
            if (other.collectionUrl != null)
                return false;
        } else if (!collectionUrl.equals(other.collectionUrl))
            return false;

        if (downloadFolder == null) {
            if (other.downloadFolder != null)
                return false;
        } else if (!downloadFolder.equals(other.downloadFolder))
            return false;

        if (collectionPage == null) {
            if (other.collectionPage != null)
                return false;
        } else if (!collectionPage.hasSameValue(other.collectionPage))
            return false;

        if (contactsPage == null) {
            if (other.contactsPage != null)
                return false;
        } else if (!contactsPage.hasSameValue(other.contactsPage))
            return false;

        return true;
    }


    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((collectionPage == null) ? 0 : collectionPage.hashCode());
        result = prime * result + ((collectionUrl == null) ? 0 : collectionUrl.hashCode());
        result = prime * result + ((contactsPage == null) ? 0 : contactsPage.hashCode());
        result = prime * result + ((downloadFolder == null) ? 0 : downloadFolder.hashCode());
        return result;
    }
}
