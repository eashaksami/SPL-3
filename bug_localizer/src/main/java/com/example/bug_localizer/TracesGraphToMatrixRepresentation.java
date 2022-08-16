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

    public void representGraphAsMatrix() {
        String previousClass = null;
        String previousMethod = null;

        int graph[][] = new int[][]{};
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
        System.out.println(tracesMap);
        return null;
    }

    public static void main(String[] args) throws IOException {
        TracesGraphToMatrixRepresentation graphToMatrixRepresentation = new TracesGraphToMatrixRepresentation();
        ReadFile readFile = new ReadFile();
        String bugReport = readFile.readFileFromBugReport("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018-v1.1/BR-Raw/eclipse.jdt.core/46084.txt");
        ClassifyBugReport classifyBugReport = new ClassifyBugReport();
        List<String> traces = classifyBugReport.getAllStackTraces(bugReport);
        graphToMatrixRepresentation.representStringToMap(
                classifyBugReport.getAllClassesFromStackTraces(traces),
                classifyBugReport.getAllMethodsFromStackTraces(traces));
    }
}
