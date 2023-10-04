package com.example.bug_localizer.utils;

import java.io.IOException;
import java.util.*;

public class ResultValidation {
    public static void main(String[] args) throws IOException {
        FileReader fileReader = new FileReader();
        String fileContent = fileReader.readFile("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/Lucene-Index2File-Mapping/tomcat70.ckeys");
        ResultValidation validation = new ResultValidation();
        validation.getFileNameAndNumber(fileContent);

        String changedFilesContent = fileReader.readFile("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/Goldset/tomcat70/3839.txt");
        validation.changedFilesList(changedFilesContent);

        String bugIdsReport = fileReader.readFile("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/BLIZZARD/Result-Matched-Indices/tomcat70/proposed-ST.txt");
        validation.getAllBugsIdOfBugType(bugIdsReport);
    }

    public Map<String, String> getFileNameAndNumber(String file) {
        String[] files = file.split("\\r?\\n");
        Map<String, String> fileNameAndNumberMap = new HashMap<>();
        for (String fileName : files) {
            fileName = fileName.replace("\\", "\\\\");
            String[] items= fileName.split("\\\\");
            String[] fileNo = items[0].split(":");
//            System.out.println(Arrays.toString(items));
            fileNameAndNumberMap.put(fileNo[0] + ".java", items[items.length - 1]);
//            System.out.println(fileName);
        }
        System.out.println(fileNameAndNumberMap);
        return fileNameAndNumberMap;
    }

    public List<String> changedFilesList(String file) {
        String[] files = file.split("\\r?\\n");
        List<String> fileNames = new ArrayList<>();

        for (String fileName : files) {
            String[] name = fileName.split("/");
            fileNames.add(name[name.length-1]);
            System.out.println(name[name.length-1]);
        }
        return fileNames;
    }

    public List<String> getAllBugsIdOfBugType(String file) {
        List<String> bugIds = new ArrayList<>();

        String[] lines = file.split("\\r?\\n");
        for (String line : lines) {
            String[] lineContent = line.split(":");
            System.out.println(lineContent[0]);
            bugIds.add(lineContent[0]);
        }
        return bugIds;
    }
}
