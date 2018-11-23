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
package de.gerdiproject.harvest.fishstatj.utils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;

public class UtilFolder
{


    private static final String ERROR_MESSAGE = "Error";
    private static final Logger log = LoggerFactory.getLogger(UtilFolder.class);


    /**
     * Private constructor, because only static methods are provided.
     */
    private UtilFolder()
    {


    }


    //return list of relevant file
    public static  List<String> listOfFilesByFormat(String destination, String format)
    {
        List<String> listOfPath = new LinkedList<String>();


        try {
            File[] files = new File(destination).listFiles();

            for (File file : files)
                if (file.isFile() && file.getName().contains(format))
                    listOfPath.add(FishstatjParameterConstants.PATH_DESTINATION_FOLDER + file.getName());

        } catch (NullPointerException e) {
            log.error(ERROR_MESSAGE, e);
        }

        return listOfPath;
    }
}
