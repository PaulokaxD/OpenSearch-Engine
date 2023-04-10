import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JSONReader {

    public static List<Article> readFile(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Article> articles = new ArrayList<>();
        String line;
        File file = path.toFile();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while ((line = reader.readLine()) != null) {
            Article article = mapper.readValue(line, Article.class);
            articles.add(article);
        }
        return articles;
    }
}
