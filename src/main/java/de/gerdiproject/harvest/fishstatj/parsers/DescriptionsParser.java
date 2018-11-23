/**
 * Copyright © 2017 Bohdan Tkachuk (http://www.gerdi-project.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.gerdiproject.harvest.fishstatj.parsers;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;
import de.gerdiproject.harvest.utils.data.HttpRequester;
import de.gerdiproject.json.datacite.Description;
import de.gerdiproject.json.datacite.enums.DescriptionType;

public class DescriptionsParser
{

    private HttpRequester httpRequester;

    public DescriptionsParser()
    {
        httpRequester = new HttpRequester(new Gson(), StandardCharsets.UTF_8);
    }
    public List<Description> descriptionParser(String url)
    {
        List<Description> descriptions = new LinkedList<>();
        Document doc = httpRequester.getHtmlFromUrl(url);
        //choose from webpage element with class allWidth and all his children
        Elements descriptionNames = doc.select("#allWidth").first().children();
        //define previousTextOfItem as zero because for first entry it will be empty
        String previousTextOfItem = "";

        for (Element item : descriptionNames) {
            //compare previousTextOfItem with all text that define description, if true, add to the description text after previousTextOfItem

            for (String validDescription : FishstatjParameterConstants.VALID_DESCRIPTION) {
                if (previousTextOfItem.contains(validDescription)) {
                    Description description = new Description(String.format(FishstatjParameterConstants.DESCRIPTION_FORMAT, previousTextOfItem, item.text()), DescriptionType.Abstract);
                    descriptions.add(description);
                    break;
                }
            }

            previousTextOfItem = item.text();

        }


        return descriptions;
    }

}
