package com.example.bug_localizer.test;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TextNormalizerTest {
    List<String> javaKeyWords = new ArrayList<>();
    CharArraySet enStopSet;

    public TextNormalizerTest() throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream stream = classloader.getResourceAsStream("JavaKeywords.txt");
        Scanner sc = new Scanner(stream);
        while (sc.hasNextLine())  {
            javaKeyWords.add(sc.nextLine());
        }
        this.enStopSet = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;
    }
    public List<String> removeStopWords(String query) throws IOException {
//        CharArraySet enStopSet = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;
//        enStopSet.add(javaKeyWords.get(0));
//        System.out.println(enStopSet);
        List<String> result = analyze(query, new StopAnalyzer(this.enStopSet));
        System.out.println(result);
        return result;
    }

    public List<String> analyze(String text, Analyzer analyzer) throws IOException{
        List<String> result = new ArrayList<>();
        TokenStream tokenStream = analyzer.tokenStream("FIELD_NAME", text);
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while(tokenStream.incrementToken()) {
            result.add(attr.toString());
        }
        return result;
    }

    public List<String> removeStopWordsAndJavaKeywords(String query) throws IOException {
        CharArraySet stopSet = new CharArraySet(javaKeyWords, false);
        stopSet.addAll(this.enStopSet);
        List<String> result = analyze(query, new StopAnalyzer(stopSet));
        System.out.println(result);
        return result;
    }

    public static void main(String[] args) throws IOException {
        String query = "48131 � Add @deprecated annotations to deprecated elements\n" +
                "Created attachment 24475 [details]\n" +
                "Patch to add @deprecated annotations to deprecated elements\n" +
                "Patch to add @deprecated annotations to deprecated elements";
        TextNormalizerTest textNormalizerTest = new TextNormalizerTest();
        textNormalizerTest.removeStopWords(query);
    }
}
