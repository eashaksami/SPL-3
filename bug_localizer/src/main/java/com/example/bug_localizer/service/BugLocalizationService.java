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

    private final ClassifyBugReport classifyBugReport;
    private final FileReader fileReader;
    private final CreateGraphFromStackTrace createGraphFromStackTrace;
    private final CreateGraphForProgramElement createGraphForProgramElement;
    private final PseudoRelevanceFeedback pseudoRelevanceFeedback;
    private final CreateGraphFromNaturalLanguage createGraphFromNaturalLanguage;
    private final CalculatePageRank calculatePageRank;
    private BugLocalizationService(ClassifyBugReport classifyBugReport,
                                   FileReader fileReader,
                                   CreateGraphFromStackTrace createGraphFromStackTrace,
                                   CreateGraphForProgramElement createGraphForProgramElement,
                                   PseudoRelevanceFeedback pseudoRelevanceFeedback,
                                   CreateGraphFromNaturalLanguage createGraphFromNaturalLanguage,
                                   CalculatePageRank calculatePageRank) {
        this.classifyBugReport = classifyBugReport;
        this.fileReader = fileReader;
        this.createGraphFromStackTrace = createGraphFromStackTrace;
        this.createGraphForProgramElement = createGraphForProgramElement;
        this.pseudoRelevanceFeedback = pseudoRelevanceFeedback;
        this.createGraphFromNaturalLanguage = createGraphFromNaturalLanguage;
        this.calculatePageRank = calculatePageRank;
    }

    public List<String> getStackTraceBuggyFiles(String indexDirectory, String bugReportContent, int noOfBuggyFiles)
            throws IOException, ParseException {
        List<String> traces = classifyBugReport.getAllStackTraces(bugReportContent);

        List<String> classes = classifyBugReport.getAllClassesFromStackTraces(traces);
        List<String> methods = classifyBugReport.getAllMethodsFromStackTraces(traces);

        Map<Integer, String> tracesMap = createGraphFromStackTrace.representStringToMap(classes, methods);
        int[][] traceGraph = createGraphFromStackTrace.representGraphAsMatrix(traces, tracesMap);

        Map<String, Double> pageRanksMap = getPageRankMap(traceGraph, tracesMap);

        String searchQuery = getSearchQuery(pageRanksMap, "ST", bugReportContent);
        System.out.println(searchQuery.trim());

        return getTopDocuments(indexDirectory, searchQuery, noOfBuggyFiles);
    }

    public List<String> getProgramElementBuggyFiles(String indexDirectory, String bugReportContent, int noOfBuggyFiles) throws IOException, ParseException {
        TextNormalizer textNormalizer = new TextNormalizer();
        String processedBugReport = textNormalizer.extractProgramElements(bugReportContent);
        List<String> sentences = Arrays.stream(processedBugReport.split("\\.")).toList();
        sentences = textNormalizer.removeStopWords(sentences);

        Map<Integer, String> wordMap = createGraphForProgramElement.representStringToMap(sentences);
        int[][] graph = new int[wordMap.size()][wordMap.size()];

        graph = createGraphForProgramElement.createPosGraph(sentences, wordMap, graph);

        Map<String, Double> pageRanksMap = getPageRankMap(graph, wordMap);

        String searchQuery = getSearchQuery(pageRanksMap, "PE", bugReportContent);
        System.out.println(searchQuery.trim());

        return getTopDocuments(indexDirectory, searchQuery, noOfBuggyFiles);
    }

    public List<String> getNaturalLanguageBuggyFiles(String indexDirectory, String bugReportContent, int noOfBuggyFiles)
            throws IOException, ParseException {
        List<String> normalizedText = pseudoRelevanceFeedback.normalizeText(bugReportContent);

        String baselineQuery = pseudoRelevanceFeedback.listToString(normalizedText);

        List<String> topDocumentsPath = pseudoRelevanceFeedback.getTopDocsFromBaselineQuery(baselineQuery);

        List<String> methodDeclarations = pseudoRelevanceFeedback.getAllMethodNamesFromTopDocuments(topDocumentsPath);

        Set<String> methodsAndFieldsList = new HashSet<>(methodDeclarations);
        /**
         * if add field declarations then it adds extra noise which decrease the result accuracy
         * */
//        methodsAndFieldsList.addAll(fieldDeclarations);

        List<String> splittedList = new ArrayList<>();
        Regex regex = new Regex();
        TextNormalizer textNormalizer = new TextNormalizer();
        methodsAndFieldsList.forEach(list -> {
            try {
                String splittedSentence = String.join(" ",
                        textNormalizer.removeStopWordsAndJavaKeywords(regex.splitCamelCase(list)));
                splittedList.add(splittedSentence);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Map<Integer, String> naturalLanguageMap = createGraphFromNaturalLanguage.representStringToMap(splittedList);
        int[][] naturalLanguageGraph = createGraphFromNaturalLanguage.representGraphAsMatrix(splittedList, naturalLanguageMap);
        createGraphFromNaturalLanguage.printTraceGraph(naturalLanguageGraph, naturalLanguageMap);

        Map<String, Double> pageRanksMap = getPageRankMap(naturalLanguageGraph, naturalLanguageMap);

        String searchQuery = getSearchQuery(pageRanksMap, "NL", bugReportContent);
        System.out.println(searchQuery.trim());

        return getTopDocuments(indexDirectory, searchQuery, noOfBuggyFiles);
    }

    public Map<String, Double> getPageRankMap(int[][] graph, Map<Integer, String> stringMap) {
        double [] pageRanks = calculatePageRank.pageRank(graph);
        Map<String, Double> pageRanksMap = new HashMap<>();
        for (int i = 0; i < pageRanks.length; i++) {
            pageRanksMap.put(stringMap.get(i), pageRanks[i]);
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

    public String getSearchQuery(Map<String, Double> pageRankMap, String reportType, String bugReport) {
        int size = pageRankMap.size();
        if(reportType.equals("ST")) {
            if(size > 10) {
                Arrays.asList(pageRankMap.keySet().toArray()).subList(10, size).forEach(pageRankMap.keySet()::remove);
            }
        } else if(reportType.equals("PE")) {
            if(size > 20) {
                Arrays.asList(pageRankMap.keySet().toArray()).subList(10, size).forEach(pageRankMap.keySet()::remove);
            }
        } else {
            if(size > 15) {
                Arrays.asList(pageRankMap.keySet().toArray()).subList(15, size).forEach(pageRankMap.keySet()::remove);
            }
        }

        String searchQuery = "";

        try {
            searchQuery = pseudoRelevanceFeedback.getNormalizedBugReportTitle(bugReport);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        searchQuery += " " + String.join(" ", pageRankMap.keySet());
        return searchQuery;
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
            i++;
        }
        return files;
    }

    public String createLuceneIndexDirectory(String directory) throws IOException {
        Files.createDirectories(Paths.get(directory + "/index"));

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        String indexDirectory = directory + "/index_" + timeStamp;
        LuceneIndexer luceneIndexer = new LuceneIndexer(indexDirectory);
        luceneIndexer.createIndex(directory);

        return indexDirectory;
    }

    public List<String> getBuggyFiles(String directory, MultipartFile bugReport, int noOfBuggyFiles)
            throws IOException, ParseException {
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
