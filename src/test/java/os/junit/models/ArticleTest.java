package os.junit.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import os.exercise.models.Article;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArticleTest {

    Article article;

    @BeforeEach
    void setUp(){
        article = new Article();
    }

    @Test
    void whenSetAndGetIdThenSuccess() {
        article.setId("pmid:11164502");
        assertEquals("pmid:11164502", article.getId());
    }

    @Test
    void whenSetAndGetArticleTitleThenSuccess() {
        article.setArticleTitle("A single chain Fv antibody displayed");
        assertEquals("A single chain Fv antibody displayed", article.getArticleTitle());
    }

    @Test
    void whenSetAndGetAbstractTextThenSuccess() {
        article.setAbstractText("A single chain fragment variable (scFv) antibody");
        assertEquals("A single chain fragment variable (scFv) antibody", article.getAbstractText());
    }

    @Test
    void whenSetAndGetLanguageThenSuccess() {
        article.setLanguage(Arrays.asList("eng", "esp"));
        assertEquals(Arrays.asList("eng", "esp"), article.getLanguage());
    }

    @Test
    void whenSetAndGetPubDateThenSuccess() {
        article.setPubDate("2022-01-01");
        assertEquals("2022-01-01", article.getPubDate());
    }

    @Test
    void whenSetAndGetFilterSourcesThenSuccess() {
        article.setFilterSources(Arrays.asList("cord19", "cord191"));
        assertEquals(Arrays.asList("cord19", "cord191"), article.getFilterSources());
    }

    @Test
    void whenSetAndGetKeywordsThenSuccess() {
        article.setKeywords(Arrays.asList("Keyword 1", "Keyword 2"));
        assertEquals(Arrays.asList("Keyword 1", "Keyword 2"), article.getKeywords());
    }

    @Test
    void whenSetAndGetPubTypesThenSuccess() {
        article.setPubTypes(Arrays.asList("Journal Article","Research Support, Non-U.S. Gov't"));
        assertEquals(Arrays.asList("Journal Article","Research Support, Non-U.S. Gov't"), article.getPubTypes());
    }
}
