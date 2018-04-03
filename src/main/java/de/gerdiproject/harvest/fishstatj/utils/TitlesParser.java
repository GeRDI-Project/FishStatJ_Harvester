package de.gerdiproject.harvest.fishstatj.utils;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import de.gerdiproject.harvest.utils.data.HttpRequester;
import de.gerdiproject.json.datacite.Title;

public class TitlesParser
{

    private HttpRequester httpRequester;

    public TitlesParser()
    {
        httpRequester = new HttpRequester();
    }

    public List<Title> titleParser(String url)
    {

        List<Title> listOfTitle = new ArrayList<Title>();
        Document doc = httpRequester.getHtmlFromUrl(url);

        Element titleFirst = doc.select("#head_title_class").first();
        String title1 = titleFirst.text();
        Title firstTitle = new Title(title1);
        listOfTitle.add(firstTitle);
        //logger.info("Title "+title1);
        Element title_second = doc.select("#head_title_instance").first();
        String title2 = title_second.text();

        //logger.info("Title "+title2);
        Title secondTitle = new Title(title2);
        listOfTitle.add(secondTitle);




        return listOfTitle;

    }

}
