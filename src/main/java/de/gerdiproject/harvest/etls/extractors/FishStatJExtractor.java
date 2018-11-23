/*
 *  Copyright © 2018 Robin Weiss (http://www.gerdi-project.de/)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
package de.gerdiproject.harvest.etls.extractors;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;
import de.gerdiproject.harvest.utils.data.HttpRequester;

/**
 * @author Robin Weiss, Bohdan Tkachuk
 *
 */
public class FishStatJExtractor extends AbstractIteratorExtractor<Element>
{
    private final HttpRequester httpRequester = new HttpRequester(new Gson(), StandardCharsets.UTF_8);

    private Iterator<Element> sourceIterator;
    private String version = null;
    private int size = -1;


    @Override
    public void init(AbstractETL<?, ?> etl)
    {
        super.init(etl);
        httpRequester.setCharset(etl.getCharset());

        final Document baseWebsite = httpRequester.getHtmlFromUrl(FishstatjParameterConstants.BASE_URL);
        final Elements fishStatJSources = baseWebsite.select("a[title=\"data collection\"], a[title=\"search interface\"], a[title=\"webpage\"], a[title=\"website; map\"]");

        this.size = fishStatJSources.size();

        // TODO replace with something better
        this.version = String.valueOf(fishStatJSources.size());

        this.sourceIterator = fishStatJSources.iterator();
    }


    @Override
    public String getUniqueVersionString()
    {
        return version;
    }


    @Override
    public int size()
    {
        return size;
    }


    @Override
    protected Iterator<Element> extractAll() throws ExtractorException
    {
        return new FishStatJIterator();
    }


    /**
     * This {@linkplain Iterator} iterates through FishStatJ HTML documents and
     * generates
     *
     *
     *
     * TODO
     *
     *
     *
     * @author Robin Weiss
     *
     */
    private class FishStatJIterator implements Iterator<Element>
    {
        @Override
        public boolean hasNext()
        {
            return sourceIterator.hasNext();
        }

        @Override
        public Element next()
        {
            final Element nextSource = sourceIterator.next();
            final String url = nextSource.attr(FishstatjParameterConstants.ATTRIBUTE_HREF);
            final Document subSite = httpRequester.getHtmlFromUrl(url);

            if (subSite.hasText())
                return nextSource;
            else
                return null;
        }

    }
}
