package com.example.bug_localizer.test;

import com.example.bug_localizer.staticData.StaticData;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;

public class LuceneIndexer {

    IndexWriter writer;

    public LuceneIndexer(String directoryPath) throws IOException {
        Directory indexDirectory = FSDirectory.open(Paths.get(directoryPath));
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(indexDirectory, indexWriterConfig);
    }

    public Document getDocument(File file) throws IOException {
        Document document = new Document();

        TextField content = new TextField("contents", new FileReader(file));
        TextField filename = new TextField("filename", file.getName(), Field.Store.YES);
        TextField filepath = new TextField("filepath", file.getCanonicalPath(), Field.Store.YES);

        document.add(content);
        document.add(filename);
        document.add(filepath);

        return document;
    }

    public void indexFile(File file) throws IOException {
        System.out.println("Indexing "+ file.getCanonicalPath());
        Document document = getDocument(file);
        writer.addDocument(document);
    }

    public void createIndex(String dataDirPath) throws IOException {
        File[] files = new File(dataDirPath).listFiles();

        for (File file: files) {
            if(file.isDirectory()) {
                createIndex(file.getPath());
            } else if(file.getName().toLowerCase().endsWith(".java")){
                indexFile(file);
            }
        }
        writer.commit();
    }

    public static void main(String[] args) throws IOException {
        LuceneIndexer luceneIndexer = new LuceneIndexer(StaticData.indexDir);
        luceneIndexer.createIndex(StaticData.dataDir);
    }
}
