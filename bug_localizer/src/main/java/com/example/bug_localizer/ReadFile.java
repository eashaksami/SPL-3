package com.example.bug_localizer;

import java.io.FileReader;
import java.io.IOException;

public class ReadFile {

    public String readFileFromBugReport(String filePath) throws IOException {
        FileReader fr = new FileReader(filePath);
        String fileContent = "";
        int i;
        while ((i = fr.read()) != -1) {

            // Print all the content of a file
//            System.out.print((char) i);
            fileContent += (char) i;
        }
        System.out.println(fileContent);
        return fileContent;
    }

    public static void main(String[] args) throws IOException {
        ReadFile readFile = new ReadFile();
        readFile.readFileFromBugReport("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018-v1.1/BR-Raw/eclipse.jdt.core/46084.txt");
    }
}
