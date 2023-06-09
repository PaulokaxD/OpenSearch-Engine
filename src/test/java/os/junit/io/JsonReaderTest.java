package os.junit.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import os.exercise.io.JsonReader;
import os.exercise.models.Article;

/**
 * Unitary tests for the JsonReader class.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JsonReaderTest {

    private Path articlesFilePath;
    private List<Article> articles;

    @BeforeAll
    void setUp() {

        articlesFilePath = Paths.get("src\\test\\resources\\articles-example.json");

        Article article1 = new Article();
        article1.setId("pmid:14663354");
        article1.setArticleTitle("Acute immunodeficiency, multiple organ injury, and the pathogenesis of SARS.");
        article1.setAbstractText("Antibody production tests have traditionally");
        article1.setLanguage(Collections.singletonList("eng"));
        article1.setPubDate("2003-12-01");
        article1.setFilterSources(Collections.singletonList("cord19"));
        article1.setKeywords(Arrays.asList("kw1", "kw3"));
        article1.setPubTypes(Collections.singletonList("Editorial"));

        Article article2 = new Article();
        article2.setId("pmid:15224676");
        article2.setArticleTitle("Diagnostic criteria during SARS outbreak in Hong Kong.");
        article2.setAbstractText("Abstract text!");
        article2.setLanguage(Collections.singletonList("eng"));
        article2.setPubDate("2004-06-01");
        article2.setFilterSources(Collections.singletonList("cord19"));
        article2.setKeywords(Arrays.asList("kw1", "kw2", "kw3"));
        article2.setPubTypes(Collections.singletonList("Letter"));

        Article article3 = new Article();
        article3.setId("pmid:15235643");
        article3.setArticleTitle("Virus PCR assay panels: an alternative to the mouse antibody production test.");
        article3.setAbstractText("been used to test biological materials for viral contamination.");
        article3.setLanguage(Arrays.asList("eng", "esp", "ger"));
        article3.setPubDate("2004-02-01");
        article3.setFilterSources(Collections.singletonList("cord19"));
        article3.setKeywords(Arrays.asList("kw1", "kw2"));
        article3.setPubTypes(Arrays.asList("Comparative Study", "Journal Article"));

        articles = Arrays.asList(article1, article2, article3);
    }

    @Test
    void givenArticlesFilePathWhenReadArticlesFileThenReturnArticlesList() {
        List<Article> actual = JsonReader.readArticlesFile(articlesFilePath);
        assertEquals(articles, actual);
    }

    @Test
    void givenArticlesFilePathAndPartitionSizeWhenReadArticlesFileIntoPartitionThenSuccess() {
        List<List<Article>> actual = JsonReader.readArticlesFileIntoPartition(articlesFilePath,1);
        assertEquals(3, actual.size());
        for(List<Article> batch : actual){
            assertEquals(1, batch.size());
        }
    }

    @Test
    void givenNonExistingFileWhenReadArticlesFileThenFailure() {
        Path wrongPath = Paths.get("src/test/resources/non-existing-file.json");
        List<Article> actual = JsonReader.readArticlesFile(wrongPath);
        assertEquals(0, actual.size());
    }

    @Test
    void givenArticlesFilePathWhenGetFileNumberThenSuccess() {
        assertEquals(3, JsonReader.getFileNumber(articlesFilePath));
    }

}
