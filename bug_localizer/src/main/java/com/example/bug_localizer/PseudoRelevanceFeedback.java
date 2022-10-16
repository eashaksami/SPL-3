package com.example.bug_localizer;

import com.example.bug_localizer.staticData.StaticData;
import com.example.bug_localizer.test.AstParserTest;
import com.example.bug_localizer.test.Searcher;
import com.example.bug_localizer.test.TextNormalizerTest;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PseudoRelevanceFeedback {
    public List<String> normalizeText(String query) throws IOException {
        TextNormalizerTest textNormalizerTest = new TextNormalizerTest();
        return textNormalizerTest.removeStopWords(query);
    }

    public String listToString(List<String> stringList) {
        return stringList.stream().collect(Collectors.joining(" "));
    }

    public List<String> getTopDocsFromBaselineQuery(String baselineQuery) throws IOException, ParseException {
        Searcher searcher = new Searcher(StaticData.indexDir);
        List<String> topDocumentsPath = new ArrayList<>();

        TopDocs hits = searcher.search(baselineQuery);
        System.out.println(hits.totalHits + " documents found. ");

        for(ScoreDoc scoreDoc: hits.scoreDocs) {
            Document document = Searcher.indexSearcher.doc(scoreDoc.doc);
            System.out.println("File: " + document.get("path") + " Score: " + scoreDoc.score);
            topDocumentsPath.add(document.get("path"));
        }
        return topDocumentsPath;
    }

    public List<String> getAllMethodNamesFromTopDocuments(List<String> documentsPath) throws IOException {
        AstParserTest astParserTest = new AstParserTest();
        List<String> methodNames = new ArrayList<>();

        for (String documentPath: documentsPath) {
            CompilationUnit cu = astParserTest.getCompilationUnit(documentPath);
            methodNames.addAll(astParserTest.getAllMethodNames(cu));
        }
        System.out.println(methodNames);
        return methodNames;
    }

    public List<String> getAllFieldDeclarationsFromTopDocuments(List<String> documentsPath) throws IOException {
        AstParserTest astParserTest = new AstParserTest();
        List<String> fieldDeclarations = new ArrayList<>();

        for (String documentPath: documentsPath) {
            CompilationUnit cu = astParserTest.getCompilationUnit(documentPath);
            fieldDeclarations.addAll(astParserTest.getAllFieldSignatures(cu));
        }
        System.out.println(fieldDeclarations);
        return fieldDeclarations;
    }

    public static void main(String[] args) throws IOException, ParseException {
        PseudoRelevanceFeedback feedback = new PseudoRelevanceFeedback();
        FileReader fileReader = new FileReader();
        String query = fileReader.readFileFromBugReport("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/BR-Raw/tomcat70/38216.txt");

        List<String> normalizedText = feedback.normalizeText(query);

        String baselineQuery = feedback.listToString(normalizedText);
        System.out.println(baselineQuery);
        List<String> topDocumentsPath = feedback.getTopDocsFromBaselineQuery(baselineQuery);

        List<String> methodDeclarations = feedback.getAllMethodNamesFromTopDocuments(topDocumentsPath);
        List<String> fieldDeclarations = feedback.getAllFieldDeclarationsFromTopDocuments(topDocumentsPath);
    }
}
