package com.example.bug_localizer.utils;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ClassifyBugReport {

    public boolean haveStackTrace(String bugReport) {
        Pattern p = Pattern.compile("(.*)?(.+)\\.(.+)(\\((.+)\\.java:\\d+\\)|\\(Unknown Source\\)|\\(Native Method\\))");
        Matcher m = p.matcher(bugReport);
        return m.find();
    }

    public boolean haveProgramElements(String bugReport) {
        Pattern p = Pattern.compile("((\\w+)?\\.[\\s\\n\\r]*[\\w]+)[\\s\\n\\r]*(?=\\(.*\\))|([A-Z][a-z0-9]+){2,}");
        Matcher m = p.matcher(bugReport);
        return m.find();
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
            String className = null;
            if (splits.size() >= 2) {
                className = splits.get(splits.size() - 2);
            }
            classes.add(className);
        });
        return classes.stream().distinct().collect(Collectors.toList());
    }

    public List<String> getAllMethodsFromStackTraces(List<String> traces) {
        List<String> methods = new ArrayList<>();
        traces.forEach(t -> {
            List<String> splits = Arrays.stream(t.split("\\.")).toList();
            String methodName = splits.get(splits.size() - 1);
            methods.add(methodName);
        });
        List<String> uniqueMethods = methods.stream().distinct().collect(Collectors.toList());
        return uniqueMethods;
    }

    public String getMethodFromStackTrace(String traces) {
        List<String> splits = Arrays.stream(traces.split("\\.")).toList();
        return splits.get(splits.size() - 1);
    }

    public String getClassFromStackTrace(String traces) {
        List<String> splits = Arrays.stream(traces.split("\\.")).toList();
        String className = null;
        if (splits.size() >= 2) {
            className = splits.get(splits.size() - 2);
        }
        return className;
    }
}
