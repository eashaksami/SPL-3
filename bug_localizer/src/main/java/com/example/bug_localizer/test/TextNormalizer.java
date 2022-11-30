package com.example.bug_localizer.test;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class TextNormalizer {
    List<String> javaKeyWords = new ArrayList<>();
    CharArraySet enStopSet;
    List<String> enStopList = new ArrayList<>();

    public TextNormalizer() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/JavaKeywords.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            Scanner sc = new Scanner(reader);
            while (sc.hasNextLine())  {
                javaKeyWords.add(sc.nextLine());
            }
        }
        try (InputStream in = getClass().getResourceAsStream("/stopwords_en.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            Scanner sc = new Scanner(reader);
            while (sc.hasNextLine())  {
                enStopList.add(sc.nextLine());
            }
        }
        enStopSet = new CharArraySet(enStopList, false);
    }
    public List<String> removeStopWords(String query) throws IOException {
//        CharArraySet enStopSet = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;
//        enStopSet.add(javaKeyWords.get(0));
//        System.out.println(enStopSet);
        List<String> result = analyze(query, new StopAnalyzer(this.enStopSet));
        System.out.println(result);
        return result;
    }

    public List<String> removeStopWords(List<String> sentences) throws IOException {
        List<String> result = new ArrayList<>();
        sentences.forEach(sentence -> {
            try {
                String modifiedSentence = analyze(sentence, new StopAnalyzer(this.enStopSet)).stream().collect(Collectors.joining(" "));
                if(modifiedSentence.length() > 0) {
                    result.add(modifiedSentence);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return result;
    }

    public String extractProgramElements(String sentence) {
        /**
         * append extracted code element at the last of the string...
         * */
        sentence = sentence.replace("\n", ". ");
        String[] splitSentence = sentence.split("\\s+");
        String processedSentence = "";
        for(String word : splitSentence) {
            processedSentence += word + " ";
            Regex regex = new Regex();
            String splited = regex.splitCamelCase(word);
            if(word.length() < splited.length()) {
                processedSentence += splited + " ";
            }
        }
        return processedSentence;
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
        TextNormalizer textNormalizer = new TextNormalizer();
        textNormalizer.removeStopWords(query);
    }
}
