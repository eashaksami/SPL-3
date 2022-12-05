package com.example.bug_localizer.utils.graph;

import com.example.bug_localizer.test.OpenNlp;

import java.io.IOException;
import java.util.*;

public class CreateGraphForProgramElement {

    Map<Integer, String> wordMap = new HashMap<>();
    public int[][] representGraphAsMatrix(List<String> words, Map<Integer, String> wordMap, int[][] graph) {

//        for(String sentence: sentences) {
            Integer previousWordIndex = null;
            Integer currentWordIndex = null;
            Integer nextWordIndex = null;
//            String[] words = sentence.split("\\s+");
            for(int i = 0; i < words.size(); i++) {
                String previousWord = null;
                String nextWord = null;
                String currentWord = words.get(i);
                if(i > 0) {
                    previousWord = words.get(i-1);
                }
                if(i < words.size() - 1) {
                    nextWord = words.get(i+1);
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
//        }
//        printTraceGraph(graph, wordMap);
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

//    public Map<String, List<String>>
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

    public int[][] createGraphWithHierarchicalLink(int[][] graph, Map<Integer, String> wordMap, List<String> sourceNodes, List<String> destinationNodes) {
        for (String sourceNode : sourceNodes) {
            for (String destinationNode : destinationNodes) {
                Integer sourceNodeIndex = null;
                Integer destinationNodeIndex = null;
                for (Map.Entry<Integer, String> entry : wordMap.entrySet()) {
                    if (entry.getValue().equals(sourceNode)) {
                        sourceNodeIndex = entry.getKey();
//                    System.out.println(entry.getKey());
                    }
                    else if (entry.getValue().equals(destinationNode)) {
                        destinationNodeIndex = entry.getKey();
//                    System.out.println(entry.getKey());
                    }
                }
                if(sourceNodeIndex != null && destinationNodeIndex != null) {
                    graph[sourceNodeIndex][destinationNodeIndex] = 1;
                }
            }
        }
        return graph;
    }

    public int[][] createPosGraph(List<String> sentences, Map<Integer, String> wordMap, int[][] graph) throws IOException {
        for(String sentence : sentences) {
            ArrayList<String> nouns = new ArrayList<>();
            ArrayList<String> verbs = new ArrayList<>();
            ArrayList<String> adjectives = new ArrayList<>();
            ArrayList<String> adverbs = new ArrayList<>();
            Map<String, Set<String>> posTaggedWords = getPosTaggedWords(sentence);
            for(String pos : posTaggedWords.keySet()) {
                if(pos.contains("NN")) nouns.addAll(posTaggedWords.get(pos));
                else if(pos.contains("VB")) verbs.addAll(posTaggedWords.get(pos));
                else if (pos.contains("JJ")) adjectives.addAll(posTaggedWords.get(pos));
                else if(pos.contains("RB")) adverbs.addAll(posTaggedWords.get(pos));
            }
            graph = representGraphAsMatrix(nouns, wordMap, graph);
            graph = representGraphAsMatrix(verbs, wordMap, graph);

            graph = createGraphWithHierarchicalLink(graph, wordMap, verbs, nouns);
            graph = createGraphWithHierarchicalLink(graph, wordMap, verbs, adjectives);
            graph = createGraphWithHierarchicalLink(graph, wordMap, adverbs, verbs);
        }
        System.out.println("Final graph======>");
        printTraceGraph(graph, wordMap);
        return graph;
    }

    public Map<String, Set<String>> getPosTaggedWords(String query) throws IOException {
        OpenNlp openNlp = new OpenNlp();
        return openNlp.posTagging(query);
    }

    public static void main(String[] args) throws IOException {
        CreateGraphForProgramElement graphForProgramElement = new CreateGraphForProgramElement();
        List<String> sentences = new ArrayList<>();
        sentences.add("element reported plain flat element hierarchical java search view");

        Map<Integer, String> wordMap = graphForProgramElement.representStringToMap(sentences);
        int graph[][] = new int[wordMap.size()][wordMap.size()];
        System.out.println(wordMap);
        graphForProgramElement.createPosGraph(sentences, wordMap, graph);
//        System.out.println(graphForProgramElement.representGraphAsMatrix(sentences, wordMap, graph));
    }
}
