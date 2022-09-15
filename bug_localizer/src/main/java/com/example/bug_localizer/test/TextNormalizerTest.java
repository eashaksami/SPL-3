package com.example.bug_localizer.test;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextNormalizerTest {
    public List<String> removeStopWords(String query) throws IOException {
        CharArraySet enStopSet = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;
        List<String> result = analyze(query, new StopAnalyzer(enStopSet));
        System.out.println(result);
        return result;
    }

    public List<String> analyze(String text, Analyzer analyzer) throws IOException{
        List<String> result = new ArrayList<String>();
        TokenStream tokenStream = analyzer.tokenStream("FIELD_NAME", text);
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while(tokenStream.incrementToken()) {
            result.add(attr.toString());
        }
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
