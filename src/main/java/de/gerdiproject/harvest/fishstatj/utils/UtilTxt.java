package de.gerdiproject.harvest.fishstatj.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class UtilTxt
{
    /**
     * Private constructor, because only static methods are provided.
     */
    private UtilTxt()
    {
    }
    

    //read txt files, in resuly we have list of string
    public static List<String> readTextFile(String destination) throws IOException
    {
        Path path = Paths.get(destination);
        return Files.readAllLines(path, StandardCharsets.ISO_8859_1);
    }
}
