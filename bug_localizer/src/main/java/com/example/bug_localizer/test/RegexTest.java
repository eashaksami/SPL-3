package com.example.bug_localizer.test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RegexTest {

    public String splitCamelCase(String camelCase) {
//        references: https://stackoverflow.com/questions/7593969/regex-to-split-camelcase-or-titlecase-advanced

        List<String> splittedWord = new ArrayList<>();
        for (String w : camelCase.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
            splittedWord.add(w);
//            System.out.println(w);
        }
        return splittedWord.stream().collect(Collectors.joining(" "));
    }

    public static void main(String[] args) {
        RegexTest regexTest = new RegexTest();
        String splited = regexTest.splitCamelCase("NumsAt12345TheStartIncludedToo");
        System.out.println(splited);
    }
}
