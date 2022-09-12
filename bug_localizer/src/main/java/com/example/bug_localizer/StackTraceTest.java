package com.example.bug_localizer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class StackTraceTest {
    public static void main(String[] args) throws IOException {
        ClassifyBugReport classifyBugReport = new ClassifyBugReport();
        FileReader fileReader = new FileReader();
        String bugReport = fileReader.readFileFromBugReport("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018-v1.1/BR-Raw/eclipse.jdt.core/46084.txt");

        List<String> traces = classifyBugReport.getAllStackTraces(bugReport);
//        System.out.println("Type is ST");
        List<String> classes = classifyBugReport.getAllClassesFromStackTraces(traces);
        List<String> methods = classifyBugReport.getAllMethodsFromStackTraces(traces);
//        System.out.println(classes);
//        System.out.println(methods);
        TracesGraphToMatrixRepresentation graphToMatrixRepresentation = new TracesGraphToMatrixRepresentation();

        Map<Integer, String> tracesMap = graphToMatrixRepresentation.representStringToMap(classes, methods);
        int[][] traceGraph = graphToMatrixRepresentation.representGraphAsMatrix(traces, tracesMap);
//        System.out.println(traceGraph);
        CalculatePageRank calculatePageRank = new CalculatePageRank();
        double [] pageRanks = calculatePageRank.pageRank(traceGraph);
        for (int i = 0; i < pageRanks.length; i++) {
            System.out.println("Page Rank at: " + tracesMap.get(i) + " "+pageRanks[i]);
        }
    }
}
