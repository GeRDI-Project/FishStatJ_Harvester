package de.gerdiproject.harvest.fishstatj.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;
import de.gerdiproject.harvest.utils.data.HttpRequester;
import de.gerdiproject.json.datacite.Rights;
import de.gerdiproject.json.datacite.Subject;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class ZipParser
{
    private HttpRequester httpRequester;
    private static final Logger log = LoggerFactory.getLogger("Name");
    final static Charset ENCODING = StandardCharsets.UTF_8;
    private static final String ERROR_MESSAGE = "Error";

    public ZipParser()
    {
        //logger = LoggerFactory.getLogger(ERROR_MESSAGE);
        httpRequester = new HttpRequester();

    }

    //parse subjects
    public String findLinkForDownload(String url)
    {
        //need to find zip
        Document doc = httpRequester.getHtmlFromUrl(url);
        Element element = doc.select("a[href$=.zip]").first();

        if (element != null) {
            String downloadLink = element.attr(FishstatjParameterConstants.ATTRIBUTE_HREF);
            return downloadLink;

        }

        return "";

    }

    //download to server zip archive, way: /var/lib/jetty/downloaded.zip, if all ok, return true
    public static boolean downloadZipFromUrl(String downloadLink, String destination)
    {
        URL urlDownload = null;

        try {
            urlDownload = new URL(downloadLink);

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            //logger.error("Error", e)
            log.error(ERROR_MESSAGE, e);
            //e.printStackTrace();
        }

        File file = new File(destination);

        try {

            FileUtils.copyURLToFile(urlDownload, file);
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error(ERROR_MESSAGE, e);
        }

        return false;
    }
    //unzip file
    public static void unZip(String destination, String source)
    {

        try {
            ZipFile zipFile = new ZipFile(source);
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            log.error(ERROR_MESSAGE, e);
        }
    }
    //return list of relevant file
    public static  List<String> listOfFilesCsv(String destination)
    {
        List<String> listOfPath = new LinkedList<String>();

        //questions about findbag and null pointer exeption!!!!!!!!
        try {
            File[] files = new File(destination).listFiles();

            //If this pathname does not denote a directory, then listFiles() returns null.
            //also, here i skipped a huge csv file which doesn't contain any useful information
            for (File file : files) {
                if (file.isFile() && file.getName().contains("csv") && !file.getName().contains("TS_FI")) {
                    listOfPath.add(FishstatjParameterConstants.PATH_DESTINATION_FOLDER + file.getName());
                    //logger.info(PATH_DESTINATION_FOLDER+file.getName());
                }
            }

        } catch (NullPointerException e) {
            log.error(ERROR_MESSAGE, e);
        }

        return listOfPath;
    }

    //find txt files
    public static  List<String> listOfFilesTxt(String destination)
    {
        List<String> listOfPath = new LinkedList<String>();

        try {
            File[] files = new File(destination).listFiles();

            //If this pathname does not denote a directory, then listFiles() returns null.
            //also, here i skipped a huge csv file which doesn't contain any useful information
            for (File file : files) {
                if (file.isFile() && file.getName().contains("txt")) {
                    listOfPath.add(FishstatjParameterConstants.PATH_DESTINATION_FOLDER + file.getName());
                    //logger.info(PATH_DESTINATION_FOLDER+file.getName());
                }
            }

        } catch (NullPointerException e) {
            log.error(ERROR_MESSAGE, e);
        }

        return listOfPath;
    }

    //read txt files
    List<String> readTextFile(String aFileName) throws IOException
    {
        Path path = Paths.get(aFileName);
        return Files.readAllLines(path, ENCODING);
    }


    public List<Rights> rightsParser(String url)
    {
        List<Rights> Rights = new LinkedList<>();

        return Rights;
    }

    public static Set<Subject> addSubject(List<String> listOfFiles, List<String> listOfSubject)
    {
        // create list of subject
        Set<Subject> subjects = new HashSet<Subject>();

        // reading for each csv document
        for (String iterator : listOfFiles) {

            try {
                // nextLine[] is an array of values from the line
                String [] nextLine;
                //create reader
                @SuppressWarnings("resource")
                CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(iterator), ENCODING));
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
                                if (iterator.contains("COUNTRY")) {
                                    if (!nextLine[i + 1].equals("")) {
                                        Subject subject = new Subject(nextLine[i + 1]);
                                        //logger.info("Subject "+nextLine[i]);
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
    //check why not for all it works correctly
    public  List<Subject> getSubjectFromUrl(String url)
    {
        List<Subject> subjects = new ArrayList<Subject>();

        //String LinkToZip = findLinkForDownload(url);
        if (downloadZipFromUrl(findLinkForDownload(url), FishstatjParameterConstants.getPathDestination())) {
            unZip(FishstatjParameterConstants.getPathDestinatioFolder(), FishstatjParameterConstants.getPathDestination());
            //logger.info("parsing subjects");
            subjects.addAll(addSubject(listOfFilesCsv(FishstatjParameterConstants.getPathDestinatioFolder()), FishstatjParameterConstants.LIST_OF_SUBJECTS));
            // List<Subject> list = new ArrayList<Subject>(addSubject(listOfFiles(FishstatjParameterConstants.PATH_DESTINATION_FOLDER), FishstatjParameterConstants.LIST_OF_SUBJECTS));
            //return list;
        }

        return  subjects;

    }


}
