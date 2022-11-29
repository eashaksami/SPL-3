package com.example.bug_localizer.service;

import com.example.bug_localizer.CalculatePageRank;
import com.example.bug_localizer.ClassifyBugReport;
import com.example.bug_localizer.CreateGraphFromStackTrace;
import com.example.bug_localizer.FileReader;
import com.example.bug_localizer.test.LuceneIndexer;
import com.example.bug_localizer.test.Searcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BugLocalizationService {
    public List<String> getBuggyFiles(String directory) throws IOException, ParseException {
        Files.createDirectories(Paths.get(directory + "/index"));
        System.out.println("Start index creation");
        LuceneIndexer luceneIndexer = new LuceneIndexer(directory + "/index");
        luceneIndexer.createIndex(directory);
        System.out.println("End index creation");
        return getFiles(directory + "/index");
    }

    public List<String> getFiles(String indexDirectory) throws IOException, ParseException {
        ClassifyBugReport classifyBugReport = new ClassifyBugReport();
        FileReader fileReader = new FileReader();
        String bugReport = fileReader.readFile("/home/sami/Desktop/st.txt");
        System.out.println(classifyBugReport.haveStackTrace("/home/sami/Desktop/st.txt"));

        List<String> traces = classifyBugReport.getAllStackTraces(bugReport);
//        System.out.println("Type is ST");
        List<String> classes = classifyBugReport.getAllClassesFromStackTraces(traces);
        List<String> methods = classifyBugReport.getAllMethodsFromStackTraces(traces);
        System.out.println(classes);
        System.out.println(methods);
        CreateGraphFromStackTrace stackTraceGraph = new CreateGraphFromStackTrace();

        Map<Integer, String> tracesMap = stackTraceGraph.representStringToMap(classes, methods);
        int[][] traceGraph = stackTraceGraph.representGraphAsMatrix(traces, tracesMap);
//        System.out.println(traceGraph);
        CalculatePageRank calculatePageRank = new CalculatePageRank();
        double [] pageRanks = calculatePageRank.pageRank(traceGraph);
        Map<String, Double> pageRanksMap = new HashMap<>();
        for (int i = 0; i < pageRanks.length; i++) {
            pageRanksMap.put(tracesMap.get(i), pageRanks[i]);
            System.out.println("Page Rank at: " + tracesMap.get(i) + " "+pageRanks[i]);
        }

        pageRanksMap = pageRanksMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
        pageRanksMap.keySet().removeIf(Objects::isNull);
        System.out.println(pageRanksMap);
        String searchQuery = pageRanksMap.entrySet().stream().map(Map.Entry:: getKey).collect(Collectors.joining(" "));
        System.out.println(searchQuery.trim());

        Searcher searcher = new Searcher(indexDirectory);

        TopDocs hits = searcher.search(searchQuery);
        System.out.println(hits.totalHits + " documents found. ");

        List<String> files = new ArrayList<>();
        for(ScoreDoc scoreDoc: hits.scoreDocs) {
            Document document = Searcher.indexSearcher.doc(scoreDoc.doc);
            files.add(document.get("filepath"));
            System.out.println("File: " + document.get("filepath") + "Score: " + scoreDoc.score);
        }
        return files;
    }
}
