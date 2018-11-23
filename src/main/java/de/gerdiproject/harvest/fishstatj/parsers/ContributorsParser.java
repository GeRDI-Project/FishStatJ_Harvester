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
import de.gerdiproject.json.datacite.Contributor;
import de.gerdiproject.json.datacite.enums.ContributorType;
import de.gerdiproject.json.datacite.enums.NameType;
import de.gerdiproject.json.datacite.nested.PersonName;

public class ContributorsParser
{

    private HttpRequester httpRequester;


    public ContributorsParser()
    {
        // TODO Auto-generated constructor stub
        httpRequester = new HttpRequester(new Gson(), StandardCharsets.UTF_8);
    }
    public List<Contributor> contributorsParser(String url)
    {
        List<Contributor> listOfContributor = new LinkedList<>();
        Document doc = httpRequester.getHtmlFromUrl(url);
        //find element on page with "contact"
        Element linkToTheContact = doc.select("a:contains(Contact)").first();

        if (linkToTheContact != null) {
            String urlToContact = String.format(FishstatjParameterConstants.SITE_URL, linkToTheContact.attr(FishstatjParameterConstants.ATTRIBUTE_HREF));

            Document contactDoc = httpRequester.getHtmlFromUrl(urlToContact);
            Elements contactElements = contactDoc.select(".padBottom");

            for (Element item : contactElements) {

                //we need to parse name and surname and recognise where is organisation where is person
                // for recognition we just check manually through all list of family name
                // in our case we can just can use hard coding
                PersonName person = new PersonName(item.text(), NameType.Organisational);
                Contributor contributor = new Contributor(person, ContributorType.ContactPerson);

                if (item.text().contains("Valerio Crespi") || item.text().contains("Dr Jacek Majkowski") || item.text().contains("Ms Rossi Taddei") || item.text().contains("Mr Fabio Carocci") || item.text().contains("José Aguilar-Manjarrez") || item.text().contains("Francesco Cardia") || item.text().contains("Karn Sukwong") || item.text().contains("Devin Bartley") || item.text().contains("Blaise Kuemlangan") || item.text().contains("Ms. Marianne Guyonnet")) {
                    person.setNameType(NameType.Personal);
                    contributor.setName(person);
                }

                listOfContributor.add(contributor);
            }

        }



        return listOfContributor;
    }

}
