package com.example.bug_localizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CalculatePageRank {

    public void pageRank(int[][] graph) {
        double[] prevWeightedVertexArray = new double[graph.length];
        double[] newWeightedVertexArray = new double[graph.length];

        for(int i = 0; i < graph.length; i++) {
            prevWeightedVertexArray[i] = .25;
            newWeightedVertexArray[i] = .25;
        }

        for(int i = 0; i < graph.length; i++) {
            int[] incomingEdgesIndex = new int[graph.length];
            int[] insignificant = new int[graph.length];
            double totalIncomingScore = 0;
            for(int j = 0; j < graph.length; j++) {
                incomingEdgesIndex[j] = graph[j][i]; //column will be incoming edges so [j][i]
//                System.out.println(incomingEdgesIndex[j]);

                if(incomingEdgesIndex[j] == 1) {
                    int sumOfOutDegreeEdges = Arrays.stream(graph[j]).sum();
//                System.out.println(sumOfOutDegreeEdges);
                    double weight = prevWeightedVertexArray[j];
                    if(sumOfOutDegreeEdges != 0)
                        totalIncomingScore += weight/sumOfOutDegreeEdges;
                }
            }
            totalIncomingScore = totalIncomingScore*.85;
            totalIncomingScore += .15;
            System.out.println(totalIncomingScore);
        }
    }


    public static void main(String[] args) throws IOException {
//        TracesGraphToMatrixRepresentation graphToMatrixRepresentation = new TracesGraphToMatrixRepresentation();
//        ReadFile readFile = new ReadFile();
//        String bugReport = readFile.readFileFromBugReport("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/BR-Raw/eclipse.jdt.core/46084.txt");
//        ClassifyBugReport classifyBugReport = new ClassifyBugReport();
//        List<String> traces = classifyBugReport.getAllStackTraces(bugReport);
//        Map<Integer, String> tracesMap = graphToMatrixRepresentation.representStringToMap(
//                classifyBugReport.getAllClassesFromStackTraces(traces),
//                classifyBugReport.getAllMethodsFromStackTraces(traces));
//        int [][] graph = graphToMatrixRepresentation.representGraphAsMatrix(traces, tracesMap);
        int[][] graph = new int[][]{{0,1,1,0,0}, {0,0,0,1,1}, {0,0,0,1,0}, {1,0,0,1,1}, {0,0,0,0,0}};
        CalculatePageRank calculatePageRank = new CalculatePageRank();
        calculatePageRank.pageRank(graph);
    }
}
