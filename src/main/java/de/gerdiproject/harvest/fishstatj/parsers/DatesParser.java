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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;
import de.gerdiproject.harvest.fishstatj.utils.UtilFolder;
import de.gerdiproject.harvest.fishstatj.utils.UtilTxt;
import de.gerdiproject.harvest.fishstatj.utils.UtilZip;
import de.gerdiproject.json.datacite.Date;
import de.gerdiproject.json.datacite.abstr.AbstractDate;
import de.gerdiproject.json.datacite.enums.DateType;

public class DatesParser
{

    private static final Logger log = LoggerFactory.getLogger(SubjectParser.class);
    private static final String ERROR_MESSAGE = "Error";

    public DatesParser()
    {

    }
    //parse dates, for previous version, txt  had dates of changing versions
    public static List<String> addDateAsString(List<String> listOfFiles, List<String> listOfKeyWords)
    {

        //indicator show us are we inside in block of lines with rights or not
        Boolean indicator = false;
        List<String> addDateAsString = new ArrayList<>();
        String Date = "";

        for (String iterator : listOfFiles) {

            try {
                List<String> Text = UtilTxt.readTextFile(iterator);

                for (String line : Text) {
                    // if we find key word for closing - indicator = false

                    if (line.contains(listOfKeyWords.get(1)))
                        indicator = false;

                    //if we inside add current line to dates
                    if (indicator)

                        //
                        for (int i = 0; i < line.length(); i++) {


                            if (line.toCharArray()[i] == '-') {

                                Date = line.substring(i - 2, i + 8);
                                addDateAsString.add(Date);
                                break;
                            }
                        }



                    // if we find key word for entering - indicator = true

                    if (line.contains(listOfKeyWords.get(0)))

                        indicator = true;


                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                log.error(ERROR_MESSAGE, e);
            }

        }



        return addDateAsString;
    }
    //parser of dates
    public List<AbstractDate> datesParser(String url)
    {

        List<AbstractDate> dates = new LinkedList<>();


        if (!UtilZip.findLinkForDownload(url).equals("") && UtilZip.downloadZipFromUrl(UtilZip.findLinkForDownload(url), FishstatjParameterConstants.PATH_DESTINATION)) {
            UtilZip.unZip(FishstatjParameterConstants.PATH_DESTINATION_FOLDER, FishstatjParameterConstants.PATH_DESTINATION);
            List<String> datesFromZip = addDateAsString(UtilFolder.listOfFilesByFormat(FishstatjParameterConstants.PATH_DESTINATION_FOLDER, "txt"), FishstatjParameterConstants.KEY_WORD_FOR_DATES);

            for (String line : datesFromZip) {
                Date lastUpdate = new Date(line, DateType.Updated);

                dates.add(lastUpdate);
            }


        }

        return dates;
    }

}
