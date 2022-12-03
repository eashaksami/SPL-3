package com.example.bug_localizer.utils;

import com.example.bug_localizer.staticData.StaticData;
import com.example.bug_localizer.test.AstParser;
import com.example.bug_localizer.utils.lucene.Searcher;
import com.example.bug_localizer.test.TextNormalizer;
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
        TextNormalizer textNormalizer = new TextNormalizer();
        return textNormalizer.removeStopWords("[Patch] Generics warnings - rawtypes");
    }

    public String listToString(List<String> stringList) {
        return stringList.stream().collect(Collectors.joining(" "));
    }

    public List<String> getTopDocsFromBaselineQuery(String baselineQuery) throws IOException, ParseException {
        Searcher searcher = new Searcher(StaticData.indexDir);
        List<String> topDocumentsPath = new ArrayList<>();

        TopDocs hits = searcher.search(baselineQuery, 10);
        System.out.println(hits.totalHits + " documents found. ");

        for(ScoreDoc scoreDoc: hits.scoreDocs) {
            Document document = Searcher.indexSearcher.doc(scoreDoc.doc);
            System.out.println("File: " + document.get("filepath") + " Score: " + scoreDoc.score);
            topDocumentsPath.add(document.get("filepath"));
        }
        return topDocumentsPath;
    }

    public List<String> getAllMethodNamesFromTopDocuments(List<String> documentsPath) throws IOException {
        AstParser astParser = new AstParser();
        List<String> methodNames = new ArrayList<>();

        for (String documentPath: documentsPath) {
            CompilationUnit cu = astParser.getCompilationUnit(documentPath);
            methodNames.addAll(astParser.getAllMethodNames(cu));
        }
        System.out.println(methodNames);
        return methodNames;
    }

    public List<String> getAllFieldDeclarationsFromTopDocuments(List<String> documentsPath) throws IOException {
        AstParser astParser = new AstParser();
        List<String> fieldDeclarations = new ArrayList<>();

        for (String documentPath: documentsPath) {
            CompilationUnit cu = astParser.getCompilationUnit(documentPath);
            fieldDeclarations.addAll(astParser.getAllFieldSignatures(cu));
        }
        System.out.println(fieldDeclarations);
        return fieldDeclarations;
    }

    public static void main(String[] args) throws IOException, ParseException {
        PseudoRelevanceFeedback feedback = new PseudoRelevanceFeedback();
        FileReader fileReader = new FileReader();
        String query = fileReader.readFile("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/BR-Raw/tomcat70/38216.txt");

        List<String> normalizedText = feedback.normalizeText(query);

        String baselineQuery = feedback.listToString(normalizedText);
        System.out.println(baselineQuery);
        List<String> topDocumentsPath = feedback.getTopDocsFromBaselineQuery(baselineQuery);

        List<String> methodDeclarations = feedback.getAllMethodNamesFromTopDocuments(topDocumentsPath);
        List<String> fieldDeclarations = feedback.getAllFieldDeclarationsFromTopDocuments(topDocumentsPath);
    }
}
