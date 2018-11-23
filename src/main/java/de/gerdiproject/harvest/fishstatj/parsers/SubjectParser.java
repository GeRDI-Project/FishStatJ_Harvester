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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import de.gerdiproject.harvest.fishstatj.constants.FishstatjParameterConstants;
import de.gerdiproject.harvest.fishstatj.utils.UtilCsv;
import de.gerdiproject.harvest.fishstatj.utils.UtilFolder;
import de.gerdiproject.harvest.fishstatj.utils.UtilZip;
import de.gerdiproject.json.datacite.Subject;

public class SubjectParser
{
    final static Charset ENCODING = StandardCharsets.UTF_8;
    public SubjectParser()
    {

    }


    public  List<Subject> getSubjectFromUrl(String url)
    {
        List<Subject> subjects = new ArrayList<Subject>();

        if (UtilZip.downloadZipFromUrl(UtilZip.findLinkForDownload(url), FishstatjParameterConstants.PATH_DESTINATION)) {

            UtilZip.unZip(FishstatjParameterConstants.PATH_DESTINATION_FOLDER, FishstatjParameterConstants.PATH_DESTINATION);
            subjects.addAll(UtilCsv.addSubject(UtilFolder.listOfFilesByFormat(FishstatjParameterConstants.PATH_DESTINATION_FOLDER, "csv"), FishstatjParameterConstants.LIST_OF_SUBJECTS, FishstatjParameterConstants.FILE_WITH_SHIFT, FishstatjParameterConstants.SIZE_OF_SHIFT));

        }

        return  subjects;

    }


}
