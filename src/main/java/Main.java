import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    private static final Integer N_INDEXING_FILES = 100;

    private static <T> List<List<T>> partition(List<T> collection, int partitionSize){
        List<List<T>> partitions = new ArrayList<>();

        for (int i = 0; i < collection.size(); i += partitionSize) {
            partitions.add(collection.subList(i, Math.min(i + partitionSize, collection.size())));
        }
        return partitions;
    }

    public static void main(String[] args) throws IOException {
        ArticlesIndexer indexer = new ArticlesIndexer();
        Path folderPath = Paths.get("data");
        List<Path> pathStream = Files.walk(folderPath)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .collect(Collectors.toList());

        Integer nFile = 1;
        for(Path path : pathStream) {
            System.out.println("Processing file ".join(path.toString()));
            List<Article> articles = JSONReader.readFile(path);
            List<List<Article>> articlesBatches = partition(articles, N_INDEXING_FILES);

            Integer nBatch = 1;
            for(List<Article> articlesBatch : articlesBatches){
                indexer.bulkAppendArticles(articlesBatch);
                System.out.println("File "+nFile+"/"+pathStream.size()+" -> "+
                        Math.min(N_INDEXING_FILES*nBatch,articles.size())+"/"+articles.size());
                nBatch++;
            }

        nFile++;
        }

        indexer.close();
    }

}

