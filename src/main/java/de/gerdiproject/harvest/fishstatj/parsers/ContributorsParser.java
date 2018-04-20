package de.gerdiproject.harvest.fishstatj.parsers;

import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
        httpRequester = new HttpRequester();
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
