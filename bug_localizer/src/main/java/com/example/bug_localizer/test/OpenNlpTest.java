package com.example.bug_localizer.test;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class OpenNlpTest {

    public void posTagging() throws IOException {
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

        String[] tokens = tokenizer.tokenize("smap generated JspC Ant precompilation noticed Tomcat pass JspC task smapSuppressed attribute appears source level debugging JSPs precompiled JspC smapSuppressed Note JSP source level debuggable set compiled fly Tomcat baffled work");

        InputStream inputStreamPOSTagger = new FileInputStream(
        "/home/sami/Desktop/SPL-3/SPL-3/bug_localizer/src/en-pos-maxent.bin");
        POSModel posModel = new POSModel(inputStreamPOSTagger);
        POSTaggerME posTagger = new POSTaggerME(posModel);
        String tags[] = posTagger.tag(tokens);
        for(int i = 0; i < tags.length; i++) {
            System.out.println(tokens[i] + ": " + tags[i]);
        }
    }

    public static void main(String[] args) throws IOException {
        OpenNlpTest test = new OpenNlpTest();
        test.posTagging();
    }
}
