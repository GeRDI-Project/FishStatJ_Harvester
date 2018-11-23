package de.gerdiproject.harvest.fishstatj.parsers;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;
//import de.gerdiproject.harvest.harvester.AbstractHarvester.httpRequester;
import de.gerdiproject.harvest.utils.data.HttpRequester;
import de.gerdiproject.json.datacite.extension.WebLink;
import de.gerdiproject.json.datacite.extension.enums.WebLinkType;


public class WeblinksParser
{

    private HttpRequester httpRequester;

    public WeblinksParser()
    {
        httpRequester = new HttpRequester(new Gson(), StandardCharsets.UTF_8);
    }

    public List<WebLink> weblinksParser(String url)
    {
        List<WebLink> weblinks = new LinkedList<>();
        Document doc = null;
        doc = httpRequester.getHtmlFromUrl(url);
        //choose from webpage element with class allWidth and all his children
        Elements webPage = doc.select("#allWidth").first().children();
        String previousTextOfItem = "";
        String hrefLink = "";
        // add link for download
        //String downloadLink = "";

        //iterate all elements
        for (Element itemwebPage : webPage) {
            //find element with text what we need
            if (previousTextOfItem.contains("Available Formats & Information Products")) {
                Elements children = itemwebPage.children().select("a");

                for (Element itemChildren : children) {
                    Attributes attributes = itemChildren.attributes();
                    hrefLink = attributes.get(FishstatjParameterConstants.ATTRIBUTE_HREF);

                    WebLink Link = new WebLink(hrefLink);

                    //need to check does this link work or not, some links not absolute, but relative, check it, if it is relative link, add SITE_URL
                    // first of at all cut /javascript:new_window('/fishery/statistics/global-production/query/en','biblio',1,1,1,1,1,1,1,700,650);
                    // and have only /fishery/statistics/global-production/query/en
                    if (hrefLink.contains("javascript:new_window"))
                        hrefLink = hrefLink.substring(hrefLink.indexOf('\'') + 1, hrefLink.indexOf(',') - 1);


                    //check this link absolute or relative
                    if (hrefLink.contains("www")) {
                        //WebLink Link = new WebLink(hrefLink);

                        //links on publication haven't any text, only picture, for this case, set name as "Publication"
                        if (itemChildren.text().equals("")) {
                            Link.setName("Publication");
                            Link.setType(WebLinkType.Related);
                        } else {
                            Link.setType(WebLinkType.SourceURL);
                            Link.setName(itemChildren.text());
                        }

                        Link.setUrl(hrefLink);
                        weblinks.add(Link);
                    }

                    else {
                        Link.setUrl(String.format(FishstatjParameterConstants.SITE_URL, hrefLink));
                        Link.setName(itemChildren.text());
                        Link.setType(WebLinkType.SourceURL);
                        weblinks.add(Link);
                    }
                }
            }

            previousTextOfItem = itemwebPage.text();
        }

        //links to "coverage", Str and etc
        //WebLink sideBar = new WebLink(); Element linkToTheContact = doc.select().first();

        Element sideBar = doc.select("a:contains(Coverage)").first();

        //if this element exist
        if (sideBar != null) {
            WebLink coverageWebLink = new WebLink(String.format(FishstatjParameterConstants.SITE_URL, sideBar.attr(FishstatjParameterConstants.ATTRIBUTE_HREF)));
            coverageWebLink.setName(sideBar.text());
            coverageWebLink.setType(WebLinkType.Related);
            weblinks.add(coverageWebLink);
        }

        sideBar = doc.select("a:contains(Structure)").first();

        if (sideBar != null) {
            WebLink StructureWebLink = new WebLink(String.format(FishstatjParameterConstants.SITE_URL, sideBar.attr(FishstatjParameterConstants.ATTRIBUTE_HREF)));
            StructureWebLink.setName(sideBar.text());
            StructureWebLink.setType(WebLinkType.Related);
            weblinks.add(StructureWebLink);
        }

        sideBar = doc.select("a:contains(Data Source)").first();

        if (sideBar != null) {
            WebLink DataSourceWebLink = new WebLink(String.format(FishstatjParameterConstants.SITE_URL, sideBar.attr(FishstatjParameterConstants.ATTRIBUTE_HREF)));
            DataSourceWebLink.setName(sideBar.text());
            DataSourceWebLink.setType(WebLinkType.Related);
            weblinks.add(DataSourceWebLink);
        }


        WebLink viewLink = new WebLink(url);
        viewLink.setName("View website");
        viewLink.setType(WebLinkType.ViewURL);
        weblinks.add(viewLink);

        WebLink logoLink = new WebLink(FishstatjParameterConstants.LOGO_URL);
        logoLink.setName("Logo");
        logoLink.setType(WebLinkType.ProviderLogoURL);
        weblinks.add(logoLink);




        return weblinks;
    }

}
