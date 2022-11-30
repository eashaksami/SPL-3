package com.example.bug_localizer.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileReader {

    public String readFile(String filePath) throws IOException {
        java.io.FileReader fr = new java.io.FileReader(filePath);
        String fileContent = "";
        int i;
        while ((i = fr.read()) != -1) {

            // Print all the content of a file
//            System.out.print((char) i);
            fileContent += (char) i;
        }
//        System.out.println(fileContent);
        return fileContent;
    }

    public String readMultipartFile(MultipartFile file) throws IOException {
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws IOException {
        FileReader fileReader = new FileReader();
        fileReader.readFile("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/BR-Raw/eclipse.jdt.core/46084.txt");
    }
}
