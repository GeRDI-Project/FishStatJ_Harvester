package de.gerdiproject.harvest.fishstatj.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

import de.gerdiproject.json.datacite.Subject;

public class UtilCsv
{

    private static final Logger log = LoggerFactory.getLogger(UtilZip.class);
    private static final String ERROR_MESSAGE = "Error";

    public UtilCsv()
    {

    }


    public static Set<Subject> addSubject(List<String> listOfFiles, List<String> listOfSubject, String filesNameWithExeption, int sizeOfShift)
    {
        // create list of subject
        Set<Subject> subjects = new HashSet<Subject>();

        //log.
        // reading for each csv document
        for (String iterator : listOfFiles) {

            try {
                // nextLine[] is an array of values from the line
                String [] nextLine;
                //create reader
                @SuppressWarnings("resource")
                CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(iterator), StandardCharsets.UTF_8));
                //go through whole document
                String[] header = reader.readNext();
                //first header "Name_en" "Ocean_Group" "Major_Group" "FARegion_Group"

                //logger.info("header"+header[0]+" "+header[1]+" "+header[2]+" "+header[3]);
                while ((nextLine = reader.readNext()) != null) {

                    // for each element of line add subject
                    for (int i = 0; i < nextLine.length; i++) {
                        //find right columnn
                        // go through list with all important header in csv for us, when we have coincidence, add to the subjects
                        for (String element : listOfSubject) {
                            //logger.info("header "+header[j]);
                            if (header[i].contains(element)) {
                                //we have mistake in our data, in xls with countries all column shifted
                                if (iterator.contains(filesNameWithExeption)) {
                                    if (!nextLine[i + 1].equals("")) {
                                        Subject subject = new Subject(nextLine[i + sizeOfShift]);
                                        subject.setLang("en");
                                        subjects.add(subject);

                                    }

                                } else {
                                    if (!nextLine[i + 1].equals("")) {
                                        Subject subject = new Subject(nextLine[i]);
                                        subject.setLang("en");
                                        subjects.add(subject);
                                    }
                                }
                            }

                        }

                    }

                }



            } catch (IOException e) {
                // TODO Auto-generated catch block
                log.error(ERROR_MESSAGE, e);
            }
        }

        return subjects;

    }


}
