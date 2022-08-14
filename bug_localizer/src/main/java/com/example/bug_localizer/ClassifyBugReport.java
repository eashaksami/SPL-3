package com.example.bug_localizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassifyBugReport {

    public boolean haveStackTrace(String bugReport) {
        Pattern p = Pattern.compile("(.*)?(.+)\\.(.+)(\\((.+)\\.java:\\d+\\)|\\(Unknown Source\\)|\\(Native Method\\))");
        Matcher m = p.matcher(bugReport);
        boolean b = m.matches();
        return b;
    }

    public static void main(String[] args) {
        ClassifyBugReport classifyBugReport = new ClassifyBugReport();
        boolean haveTraces = classifyBugReport.haveStackTrace("bug report content");
        if(haveTraces) {
            System.out.println("Type is ST");
        }
    }

}
