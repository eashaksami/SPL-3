package com.example.bug_localizer.utils;

import com.example.bug_localizer.staticData.StaticData;
import com.example.bug_localizer.test.Regex;
import com.example.bug_localizer.test.TextNormalizer;
import com.example.bug_localizer.utils.graph.CreateGraphFromNaturalLanguage;
import com.example.bug_localizer.utils.lucene.Searcher;
import com.example.bug_localizer.utils.pageRank.CalculatePageRank;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class NaturalLanguageTest {
    public static void main(String[] args) throws IOException, ParseException {
        ClassifyBugReport classifyBugReport = new ClassifyBugReport();
        FileReader fileReader = new FileReader();
        String bugReport = fileReader.readFile("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/BR-Raw/tomcat70/49758.txt");

        PseudoRelevanceFeedback feedback = new PseudoRelevanceFeedback();
        List<String> normalizedText = feedback.normalizeText(bugReport);

        String baselineQuery = feedback.listToString(normalizedText);
        System.out.println(baselineQuery);
        List<String> topDocumentsPath = feedback.getTopDocsFromBaselineQuery(baselineQuery);

        List<String> methodDeclarations = feedback.getAllMethodNamesFromTopDocuments(topDocumentsPath);
        List<String> fieldDeclarations = feedback.getAllFieldDeclarationsFromTopDocuments(topDocumentsPath);

        Set<String> methodsAndFieldsList = new HashSet<>();
        methodsAndFieldsList.addAll(methodDeclarations);
        methodsAndFieldsList.addAll(fieldDeclarations);

        System.out.println(methodDeclarations.size());
        System.out.println(fieldDeclarations.size());
        System.out.println(methodsAndFieldsList.size());
        System.out.println(methodsAndFieldsList);

        List<String> splittedList = new ArrayList<>();
        Regex regexTest = new Regex();
        TextNormalizer textNormalizerTest = new TextNormalizer();
        methodsAndFieldsList.forEach(list -> {
            try {
                String splittedSentence = textNormalizerTest.removeStopWordsAndJavaKeywords(regexTest.splitCamelCase(list))
                        .stream().collect(Collectors.joining(" "));
                splittedList.add(splittedSentence);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(splittedList);

        CreateGraphFromNaturalLanguage graphFromNaturalLanguage = new CreateGraphFromNaturalLanguage();
        Map<Integer, String> naturalLanguageMap = graphFromNaturalLanguage.representStringToMap(splittedList);
        int[][] naturalLanguageGraph = graphFromNaturalLanguage.representGraphAsMatrix(splittedList, naturalLanguageMap);
        graphFromNaturalLanguage.printTraceGraph(naturalLanguageGraph, naturalLanguageMap);


        CalculatePageRank calculatePageRank = new CalculatePageRank();
        double [] pageRanks = calculatePageRank.pageRank(naturalLanguageGraph);
        Map<String, Double> pageRanksMap = new HashMap<>();
        for (int i = 0; i < pageRanks.length; i++) {
            pageRanksMap.put(naturalLanguageMap.get(i), pageRanks[i]);
            System.out.println("Page Rank at: " + naturalLanguageMap.get(i) + " "+pageRanks[i]);
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

        Searcher searcher = new Searcher(StaticData.indexDir);

        TopDocs hits = searcher.search(searchQuery);
        System.out.println(hits.totalHits + " documents found. ");

        for(ScoreDoc scoreDoc: hits.scoreDocs) {
            Document document = Searcher.indexSearcher.doc(scoreDoc.doc);
            System.out.println("File: " + document.get("filepath") + "Score: " + scoreDoc.score);
        }
    }
}
