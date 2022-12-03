package com.example.bug_localizer.service;

import com.example.bug_localizer.test.Regex;
import com.example.bug_localizer.test.TextNormalizer;
import com.example.bug_localizer.utils.PseudoRelevanceFeedback;
import com.example.bug_localizer.utils.graph.CreateGraphForProgramElement;
import com.example.bug_localizer.utils.graph.CreateGraphFromNaturalLanguage;
import com.example.bug_localizer.utils.pageRank.CalculatePageRank;
import com.example.bug_localizer.utils.ClassifyBugReport;
import com.example.bug_localizer.utils.graph.CreateGraphFromStackTrace;
import com.example.bug_localizer.utils.FileReader;
import com.example.bug_localizer.utils.lucene.LuceneIndexer;
import com.example.bug_localizer.utils.lucene.Searcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BugLocalizationService {
    public List<String> getStackTraceBuggyFiles(String indexDirectory, String bugReportContent, int noOfBuggyFiles) throws IOException, ParseException {
        ClassifyBugReport classifyBugReport = new ClassifyBugReport();
        List<String> traces = classifyBugReport.getAllStackTraces(bugReportContent);
//        System.out.println("Type is ST");
        List<String> classes = classifyBugReport.getAllClassesFromStackTraces(traces);
        List<String> methods = classifyBugReport.getAllMethodsFromStackTraces(traces);
        System.out.println(classes);
        System.out.println(methods);
        CreateGraphFromStackTrace stackTraceGraph = new CreateGraphFromStackTrace();

        Map<Integer, String> tracesMap = stackTraceGraph.representStringToMap(classes, methods);
        int[][] traceGraph = stackTraceGraph.representGraphAsMatrix(traces, tracesMap);
//        System.out.println(traceGraph);
        Map<String, Double> pageRanksMap = getPageRankMap(traceGraph, tracesMap);

        System.out.println(pageRanksMap);
        String searchQuery = getSearchQuery(pageRanksMap);
        System.out.println(searchQuery.trim());

        List<String> files = getTopDocuments(indexDirectory, searchQuery, noOfBuggyFiles);

        return files;
    }

    public List<String> getProgramElementBuggyFiles(String indexDirectory, String bugReportContent, int noOfBuggyFiles) throws IOException, ParseException {
        TextNormalizer textNormalizer = new TextNormalizer();
        String processedBugReport = textNormalizer.extractProgramElements(bugReportContent);
        System.out.println(processedBugReport);
        List<String> sentences = Arrays.stream(processedBugReport.split("\\.")).toList();
        sentences = textNormalizer.removeStopWords(sentences);
        System.out.println(sentences);

        CreateGraphForProgramElement graphForProgramElement = new CreateGraphForProgramElement();
        Map<Integer, String> wordMap = graphForProgramElement.representStringToMap(sentences);
        int graph[][] = new int[wordMap.size()][wordMap.size()];
        System.out.println(wordMap);
        graph = graphForProgramElement.createPosGraph(sentences, wordMap, graph);

        Map<String, Double> pageRanksMap = getPageRankMap(graph, wordMap);

        System.out.println(pageRanksMap);
        String searchQuery = getSearchQuery(pageRanksMap);
        System.out.println(searchQuery.trim());

        List<String> files = getTopDocuments(indexDirectory, searchQuery, noOfBuggyFiles);

        return files;
    }

    public List<String> getNaturalLanguageBuggyFiles(String indexDirectory, String bugReportContent, int noOfBuggyFiles) throws IOException, ParseException {
        PseudoRelevanceFeedback feedback = new PseudoRelevanceFeedback();
        List<String> normalizedText = feedback.normalizeText(bugReportContent);

        String baselineQuery = feedback.listToString(normalizedText);
        System.out.println(baselineQuery);
        List<String> topDocumentsPath = feedback.getTopDocsFromBaselineQuery(baselineQuery);

        List<String> methodDeclarations = feedback.getAllMethodNamesFromTopDocuments(topDocumentsPath);
        List<String> fieldDeclarations = feedback.getAllFieldDeclarationsFromTopDocuments(topDocumentsPath);

        Set<String> methodsAndFieldsList = new HashSet<>();
        methodsAndFieldsList.addAll(methodDeclarations);
        /**
         * if add field declarations then it adds extra noise which decrease the result accuracy
         * */
//        methodsAndFieldsList.addAll(fieldDeclarations);

        System.out.println(methodDeclarations.size());
        System.out.println(fieldDeclarations.size());
        System.out.println(methodsAndFieldsList.size());
        System.out.println(methodsAndFieldsList);

        List<String> splittedList = new ArrayList<>();
        Regex regex = new Regex();
        TextNormalizer textNormalizer = new TextNormalizer();
        methodsAndFieldsList.forEach(list -> {
            try {
                String splittedSentence = textNormalizer.removeStopWordsAndJavaKeywords(regex.splitCamelCase(list))
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

        Map<String, Double> pageRanksMap = getPageRankMap(naturalLanguageGraph, naturalLanguageMap);

        System.out.println(pageRanksMap);
        String searchQuery = getSearchQuery(pageRanksMap);
        System.out.println(searchQuery.trim());

        List<String> files = getTopDocuments(indexDirectory, searchQuery, noOfBuggyFiles);

        return files;
    }

    public Map<String, Double> getPageRankMap(int[][] graph, Map<Integer, String> stringMap) {
        CalculatePageRank calculatePageRank = new CalculatePageRank();
        double [] pageRanks = calculatePageRank.pageRank(graph);
        Map<String, Double> pageRanksMap = new HashMap<>();
        for (int i = 0; i < pageRanks.length; i++) {
            pageRanksMap.put(stringMap.get(i), pageRanks[i]);
            System.out.println("Page Rank at: " + stringMap.get(i) + " "+pageRanks[i]);
        }
        pageRanksMap = sortPageRankMap(pageRanksMap);
        return pageRanksMap;
    }

    public Map<String, Double> sortPageRankMap(Map<String, Double> pageRankMap) {
        pageRankMap = pageRankMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
        pageRankMap.keySet().removeIf(Objects::isNull);
        return pageRankMap;
    }

    public String getSearchQuery(Map<String, Double> pageRankMap) {
        int size = pageRankMap.size();
        pageRankMap.keySet().removeAll(Arrays.asList(pageRankMap.keySet().toArray()).subList(10, size));
//        System.out.println(pageRankMap);
        return pageRankMap.entrySet().stream().map(Map.Entry:: getKey).collect(Collectors.joining(" "));
    }

    public List<String> getTopDocuments(String indexDirectory, String searchQuery, int noOfBuggyFiles) throws IOException, ParseException {
        Searcher searcher = new Searcher(indexDirectory);

        TopDocs hits = searcher.search(searchQuery, noOfBuggyFiles);

        System.out.println(hits.totalHits + " documents found. ");
        int i = 0;
        List<String> files = new ArrayList<>();
        for(ScoreDoc scoreDoc: hits.scoreDocs) {
            Document document = Searcher.indexSearcher.doc(scoreDoc.doc);
            files.add(document.get("filepath"));
            System.out.println("File: " +i + " " + document.get("filepath") + "Score: " + scoreDoc.score);
            i++;
        }
        return files;
    }

    public String createLuceneIndexDirectory(String directory) throws IOException {
        Files.createDirectories(Paths.get(directory + "/index"));
        System.out.println("Start index creation");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        String indexDirectory = directory + "/index_" + timeStamp;
        LuceneIndexer luceneIndexer = new LuceneIndexer(indexDirectory);
        luceneIndexer.createIndex(directory);
        System.out.println("End index creation");
        return indexDirectory;
    }

    public List<String> getBuggyFiles(String directory, MultipartFile bugReport, int noOfBuggyFiles) throws IOException, ParseException {
        ClassifyBugReport classifyBugReport = new ClassifyBugReport();
        FileReader fileReader = new FileReader();
        String bugReportContent = fileReader.readMultipartFile(bugReport);

        String indexDirectory = createLuceneIndexDirectory(directory);

        if(classifyBugReport.haveStackTrace(bugReportContent)) {
            return getStackTraceBuggyFiles(indexDirectory, bugReportContent, noOfBuggyFiles);
        } else if(classifyBugReport.haveProgramElements(bugReportContent)) {
            return getProgramElementBuggyFiles(indexDirectory, bugReportContent, noOfBuggyFiles);
        } else {
            return getNaturalLanguageBuggyFiles(indexDirectory, bugReportContent, noOfBuggyFiles);
        }
    }
}
