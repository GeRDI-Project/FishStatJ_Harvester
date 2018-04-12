package de.gerdiproject.harvest.fishstatj.utils;

import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;
import de.gerdiproject.harvest.utils.data.HttpRequester;
import de.gerdiproject.json.datacite.Rights;




public class RightsParser
{

    private HttpRequester httpRequester;


    public RightsParser()
    {
        httpRequester = new HttpRequester();

    }




    public List<Rights> rightsParser(String url)
    {
        List<Rights> Rights = new LinkedList<>();
        Document doc = httpRequester.getHtmlFromUrl(url);
        //choose from webpage element with class allWidth and all his children
        Elements descriptionNames = doc.select("#allWidth").first().children();
        //define previousTextOfItem as zero because for first entry it will be empty
        String previousTextOfItem = "";
        String hrefLink = "";

        for (Element item : descriptionNames) {

            if (previousTextOfItem.contains("Data Security Access Rules")) {
                //add all text of rights
                Rights right = new Rights(item.text());
                Element children = item.children().first();
                hrefLink = children.attr(FishstatjParameterConstants.ATTRIBUTE_HREF);

                // if we have some url, we add this url to the Rights
                if (!hrefLink.equals(""))
                    right.setURI(String.format(FishstatjParameterConstants.SITE_URL, hrefLink));

                Rights.add(right);
            }

            previousTextOfItem = item.text();
        }

        // add rights from zip
        if (!ZipParser.findLinkForDownload(url).equals("") && ZipParser.downloadZipFromUrl(ZipParser.findLinkForDownload(url), FishstatjParameterConstants.PATH_DESTINATION)) {
            ZipParser.unZip(FishstatjParameterConstants.PATH_DESTINATION_FOLDER, FishstatjParameterConstants.PATH_DESTINATION);
            Rights.add(ZipParser.addRights(ZipParser.listOfFilesTxt(FishstatjParameterConstants.PATH_DESTINATION_FOLDER), FishstatjParameterConstants.KEY_WORD_FOR_CR));


        }



        return Rights;
    }
}
