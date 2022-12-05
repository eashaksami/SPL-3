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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ProgramElementTest {
    public static void main(String[] args) throws IOException, ParseException {
        FileReader fileReader = new FileReader();
        ResultValidation validation = new ResultValidation();
        String bugIdsReport = fileReader.readFile("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/BLIZZARD/Result-Matched-Indices/tomcat70/proposed-PE.txt");
        List<String> bugIds = validation.getAllBugsIdOfBugType(bugIdsReport);

        String fileContent = fileReader.readFile("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/Lucene-Index2File-Mapping/tomcat70.ckeys");
        Map<String, String> fileNoAndNameMap = validation.getFileNameAndNumber(fileContent);

        File resultMap = new File("program_element.txt");
        resultMap.createNewFile();

        bugIds.forEach(bugId -> {
            String bugReport = null;
            try {
                String bugReportLocation = "/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/BR-Raw/tomcat70/" + bugId + ".txt";
                bugReport = fileReader.readFile(bugReportLocation);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            TextNormalizer textNormalizer = null;
            try {
                textNormalizer = new TextNormalizer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String processedBugReport = textNormalizer.extractProgramElements(bugReport);
            System.out.println(processedBugReport);
            List<String> sentences = Arrays.stream(processedBugReport.split("\\.")).toList();
            try {
                sentences = textNormalizer.removeStopWords(sentences);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(sentences);

            CreateGraphForProgramElement graphForProgramElement = new CreateGraphForProgramElement();
            Map<Integer, String> wordMap = graphForProgramElement.representStringToMap(sentences);
            int graph[][] = new int[wordMap.size()][wordMap.size()];
            System.out.println(wordMap);
            try {
                graph = graphForProgramElement.createPosGraph(sentences, wordMap, graph);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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
            if(size > 10){
                pageRanksMap.keySet().removeAll(Arrays.asList(pageRanksMap.keySet().toArray()).subList(10, size));
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
                String changedFileContentPath = "/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/Goldset/tomcat70/" + bugId + ".txt";
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
//                System.out.println("File: " +i + " " + document.get("filename") + " Score: " + scoreDoc.score);
//                i++;
            }
            BufferedWriter out = null;
            try {
                FileWriter fstream = new FileWriter("program_element.txt", true); //true tells to append data.
                out = new BufferedWriter(fstream);

                out.write(content);
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }
}
