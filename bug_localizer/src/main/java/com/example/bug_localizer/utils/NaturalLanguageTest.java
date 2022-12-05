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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class NaturalLanguageTest {
    public static void main(String[] args) throws IOException, ParseException {
        FileReader fileReader = new FileReader();
        ResultValidation validation = new ResultValidation();
        String bugIdsReport = fileReader.readFile("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/BLIZZARD/Result-Matched-Indices/ecf/proposed-NL.txt");
        List<String> bugIds = validation.getAllBugsIdOfBugType(bugIdsReport);

        String fileContent = fileReader.readFile("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/Lucene-Index2File-Mapping/ecf.ckeys");
        Map<String, String> fileNoAndNameMap = validation.getFileNameAndNumber(fileContent);

        File resultMap = new File("natural_language.txt");
        resultMap.createNewFile();

        bugIds.forEach(bugId -> {
            String bugReport = null;
            try {
                String bugReportLocation = "/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/BR-Raw/ecf/" + bugId + ".txt";
                bugReport = fileReader.readFile(bugReportLocation);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            PseudoRelevanceFeedback feedback = new PseudoRelevanceFeedback();
            List<String> normalizedText = null;
            try {
                normalizedText = feedback.normalizeText(bugReport);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String baselineQuery = feedback.listToString(normalizedText);
            System.out.println(baselineQuery);
            List<String> topDocumentsPath = null;
            try {
                topDocumentsPath = feedback.getTopDocsFromBaselineQuery(baselineQuery);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            List<String> methodDeclarations = null;
            try {
                methodDeclarations = feedback.getAllMethodNamesFromTopDocuments(topDocumentsPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            List<String> fieldDeclarations = null;
            try {
                fieldDeclarations = feedback.getAllFieldDeclarationsFromTopDocuments(topDocumentsPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Set<String> methodsAndFieldsList = new HashSet<>();
            methodsAndFieldsList.addAll(methodDeclarations);
//            methodsAndFieldsList.addAll(fieldDeclarations);

            System.out.println(methodDeclarations.size());
            System.out.println(fieldDeclarations.size());
            System.out.println(methodsAndFieldsList.size());
            System.out.println(methodsAndFieldsList);

            List<String> splittedList = new ArrayList<>();
            Regex regexTest = new Regex();
            TextNormalizer textNormalizerTest = null;
            try {
                textNormalizerTest = new TextNormalizer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            TextNormalizer finalTextNormalizerTest = textNormalizerTest;
            methodsAndFieldsList.forEach(list -> {
                try {
                    String splittedSentence = finalTextNormalizerTest.removeStopWordsAndJavaKeywords(regexTest.splitCamelCase(list))
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
            int size = pageRanksMap.size();
            if(size > 15){
                pageRanksMap.keySet().removeAll(Arrays.asList(pageRanksMap.keySet().toArray()).subList(15, size));
            }
            System.out.println(pageRanksMap);
            String searchQuery = pageRanksMap.entrySet().stream().map(Map.Entry:: getKey).collect(Collectors.joining(" "));
            System.out.println(searchQuery.trim());

            Searcher searcher = null;
            try {
                searcher = new Searcher(StaticData.indexDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            TopDocs hits = null;
            try {
                hits = searcher.search(searchQuery, 20);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(hits.totalHits + " documents found. ");

            String changedFilesContent = null;
            try {
                String changedFileContentPath = "/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/Goldset/ecf/" + bugId + ".txt";
                changedFilesContent = fileReader.readFile(changedFileContentPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            List<String> changedFilesList = validation.changedFilesList(changedFilesContent);

            List<String> buggyFiles = new ArrayList<>();
            int i = 0;
            String content = "";

            for(ScoreDoc scoreDoc: hits.scoreDocs) {
                content = "";
                Document document = null;
                try {
                    document = Searcher.indexSearcher.doc(scoreDoc.doc);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String buggyFileName = fileNoAndNameMap.get(document.get("filename"));
                System.out.println(buggyFileName);
                buggyFiles.add(buggyFileName);

//                System.out.println(i + 1);
                content = bugId + "    ";
                if(changedFilesList.contains(buggyFileName)) {
                    content += i + 1 + "\n";
                    break;
                }
                content += "\n";

                i++;
//                System.out.println("File: " + document.get("filename") + " Score: " + scoreDoc.score);
            }
            BufferedWriter out = null;
            try {
                FileWriter fstream = new FileWriter("natural_language.txt", true); //true tells to append data.
                out = new BufferedWriter(fstream);

                out.write(content);
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
