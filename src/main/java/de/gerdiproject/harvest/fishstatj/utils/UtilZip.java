package de.gerdiproject.harvest.fishstatj.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;
import de.gerdiproject.harvest.utils.data.HttpRequester;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class UtilZip
{
    private static HttpRequester httpRequester = new HttpRequester(new Gson(), StandardCharsets.UTF_8);
    private static final Logger log = LoggerFactory.getLogger(UtilZip.class);
    final static Charset ENCODING = StandardCharsets.UTF_8;
    private static final String ERROR_MESSAGE = "Error";

    public UtilZip()
    {

    }

    //parse subjects
    public static String findLinkForDownload(String url)
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
            log.error(ERROR_MESSAGE, e);
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













}
