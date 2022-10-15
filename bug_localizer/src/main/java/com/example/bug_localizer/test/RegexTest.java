package com.example.bug_localizer.test;

public class RegexTest {

    public void splitCamelCase(String camelCase) {
//        references: https://stackoverflow.com/questions/7593969/regex-to-split-camelcase-or-titlecase-advanced

        for (String w : camelCase.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
            System.out.println(w);
        }
    }

    public static void main(String[] args) {
        RegexTest regexTest = new RegexTest();
        regexTest.splitCamelCase("NumsAt12345TheStartIncludedToo");
    }
}
