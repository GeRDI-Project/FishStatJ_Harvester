/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package de.gerdiproject.harvest.harvester;

import java.security.NoSuchAlgorithmException;

import de.gerdiproject.harvest.FishStatJContextListener;
import de.gerdiproject.harvest.IDocument;


import de.gerdiproject.json.datacite.DataCiteJson;
import de.gerdiproject.json.datacite.Subject;
import de.gerdiproject.json.datacite.Title;
import de.gerdiproject.json.datacite.extension.WebLink;
import de.gerdiproject.json.datacite.extension.enums.WebLinkType;
//import de.gerdiproject.harvest.fishstatj.constants.FishstatjDataCiteConstants;
import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * A harvester for FishStatJ (http://www.fao.org/fishery/statistics/collections/en).
 *
 * @author Bohdan Tkachuk
 */
public class FishStatJHarvester extends AbstractListHarvester<Element> // TODO choose an AbstractHarvester implementation that suits your needs
{
    /**
     * Default Constructor that is called by the MainContext.
     */
	private final static String BASE_URL = "http://www.fao.org/fishery/statistics/collections/en";
	private static final String PROVIDER = "Food and Agriculture Organization of the United Nations (FAO)";
    public static final String REPOSITORY_ID = "FAOSTAT";
    public static final List<String> DISCIPLINES = Collections.unmodifiableList(Arrays.asList("Statistics"));
    private static final String LOGO_URL = "http://www.fao.org/figis/website/assets/images/templates/shared/fao_logo.gif";
    
    
    public FishStatJHarvester()
    {
        super(1);// 
        // TODO initialize final fields
    }
    
    @Override
    //for entry we have list of links
    protected Collection<Element> loadEntries()
    {
        //String domainsUrl = String.format(BASE_URL);
        //logger.info(domainsUrl);
        Document doc = httpRequester.getHtmlFromUrl(BASE_URL);
        //logger.info("Document null?: " + (doc == null));
        //logger.info(doc.html());
        //logger.info(doc.attributes());
        logger.info("found " + doc.select("a[title=\"data collection\"], a[title=\"search interface\"], a[title=\"webpage\"], a[title=\\\"search interface\\\"], a[title=\"website; map\"]").size() + " documents from " + BASE_URL);
        return doc.select("a[title=\"data collection\"]+a[title=\"search interface\"+a[title=\"webpage\"");
    }
    
    //parse titles	
    public List<Title> titleParser(String url){
		
		List<Title> list_of_title = new ArrayList<Title>();
		Document doc = httpRequester.getHtmlFromUrl(url);
	    Element title_first = doc.select("a[id=\"head_title_class\"]").first(); 
	    Elements children1 = title_first.children();
	    String title1 = children1.text();
	    Title firstTitle = new Title(String.format(title1));
	    list_of_title.add(firstTitle);
	    Element title_second = doc.select("a[id=\"head_title_instance\"]").first(); 
	    Elements children2 = title_second.children();
	    String title2 = children2.text();
	    Title secondTitle = new Title(String.format(title2));
	    list_of_title.add(secondTitle);
	    return list_of_title;
	    
	}
    
   
    	   

    
    protected List<IDocument> harvestEntry(Element entry)
    {
    	// get attributes
        //Elements children = entry.children();
    	String language = getProperty(FishstatjParameterConstants.LANGUAGE_KEY);
        String version = getProperty(FishstatjParameterConstants.VERSION_KEY);
        Attributes attributes = entry.attributes();
        String url = attributes.get("href");
        
        
        DataCiteJson document = new DataCiteJson();
        document.setVersion(version);
        document.setLanguage(language);
        document.setTitles(titleParser(url));
        document.setPublisher(PROVIDER);        
        document.setResearchDisciplines(DISCIPLINES);
        document.setRepositoryIdentifier(REPOSITORY_ID);
        
        List<WebLink> links = new LinkedList<>();
        WebLink viewLink = new WebLink(url);
        viewLink.setName("View website");
        viewLink.setType(WebLinkType.ViewURL);
        links.add(viewLink);
        
        WebLink logoLink = new WebLink(LOGO_URL);
        logoLink.setName("Logo");
        logoLink.setType(WebLinkType.ProviderLogoURL);
        links.add(logoLink);
        
        document.setWebLinks(links);
        
        

        
        

       

        return Arrays.asList(document);
    }
   

	
	
	
}
