package com.example.bug_localizer.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
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
}
