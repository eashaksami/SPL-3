package com.example.bug_localizer.utils;

import com.example.bug_localizer.staticData.StaticData;
import com.example.bug_localizer.utils.lucene.Searcher;
import com.example.bug_localizer.test.TextNormalizer;
import com.example.bug_localizer.utils.graph.CreateGraphForProgramElement;
import com.example.bug_localizer.utils.pageRank.CalculatePageRank;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ProgramElementTest {
    public static void main(String[] args) throws IOException, ParseException {
        ClassifyBugReport classifyBugReport = new ClassifyBugReport();
        FileReader fileReader = new FileReader();
        String bugReport = fileReader.readFile("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/BR-Raw/tomcat70/48239.txt");

        TextNormalizer textNormalizer = new TextNormalizer();
        String processedBugReport = textNormalizer.extractProgramElements(bugReport);
        System.out.println(processedBugReport);
        List<String> sentences = Arrays.stream(processedBugReport.split("\\.")).toList();
        sentences = textNormalizer.removeStopWords(sentences);
        System.out.println(sentences);

        CreateGraphForProgramElement graphForProgramElement = new CreateGraphForProgramElement();
        Map<Integer, String> wordMap = graphForProgramElement.representStringToMap(sentences);
        int graph[][] = new int[wordMap.size()][wordMap.size()];
        System.out.println(wordMap);
        graph = graphForProgramElement.createPosGraph(sentences, wordMap, graph);

        CalculatePageRank calculatePageRank = new CalculatePageRank();
        double [] pageRanks = calculatePageRank.pageRank(graph);
        Map<String, Double> pageRanksMap = new HashMap<>();
        for (int i = 0; i < pageRanks.length; i++) {
            pageRanksMap.put(wordMap.get(i), pageRanks[i]);
            System.out.println("Page Rank at: " + wordMap.get(i) + " "+pageRanks[i]);
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
        int size = pageRanksMap.size();
        pageRanksMap.keySet().removeAll(Arrays.asList(pageRanksMap.keySet().toArray()).subList(10, size));
        System.out.println(pageRanksMap);
        String searchQuery = pageRanksMap.entrySet().stream().map(Map.Entry:: getKey).collect(Collectors.joining(" "));
        System.out.println(searchQuery.trim());

        Searcher searcher = new Searcher(StaticData.indexDir);

        TopDocs hits = searcher.search(searchQuery, 10);

        System.out.println(hits.totalHits + " documents found. ");
        int i = 0;
        for(ScoreDoc scoreDoc: hits.scoreDocs) {
            Document document = Searcher.indexSearcher.doc(scoreDoc.doc);
            System.out.println("File: " +i + " " + document.get("filepath") + "Score: " + scoreDoc.score);
            i++;
        }
    }
}
