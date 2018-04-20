package de.gerdiproject.harvest.fishstatj.parsers;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;
import de.gerdiproject.harvest.fishstatj.utils.UtilFolder;
import de.gerdiproject.harvest.fishstatj.utils.UtilTxt;
import de.gerdiproject.harvest.fishstatj.utils.UtilZip;
import de.gerdiproject.harvest.utils.data.HttpRequester;
import de.gerdiproject.json.datacite.Rights;




public class RightsParser
{

    private static HttpRequester httpRequester;
    private static final Logger log = LoggerFactory.getLogger(SubjectParser.class);
    private static final String ERROR_MESSAGE = "Error";


    public RightsParser()
    {
        httpRequester = new HttpRequester();

    }

    
    public static  Rights addRightsFromTxt (List<String> listOfFiles, List<String> listOfKeyWords)
    {

        //indicator show us are we inside in block of lines with rights or not
        Boolean indicator = false;
        String rightsAsString = "";

        for (String iterator : listOfFiles) {

            try {
                List<String> Text = UtilTxt.readTextFile(iterator);

                for (String line : Text) {
                    // if we find key word for closing - indicator = false
                    if (line.contains(listOfKeyWords.get(1)))
                        indicator = false;

                    //if we inside add current line to rights
                    if (indicator)
                        rightsAsString = rightsAsString.concat(line);

                    // if we find key word for entering - indicator = true

                    if (line.contains(listOfKeyWords.get(0)))

                        indicator = true;


                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                log.error(ERROR_MESSAGE, e);
            }

        }

        Rights right = new Rights(rightsAsString);


        return right;
    }
    
    public static  List<Rights> addRightsFromWeb (String url)
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
        return Rights;
    	
    }


    public List<Rights> rightsParser(String url)
    {
        List<Rights> Rights = new LinkedList<>();
        Rights.addAll(addRightsFromWeb(url));
        // add rights from zip
        if (!UtilZip.findLinkForDownload(url).equals("") && UtilZip.downloadZipFromUrl(UtilZip.findLinkForDownload(url), FishstatjParameterConstants.PATH_DESTINATION)) {
            UtilZip.unZip(FishstatjParameterConstants.PATH_DESTINATION_FOLDER, FishstatjParameterConstants.PATH_DESTINATION);
            Rights.add(addRightsFromTxt(UtilFolder.listOfFilesByFormat(FishstatjParameterConstants.PATH_DESTINATION_FOLDER, "txt"), FishstatjParameterConstants.KEY_WORD_FOR_CR));

        }

        return Rights;
    }
}
