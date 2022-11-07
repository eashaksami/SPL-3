package com.example.bug_localizer;

import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.List;

public class NaturalLanguageTest {
    public static void main(String[] args) throws IOException, ParseException {
        ClassifyBugReport classifyBugReport = new ClassifyBugReport();
        FileReader fileReader = new FileReader();
        String bugReport = fileReader.readFileFromBugReport("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/BR-Raw/tomcat70/38216.txt");

        PseudoRelevanceFeedback feedback = new PseudoRelevanceFeedback();
        List<String> normalizedText = feedback.normalizeText(bugReport);

        String baselineQuery = feedback.listToString(normalizedText);
        System.out.println(baselineQuery);
        List<String> topDocumentsPath = feedback.getTopDocsFromBaselineQuery(baselineQuery);

        List<String> methodDeclarations = feedback.getAllMethodNamesFromTopDocuments(topDocumentsPath);
        List<String> fieldDeclarations = feedback.getAllFieldDeclarationsFromTopDocuments(topDocumentsPath);
    }
}
