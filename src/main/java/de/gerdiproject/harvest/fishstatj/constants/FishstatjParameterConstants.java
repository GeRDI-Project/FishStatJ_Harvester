package de.gerdiproject.harvest.fishstatj.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FishstatjParameterConstants
{

    public static final String VERSION_KEY = "version";
    public static final String LANGUAGE_KEY = "language";
    public static final String VERSION_DEFAULT = "v1";
    public static final String LANGUAGE_DEFAULT = "en";
    public final static String BASE_URL = "http://www.fao.org/fishery/statistics/collections/en";
    public final static String SITE_URL = "http://www.fao.org%s";
    public static final String PROVIDER = "Food and Agriculture Organization of the United Nations (FAO)";


    public static final String REPOSITORY_ID = "FAOSTAT";
    public static final List<String> DISCIPLINES = Collections.unmodifiableList(Arrays.asList("Statistics"));
    public static final String LOGO_URL = "http://www.fao.org/figis/website/assets/images/templates/shared/fao_logo.gif";
    public static final String DESCRIPTION_FORMAT = "%s : %s";
    public static final String ATTRIBUTE_HREF = "href";
    public static final String NAME_TXT_FILE = "Notes.txt";
    public static final List<String> KEY_WORD_FOR_CR = Collections.unmodifiableList(Arrays.asList("COPYRIGHT & DISCLAIMER CLAUSES", "For comments, views and suggestions relating to this data, please email to:"));
    public static final List<String> KEY_WORD_FOR_DATES = Collections.unmodifiableList(Arrays.asList("Version", "FAO 2017"));

    public static final String PATH_DESTINATION = "downloaded.zip";
    public static final String PATH_DESTINATION_FOLDER = "downloaded/";
    public static final List<String> VALID_DESCRIPTION = Collections.unmodifiableList(Arrays.asList("Collection Overview", "Status", "Typical Usage", "Audience", "Data Security Access Rules", "Dataset Overview"));

    //first header "Name_en" "Ocean_Group" "Major_Group" "FARegion_Group"
    //  "Family_Group", "Order_Group", "Major_Group", "ISSCAAP_Group", "CPCdiv_Group", "CPC_Group"
    //"Ocean_Group", "Major_Group", "FARegion_Group", "Name_E", "Scientific_Name", "Author", "Yearbook_Group"
    public static final List<String> LIST_OF_SUBJECTS = Collections.unmodifiableList(Arrays.asList("Name_en", "Scientific_Name"));
    public static final int SIZE_OF_SHIFT = 1;
    public static final String FILE_WITH_SHIFT = "COUNTRY";



}
