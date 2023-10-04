package com.example.bug_localizer.utils.graph;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CreateGraphFromNaturalLanguage {

    public int[][] representGraphAsMatrix(List<String> sentences, Map<Integer, String> wordMap) {
        int[][] graph = new int[wordMap.size()][wordMap.size()];
        for(String sentence: sentences) {
            Integer previousWordIndex = null;
            Integer currentWordIndex = null;
            Integer nextWordIndex = null;
            String[] words = sentence.split("\\s+");
            for(int i = 0; i < words.length; i++) {
                String previousWord = null;
                String nextWord = null;
                String currentWord = words[i];
                if(i > 0) {
                    previousWord = words[i-1];
                }
                if(i < words.length - 1) {
                    nextWord = words[i+1];
                }

                for (Map.Entry<Integer, String> entry : wordMap.entrySet()) {
                    if (entry.getValue().equals(currentWord)) {
                        currentWordIndex = entry.getKey();
//                    System.out.println(entry.getKey());
                    }
                    else if (entry.getValue().equals(previousWord)) {
                        previousWordIndex = entry.getKey();
//                    System.out.println(entry.getKey());
                    }
                    else if (entry.getValue().equals(nextWord)) {
                        nextWordIndex = entry.getKey();
//                    System.out.println(entry.getKey());
                    }
                }
                if(previousWord != null && previousWordIndex != null) {
                    graph[currentWordIndex][previousWordIndex] = 1;
                }
                if(nextWord != null && nextWordIndex != null) {
                    graph[currentWordIndex][nextWordIndex] = 1;
                }
            }
        }
        printTraceGraph(graph, wordMap);
        return graph;
    }

    public void printTraceGraph(int[][] graph, Map<Integer, String> tracesMap) {
        for(int i = 0; i < tracesMap.size(); i++) {
            for(int j = 0; j < tracesMap.size(); j++) {
                if(graph[i][j] == 1) {
                    System.out.println("Graph value: " + tracesMap.get(i) + "=>" + tracesMap.get(j));
                }
            }
        }
    }

    public Map<Integer, String> representStringToMap(List<String> sentences) {
        System.out.println(sentences);
        Map<Integer, String> wordMap = new HashMap<>();
        int i = 0;
        for(String sentence: sentences) {
            String[] words = sentence.split("\\s+");
            for (String word: words) {
                if(wordMap.containsValue(word)) continue;
                wordMap.put(i, word);
                i++;
            }
        }
        return wordMap;
    }
}
