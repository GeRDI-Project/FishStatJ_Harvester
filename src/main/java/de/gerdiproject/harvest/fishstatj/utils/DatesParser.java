package de.gerdiproject.harvest.fishstatj.utils;

import java.util.LinkedList;
import java.util.List;

import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;
import de.gerdiproject.json.datacite.Date;
import de.gerdiproject.json.datacite.abstr.AbstractDate;
import de.gerdiproject.json.datacite.enums.DateType;

public class DatesParser
{


    public DatesParser()
    {

    }

    public List<AbstractDate> datesParser(String url)
    {

        List<AbstractDate> dates = new LinkedList<>();


        if (!ZipParser.findLinkForDownload(url).equals("") && ZipParser.downloadZipFromUrl(ZipParser.findLinkForDownload(url), FishstatjParameterConstants.PATH_DESTINATION)) {
            ZipParser.unZip(FishstatjParameterConstants.PATH_DESTINATION_FOLDER, FishstatjParameterConstants.PATH_DESTINATION);
            List<String> datesFromZip = ZipParser.addDateAsString(ZipParser.listOfFilesTxt(FishstatjParameterConstants.PATH_DESTINATION_FOLDER), FishstatjParameterConstants.KEY_WORD_FOR_DATES);

            for (String line : datesFromZip) {
                Date lastUpdate = new Date(line, DateType.Updated);

                dates.add(lastUpdate);
            }


        }

        return dates;
    }

}
