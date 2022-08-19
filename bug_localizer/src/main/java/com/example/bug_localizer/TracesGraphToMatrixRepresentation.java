package com.example.bug_localizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TracesGraphToMatrixRepresentation {

    private List<String> classes;
    private List<String> methods;

//    public TracesGraphToMatrixRepresentation(List<String> classes, List<String> methods) {
//        this.classes = classes;
//        this.methods = methods;
//    }

    public int[][] representGraphAsMatrix(List<String> traces, Map<Integer, String> tracesMap) {
        Integer previousClassIndex = null;
        Integer previousMethodIndex = null;

        int graph[][] = new int[100][100];

        for(String trace : traces) {
            ClassifyBugReport classifyBugReport = new ClassifyBugReport();
            String className = classifyBugReport.getClassFromStackTrace(trace);
            String methodName = classifyBugReport.getMethodFromStackTrace(trace);
            Integer classIndex=0, methodIndex=0;

            for (Map.Entry<Integer, String> entry : tracesMap.entrySet()) {
                if (entry.getValue().equals(className)) {
                    classIndex = entry.getKey();
//                    System.out.println(entry.getKey());
                }
                if (entry.getValue().equals(methodName)) {
                    methodIndex = entry.getKey();
//                    System.out.println(entry.getKey());
                }
            }
            graph[classIndex][methodIndex] = 1;
            graph[methodIndex][classIndex] = 1;

            if(previousClassIndex != null) {
                graph[classIndex][previousClassIndex] = 1;
            }

            if(previousMethodIndex != null) {
                graph[methodIndex][previousMethodIndex] = 1;
            }

            previousClassIndex = classIndex;
            previousMethodIndex = methodIndex;
        }
//        for(int i = 0; i < 100; i++) {
//            for(int j = 0; j < 100; j++)
//                System.out.println(graph[i][j] + " ");
//            System.out.println();
//        }
        printTraceGraph(graph, tracesMap);
        return graph;
    }

    public void printTraceGraph(int[][] graph, Map<Integer, String> tracesMap) {
        for(int i = 0; i < tracesMap.size(); i++) {
            for(int j = 0; j < tracesMap.size(); j++) {
                if(graph[i][j] == 1) {
                    System.out.println("Graph value: " + tracesMap.get(i) + "=>" + tracesMap.get(j));
                }
            }
        }
    }

    public Map<Integer, String> representStringToMap(List<String> classes, List<String> methods) {
        System.out.println(classes);
        System.out.println(methods);
        Map<Integer, String> tracesMap = new HashMap<>();
        Integer i = 0;

        Iterator<String> itrClasses = classes.iterator();
        Iterator<String> itrMethod = methods.iterator();
        while(itrClasses.hasNext() && itrMethod.hasNext()) {
            tracesMap.put(i, itrClasses.next());
            tracesMap.put(i+1, itrMethod.next());
            i +=2;
        }
//        System.out.println(tracesMap);
        return tracesMap;
    }

    public static void main(String[] args) throws IOException {
        TracesGraphToMatrixRepresentation graphToMatrixRepresentation = new TracesGraphToMatrixRepresentation();
        ReadFile readFile = new ReadFile();
        String bugReport = readFile.readFileFromBugReport("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/BR-Raw/eclipse.jdt.core/46084.txt");
        ClassifyBugReport classifyBugReport = new ClassifyBugReport();
        List<String> traces = classifyBugReport.getAllStackTraces(bugReport);
        Map<Integer, String> tracesMap = graphToMatrixRepresentation.representStringToMap(
                classifyBugReport.getAllClassesFromStackTraces(traces),
                classifyBugReport.getAllMethodsFromStackTraces(traces));
        graphToMatrixRepresentation.representGraphAsMatrix(traces, tracesMap);
    }
}
