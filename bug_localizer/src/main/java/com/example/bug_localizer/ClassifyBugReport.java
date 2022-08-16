package com.example.bug_localizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassifyBugReport {

    public boolean haveStackTrace(String bugReport) {
        Pattern p = Pattern.compile("(.*)?(.+)\\.(.+)(\\((.+)\\.java:\\d+\\)|\\(Unknown Source\\)|\\(Native Method\\))");
        Matcher m = p.matcher(bugReport);
        return m.matches();
    }

    public List<String> getAllClassesFromStackTraces(List<String> traces) {
        List<String> classes = new ArrayList<>();
        traces.forEach(t -> {
            List<String> splits = Arrays.stream(t.split("\\.")).toList();
            String className = splits.get(splits.size()-2);
            classes.add(className);
        });
        return classes;
    }

    public List<String> getAllMethodsFromStackTraces(List<String> traces) {
        List<String> methods = new ArrayList<>();
        traces.forEach(t -> {
            List<String> splits = Arrays.stream(t.split("\\.")).toList();
            String methodName = splits.get(splits.size()-1);
            methods.add(methodName);
        });
        return methods;
    }

    public static void main(String[] args) {
        ClassifyBugReport classifyBugReport = new ClassifyBugReport();
        boolean haveTraces = classifyBugReport.haveStackTrace("bug report content");
        if(haveTraces) {
            System.out.println("Type is ST");
            List<String> traces = new ArrayList<>();
            traces.add("java.lang.Class.getDeclaredMethods0");
            traces.add("sun.launcher.LauncherHelper.validateMainClass");
            List<String> classes = classifyBugReport.getAllClassesFromStackTraces(traces);
            List<String> methods = classifyBugReport.getAllMethodsFromStackTraces(traces);
            System.out.println(classes);
            System.out.println(methods);
        }
    }

}
