package com.example.bug_localizer;

import java.io.IOException;

public class FileReader {

    public String readFileFromBugReport(String filePath) throws IOException {
        java.io.FileReader fr = new java.io.FileReader(filePath);
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
        FileReader fileReader = new FileReader();
        fileReader.readFileFromBugReport("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/BR-Raw/eclipse.jdt.core/46084.txt");
    }
}
