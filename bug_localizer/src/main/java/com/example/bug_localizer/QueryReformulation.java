package com.example.bug_localizer;

import com.example.bug_localizer.staticData.StaticData;
import com.example.bug_localizer.test.Searcher;
import com.example.bug_localizer.test.TextNormalizerTest;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class QueryReformulation {
    public List<String> normalizeText(String query) throws IOException {
        TextNormalizerTest textNormalizerTest = new TextNormalizerTest();
        return textNormalizerTest.removeStopWords(query);
    }

    public String listToString(List<String> stringList) {
        return stringList.stream().collect(Collectors.joining(" "));
    }

    public void getTopDocsFromBaselineQuery(String baselineQuery) throws IOException, ParseException {
        Searcher searcher = new Searcher(StaticData.indexDir);

        TopDocs hits = searcher.search(baselineQuery);
        System.out.println(hits.totalHits + " documents found. ");

        for(ScoreDoc scoreDoc: hits.scoreDocs) {
            Document document = Searcher.indexSearcher.doc(scoreDoc.doc);
            System.out.println("File: " + document.get("path") + " Score: " + scoreDoc.score);
        }
    }

    public static void main(String[] args) throws IOException, ParseException {
        QueryReformulation reformulation = new QueryReformulation();
        FileReader fileReader = new FileReader();
        String query = fileReader.readFileFromBugReport("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/BR-Raw/tomcat70/38216.txt");

        List<String> normalizedText = reformulation.normalizeText(query);

        String baselineQuery = reformulation.listToString(normalizedText);
        System.out.println(baselineQuery);
        reformulation.getTopDocsFromBaselineQuery(baselineQuery);
    }
}
