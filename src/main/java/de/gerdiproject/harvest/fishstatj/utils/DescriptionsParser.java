package de.gerdiproject.harvest.fishstatj.utils;

import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;
import de.gerdiproject.harvest.utils.data.HttpRequester;
import de.gerdiproject.json.datacite.Description;
import de.gerdiproject.json.datacite.enums.DescriptionType;

public class DescriptionsParser
{

    private HttpRequester httpRequester;

    public DescriptionsParser()
    {
        httpRequester = new HttpRequester();
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
            // maybe better to put all this names of descriptions in one list as a parameter and use loop for checking



            for (String validDescription : FishstatjParameterConstants.VALID_DESCRIPTION) {
                if (previousTextOfItem.contains(validDescription)) {
                    Description description = new Description(String.format(FishstatjParameterConstants.DESCRIPTION_FORMAT, previousTextOfItem, item.text()), DescriptionType.Abstract);
                    descriptions.add(description);
                    break;
                }
            }

            /*if (previousTextOfItem.equals("Collection Overview")
                    || previousTextOfItem.contains("Status")
                    ) {
                Description description = new Description(String.format(FishstatjParameterConstants.DESCRIPTION_FORMAT, previousTextOfItem, item.text()), DescriptionType.Abstract);
                descriptions.add(description);
            }

            if (previousTextOfItem.contains("Status")) {
                Description description = new Description(String.format(FishstatjParameterConstants.DESCRIPTION_FORMAT, previousTextOfItem, item.text()), DescriptionType.Abstract);
                descriptions.add(description);

            }

            if (previousTextOfItem.contains("Typical Usage")) {
                Description description = new Description(String.format(FishstatjParameterConstants.DESCRIPTION_FORMAT, previousTextOfItem, item.text()), DescriptionType.Abstract);
                descriptions.add(description);

            }

            if (previousTextOfItem.contains("Audience")) {
                Description description = new Description(String.format(FishstatjParameterConstants.DESCRIPTION_FORMAT, previousTextOfItem, item.text()), DescriptionType.Abstract);
                descriptions.add(description);

            }

            if (previousTextOfItem.contains("Data Security Access Rules")) {

                Description description = new Description(String.format(FishstatjParameterConstants.DESCRIPTION_FORMAT, previousTextOfItem, item.text()), DescriptionType.Abstract);
                descriptions.add(description);

            }

            if (previousTextOfItem.contains("Dataset Overview")) {
                Description description = new Description(String.format(FishstatjParameterConstants.DESCRIPTION_FORMAT, previousTextOfItem, item.text()), DescriptionType.Abstract);
                descriptions.add(description);

            }*/

            previousTextOfItem = item.text();

        }


        return descriptions;
    }

}
