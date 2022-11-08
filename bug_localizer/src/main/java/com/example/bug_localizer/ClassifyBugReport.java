package com.example.bug_localizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClassifyBugReport {

    public boolean haveStackTrace(String bugReport) {
        Pattern p = Pattern.compile("(.*)?(.+)\\.(.+)(\\((.+)\\.java:\\d+\\)|\\(Unknown Source\\)|\\(Native Method\\))");
        Matcher m = p.matcher(bugReport);
        return m.matches();
    }

    public List<String> getAllStackTraces(String bugReport) {
        List<String> traces = new ArrayList<>();
        Pattern p = Pattern.compile("(.*)?(.+)\\.(.+)(\\((.+)\\.java:\\d+\\)|\\(Unknown Source\\)|\\(Native Method\\))");
        Matcher m = p.matcher(bugReport);
        while (m.find()) {
            String matchedPart = m.group();
            matchedPart = m.group().substring(0, matchedPart.indexOf("("));
            traces.add(matchedPart);
        }
        System.out.println(traces);
        return traces;
    }

    public List<String> getAllClassesFromStackTraces(List<String> traces) {
        List<String> classes = new ArrayList<>();
        traces.forEach(t -> {
            List<String> splits = Arrays.stream(t.split("\\.")).toList();
            String className = splits.get(splits.size()-2);
            classes.add(className);
        });
        List<String> uniqueClasses = classes.stream().distinct().collect(Collectors.toList());
        return uniqueClasses;
    }

    public List<String> getAllMethodsFromStackTraces(List<String> traces) {
        List<String> methods = new ArrayList<>();
        traces.forEach(t -> {
            List<String> splits = Arrays.stream(t.split("\\.")).toList();
            String methodName = splits.get(splits.size()-1);
            methods.add(methodName);
        });
        List<String> uniqueMethods = methods.stream().distinct().collect(Collectors.toList());
        return uniqueMethods;
    }

    public String getMethodFromStackTrace(String traces) {
            List<String> splits = Arrays.stream(traces.split("\\.")).toList();
            return splits.get(splits.size()-1);
    }

    public String getClassFromStackTrace(String traces) {
        List<String> splits = Arrays.stream(traces.split("\\.")).toList();
        return splits.get(splits.size()-2);
    }

    public static void main(String[] args) throws IOException {
        ClassifyBugReport classifyBugReport = new ClassifyBugReport();
        FileReader fileReader = new FileReader();
        String bugReport = fileReader.readFile("/home/sami/Desktop/SPL-3/BLIZZARD-Replication-Package-ESEC-FSE2018/BR-Raw/eclipse.jdt.core/46084.txt");

        List<String> traces = classifyBugReport.getAllStackTraces(bugReport);
        if(traces != null) {
            System.out.println("Type is ST");
            List<String> classes = classifyBugReport.getAllClassesFromStackTraces(traces);
            List<String> methods = classifyBugReport.getAllMethodsFromStackTraces(traces);
            System.out.println(classes);
            System.out.println(methods);
        }
    }

}
