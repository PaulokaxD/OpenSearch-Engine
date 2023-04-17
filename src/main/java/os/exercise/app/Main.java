package os.exercise.app;

import os.exercise.io.JSONReader;
import os.exercise.opensearch.ArticlesIndexer;
import os.exercise.pojo.Article;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static final Integer N_INDEXING_FILES = 100;
    private static final String INDEX_NAME = "articles";

    public static void main(String[] args) {
        Path folderPath = Paths.get("data");
        List<Path> paths = new ArrayList<>();

        try (Stream<Path> pathStream = Files.walk(folderPath)) {
            paths = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .collect(Collectors.toList());
        } catch (IOException e){
            e.printStackTrace();
        }

        ArticlesIndexer indexer = new ArticlesIndexer("localhost", 9200, "http");
        int nFile = 1;
        for(Path path : paths) {
            System.out.println("Processing file " + path.toString());

            int fileNumber = JSONReader.getFileNumber(path);
            List<List<Article>> articlesBatches = JSONReader.readArticlesFileIntoPartition(path, N_INDEXING_FILES);

            Integer nBatch = 1;
            for(List<Article> articlesBatch : articlesBatches){
                indexer.bulkAppendArticles(INDEX_NAME, articlesBatch);
                System.out.println("File "+nFile+"/"+paths.size()+" -> "+
                        Math.min(N_INDEXING_FILES*nBatch,fileNumber)+"/"+fileNumber);
                nBatch++;
            }
            nFile++;
        }
        indexer.close();
    }

}

