package com.example.bug_localizer.test;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OpenNlp {

    public Map<String, Set<String>> posTagging(String query) throws IOException {
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

        String[] tokens = tokenizer.tokenize(query);

        InputStream inputStreamPOSTagger = new FileInputStream(
        "/home/sami/Desktop/SPL-3/SPL-3/bug_localizer/src/en-pos-maxent.bin");
        POSModel posModel = new POSModel(inputStreamPOSTagger);
        POSTaggerME posTagger = new POSTaggerME(posModel);
        String tags[] = posTagger.tag(tokens);
        Map<String, Set<String>> posMap = new HashMap<>();
        for(int i = 0; i < tags.length; i++) {
            if(!posMap.containsKey(tags[i])) {
                Set<String> value = new HashSet<>();
                value.add(tokens[i]);
                posMap.put(tags[i], value);
            } else {
                Set<String> updatedSet = posMap.get(tags[i]);
                updatedSet.add(tokens[i]);
                posMap.put(tags[i], updatedSet);
            }
            System.out.println(tokens[i] + ": " + tags[i]);
        }
        return posMap;
    }

    public static void main(String[] args) throws IOException {
        OpenNlp openNlp = new OpenNlp();
        String query = "element reported plain flat element hierarchical java search view";
        openNlp.posTagging(query);
    }
}
