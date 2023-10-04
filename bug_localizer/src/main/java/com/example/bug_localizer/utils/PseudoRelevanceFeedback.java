package com.example.bug_localizer.utils;

import com.example.bug_localizer.staticData.StaticData;
import com.example.bug_localizer.test.AstParser;
import com.example.bug_localizer.test.TextNormalizer;
import com.example.bug_localizer.utils.lucene.Searcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PseudoRelevanceFeedback {
    public String getNormalizedBugReportTitle(String bugReport) throws IOException {
        TextNormalizer normalizer = new TextNormalizer();
        PseudoRelevanceFeedback feedback = new PseudoRelevanceFeedback();
        List<String> title = new ArrayList<>();
        String[] lines = bugReport.split("\\r?\\n");
        title.add(lines[0]);
        title = normalizer.removeStopWords(lines[0]);
        return feedback.listToString(title);
    }

    public List<String> normalizeText(String query) throws IOException {
        TextNormalizer textNormalizer = new TextNormalizer();
        return textNormalizer.removeStopWords(query);
    }

    public String listToString(List<String> stringList) {
        return String.join(" ", stringList);
    }

    public List<String> getTopDocsFromBaselineQuery(String baselineQuery) throws IOException, ParseException {
        Searcher searcher = new Searcher(StaticData.indexDir);
        List<String> topDocumentsPath = new ArrayList<>();

        TopDocs hits = searcher.search(baselineQuery, 10);
        System.out.println(hits.totalHits + " documents found. ");

        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document document = Searcher.indexSearcher.doc(scoreDoc.doc);
            System.out.println("File: " + document.get("filepath") + " Score: " + scoreDoc.score);
            topDocumentsPath.add(document.get("filepath"));
        }
        return topDocumentsPath;
    }

    public List<String> getAllMethodNamesFromTopDocuments(List<String> documentsPath) throws IOException {
        AstParser astParser = new AstParser();
        List<String> methodNames = new ArrayList<>();

        for (String documentPath : documentsPath) {
            CompilationUnit cu = astParser.getCompilationUnit(documentPath);
            methodNames.addAll(astParser.getAllMethodNames(cu));
        }
        System.out.println(methodNames);
        return methodNames;
    }

    public List<String> getAllFieldDeclarationsFromTopDocuments(List<String> documentsPath) throws IOException {
        AstParser astParser = new AstParser();
        List<String> fieldDeclarations = new ArrayList<>();

        for (String documentPath : documentsPath) {
            CompilationUnit cu = astParser.getCompilationUnit(documentPath);
            fieldDeclarations.addAll(astParser.getAllFieldSignatures(cu));
        }
        System.out.println(fieldDeclarations);
        return fieldDeclarations;
    }
}
