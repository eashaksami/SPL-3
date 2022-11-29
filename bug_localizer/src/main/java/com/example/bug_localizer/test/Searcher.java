package com.example.bug_localizer.test;

import com.example.bug_localizer.staticData.StaticData;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class Searcher {
    public static IndexSearcher indexSearcher;
    QueryParser queryParser;

    public Searcher(String indexDirectoryPath) throws IOException {
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexReader reader = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(reader);
        queryParser = new QueryParser("contents", new StandardAnalyzer());
    }

    public TopDocs search(String searchQuery) throws ParseException, IOException {
        Query query = queryParser.parse(searchQuery);
        return indexSearcher.search(query, StaticData.topHitDocs);
    }

    public TopDocs searchNDocs(String searchQuery) throws ParseException, IOException {
        Query query = queryParser.parse(searchQuery);
        return indexSearcher.search(query, 1000);
    }

    public static void main(String[] args) throws IOException, ParseException {
        Searcher searcher = new Searcher(StaticData.indexDir);
        String searchQuery = "sami";

        TopDocs hits = searcher.search(searchQuery);
        System.out.println(hits.totalHits + " documents found. ");

        for(ScoreDoc scoreDoc: hits.scoreDocs) {
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println("File: " + document.get("filepath") + "Score: " + scoreDoc.score);
        }
    }
}
